package com.idaoben.web.monitor.web.application;

import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Task;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.MonitoringService;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.service.TaskService;
import com.idaoben.web.monitor.service.impl.MonitoringTask;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.IdCommand;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import com.idaoben.web.monitor.web.command.TaskListCommand;
import com.idaoben.web.monitor.web.dto.ProcessJson;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import com.idaoben.web.monitor.web.dto.TaskDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MonitorApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorApplicationService.class);

    @Resource
    private SoftwareApplicationService softwareApplicationService;

    @Resource
    private ActionApplicationService actionApplicationService;

    @Resource
    private TaskService taskService;

    @Resource
    private SystemOsService systemOsService;

    @Resource
    private MonitoringService monitoringService;

    @Value("${monitor.linux-user:}")
    private String linuxUser;

    public void startMonitor(SoftwareIdCommand command){
        String softwareId = command.getId();
        if(monitoringService.isMonitoring(softwareId)){
            throw ServiceException.of(ErrorCode.MONITOR_ON_GOING);
        }
        //????????????
        if(CollectionUtils.isEmpty(monitoringService.getProcessPids(softwareId))){
            throw ServiceException.of(ErrorCode.SOFTWARE_NOT_RUNING);
        }

        //??????????????????????????????????????????????????????????????????????????????
        setTaskComplete(softwareId);

        MonitoringTask monitoringTask = new MonitoringTask();
        monitoringTask.setSoftwareId(softwareId);
        monitoringService.putMonitoringTask(softwareId, monitoringTask);
        //????????????????????????
        Task task = new Task();
        task.setStartTime(ZonedDateTime.now());
        task.setSoftwareId(softwareId);
        SoftwareDto softwareDto = softwareApplicationService.getSoftwareInfo(softwareId);
        if(softwareDto != null){
            task.setSoftwareName(softwareDto.getSoftwareName());
            task.setExePath(softwareDto.getExePath());
        }

        List<Integer> pids = null;
        List<String> users = null;
        //???????????????????????????Linux???Windows???????????????????????????????????????????????????
        //if(SystemUtils.isWindows()){
            //????????????pid???????????????
            List<ProcessJson> processes = monitoringService.getProcessPids(softwareId);
            if(processes != null){
                pids = processes.stream().map(processJsonDto -> processJsonDto.getPid()).collect(Collectors.toList());
                users = processes.stream().map(processJsonDto -> processJsonDto.getUser()).collect(Collectors.toList());
            }
//        } else {
//            //Find the main process
//            Integer pid = null;
//            String user = null;
//            List<ProcessJson> processes = monitoringService.getProcessPids(softwareId);
//            if(processes != null){
//                pids = processes.stream().map(processJsonDto -> processJsonDto.getPid()).collect(Collectors.toList());
//                for(ProcessJson process : processes){
//                    if(process.getParentPid().intValue() == 1 || pids.contains(process.getParentPid())){
//                        pid = process.getPid();
//                        user = process.getUser();
//                        break;
//                    }
//                }
//                if(pid == null){
//                    pid = processes.get(0).getPid();
//                    user = processes.get(0).getUser();
//                }
//                pids = Arrays.asList(pid);
//                users = Arrays.asList(user);
//            }
//        }
        if(pids != null){
            for(Integer pid : pids){
                logger.info("??????PID: {}??????????????????", pid);
                String resultCode = systemOsService.attachAndInjectHooks(pid);
                if(resultCode == null){
                    logger.error("?????????????????????SoftwareId: {}, PID???{}", softwareId, pid);
                    monitoringService.addMonitoringPid(monitoringTask, String.valueOf(pid));
                } else {
                    monitoringTask.getErrorPids().add(String.valueOf(pid));
                    logger.error("?????????????????????SoftwareId: {}, PID???{}", softwareId, pid);
                    if(!systemOsService.isAutoMonitorChildProcess()){
                        //cLose monitoring
                        monitoringService.removeMonitoringTask(softwareId);
                        throw ServiceException.of(resultCode);
                    }
                }
            }
            task.setPidUsers(pids, users);
            task = taskService.save(task);
            monitoringTask.setTaskId(task.getId());
            for(String pid : monitoringService.getMonitoringPids(monitoringTask)){
                actionApplicationService.startActionScan(pid, task.getId());
            }
        } else {
            throw ServiceException.of(ErrorCode.SOFTWARE_NOT_RUNING);
        }
    }

    public void startMonitorPid(String softwareId, String pid, String user){
        MonitoringTask monitoringTask = monitoringService.getMonitoringTask(softwareId);
        if(monitoringTask == null){
            //????????????????????????????????????
            return;
        }
        logger.info("????????????PID: {}??????????????????", pid);
        String resultCode = systemOsService.attachAndInjectHooks(Integer.parseInt(pid));
        if(resultCode == null){
            //?????????????????????pid????????????????????????task???????????????
            monitoringService.addMonitoringPid(monitoringTask, pid);
            actionApplicationService.startActionScan(pid, monitoringTask.getTaskId());
            Task task = taskService.findStrictly(monitoringTask.getTaskId());
            task.addPidUser(pid, user);
            taskService.save(task);
        } else {
            monitoringTask.getErrorPids().add(pid);
            logger.error("?????????????????????SoftwareId: {}, PID???{}", softwareId, pid);
        }

    }

    public boolean stopMonitor(SoftwareIdCommand command){
        return stopMonitor(command.getId(), true);
    }

    public boolean stopMonitor(String softwareId, boolean needRemove){
        MonitoringTask monitoringTask = monitoringService.getMonitoringTask(softwareId);
        boolean isAllSuccess = true;
        if(monitoringTask != null){
            List<String> successPids = new ArrayList<>();
            for(String pid : monitoringService.getMonitoringPids(monitoringTask)){
                boolean result = needRemove ? systemOsService.removeHooks(Integer.valueOf(pid)) : true;
                if(!result){
                    logger.error("???????????????????????????SoftwareId: {}, PID???{}", softwareId, pid);
                    isAllSuccess = false;
                }
                actionApplicationService.stopActionScan(pid);
                successPids.add(pid);
            }
            monitoringService.removeMonitoringPids(monitoringTask, successPids);
        }

        //??????????????????????????????????????????????????????????????????????????????
        setTaskComplete(softwareId);
        monitoringService.removeMonitoringTask(softwareId);
        return isAllSuccess;
    }

    public void startAndMonitor(SoftwareIdCommand command){
        String softwareId = command.getId();
        if(monitoringService.isMonitoring(softwareId)){
            throw ServiceException.of(ErrorCode.MONITOR_ON_GOING);
        }
        //????????????
        if(!CollectionUtils.isEmpty(monitoringService.getProcessPids(softwareId))){
            throw ServiceException.of(ErrorCode.SOFTWARE_RUNING);
        }

        //??????????????????????????????????????????????????????????????????????????????
        setTaskComplete(softwareId);

        SoftwareDto software = softwareApplicationService.getSoftwareInfo(softwareId);
        if(software == null){
            throw ServiceException.of(ErrorCode.CODE_REQUESE_PARAM_ERROR);
        }
        int pid = systemOsService.startProcessWithHooks(software.getCommandLine(), software.getExecutePath());
        logger.info("?????????????????????ID:{}??? ???????????????{}", software.getId(), pid);
        if(pid > 0){
            String pidStr = String.valueOf(pid);
            Task task = new Task();
            task.setStartTime(ZonedDateTime.now());
            task.setSoftwareId(softwareId);
            task.setSoftwareName(software.getSoftwareName());
            task.setExePath(software.getExePath());
            //??????????????????
            task.addPidUser(pidStr, StringUtils.isNotEmpty(linuxUser) ? linuxUser : SystemUtils.getUserName());
            task = taskService.save(task);
            MonitoringTask monitoringTask = new MonitoringTask();
            monitoringTask.setSoftwareId(softwareId);
            monitoringService.addMonitoringPid(monitoringTask, pidStr);
            monitoringService.putMonitoringTask(softwareId, monitoringTask);
            monitoringTask.setTaskId(task.getId());

            actionApplicationService.startActionScan(pidStr, task.getId());
        } else {
            logger.error("???????????????????????????????????????????????????{}", software.getCommandLine());
            throw ServiceException.of(ErrorCode.START_AND_MONITOR_ERROR);
        }
    }

    public Page<TaskDto> listTask(TaskListCommand command, Pageable pageable){
        Page<Task> tasks = taskService.findPage(Filters.query().eq(Task::getSoftwareId, command.getSoftwareId()).likeFuzzy(Task::getPids, command.getPid()).ge(Task::getStartTime, command.getStartTime()).le(Task::getStartTime, command.getEndTime()), pageable);
        return DtoTransformer.asPage(TaskDto.class).apply(tasks);
    }

    public void deleteTask(IdCommand command){
        taskService.deleteTaskAndAction(command.getId());
    }

    public Set<String> getMonitoringSoftwareIds(){
        return monitoringService.getMonitoringSoftwareIds();
    }

    private void setTaskComplete(String softwareId){
        //??????????????????????????????????????????????????????????????????????????????
        List<Task> tasks = taskService.findList(Filters.query().eq(Task::getSoftwareId, softwareId).eq(Task::isComplete, false), null);
        for(Task task : tasks){
            task.setEndTime(ZonedDateTime.now());
            task.setComplete(true);
            taskService.save(task);
        }
    }
}
