package com.idaoben.web.monitor.web.application;

import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Task;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.service.TaskService;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import com.idaoben.web.monitor.web.command.TaskListCommand;
import com.idaoben.web.monitor.web.dto.MonitoringTask;
import com.idaoben.web.monitor.web.dto.ProcessJson;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import com.idaoben.web.monitor.web.dto.TaskDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class MonitorApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorApplicationService.class);

    private Map<String, MonitoringTask> monitoringSoftwareTaskMap = new ConcurrentHashMap<>();

    @Resource
    private SoftwareApplicationService softwareApplicationService;

    @Resource
    private ActionApplicationService actionApplicationService;

    @Resource
    private TaskService taskService;

    @Resource
    private SystemOsService systemOsService;

    public void startMonitor(SoftwareIdCommand command){
        String softwareId = command.getId();
        if(monitoringSoftwareTaskMap.containsKey(softwareId)){
            throw ServiceException.of(ErrorCode.MONITOR_ON_GOING);
        }
        //启动监听
        if(CollectionUtils.isEmpty(softwareApplicationService.getProcessPids(softwareId))){
            throw ServiceException.of(ErrorCode.SOFTWARE_NOT_RUNING);
        }

        //查询当前软件是否还有未完全的监听任务，把任务置为完成
        setTaskComplete(softwareId);

        MonitoringTask monitoringTask = new MonitoringTask();
        monitoringTask.setSoftwareId(softwareId);
        monitoringSoftwareTaskMap.put(softwareId, monitoringTask);
        //创建一个监控任务
        Task task = new Task();
        task.setStartTime(ZonedDateTime.now());
        task.setSoftwareId(softwareId);
        SoftwareDto softwareDto = softwareApplicationService.getSoftwareInfo(softwareId);
        if(softwareDto != null){
            task.setSoftwareName(softwareDto.getSoftwareName());
            task.setExePath(softwareDto.getExePath());
        }
        //启动所有pid的监听线程
        List<ProcessJson> processes = softwareApplicationService.getProcessPids(softwareId);
        if(processes != null){
            List<Integer> pids = processes.stream().map(processJsonDto -> processJsonDto.getPid()).collect(Collectors.toList());
            for(Integer pid : pids){
                logger.info("启动PID: {}的注入监听。", pid);
                boolean result = systemOsService.attachAndInjectHooks(pid);
                if(result){
                    monitoringTask.getPids().add(String.valueOf(pid));
                } else {
                    monitoringTask.getErrorPids().add(String.valueOf(pid));
                    logger.error("注入进程失败，SoftwareId: {}, PID：{}", softwareId, pid);
                }
            }
            task.setPids(monitoringTask.getPids());
            task = taskService.save(task);
            monitoringTask.setTaskId(task.getId());
            for(String pid : monitoringTask.getPids()){
                actionApplicationService.startActionScan(pid, task.getId());
            }
        }
    }

    public void startMonitorPid(String softwareId, String pid){
        MonitoringTask monitoringTask = monitoringSoftwareTaskMap.get(softwareId);
        if(monitoringTask == null){
            //当前软件未监听，不做处理
            return;
        }
        logger.info("启动单个PID: {}的注入监听。", pid);
        boolean result = systemOsService.attachAndInjectHooks(Integer.parseInt(pid));
        if(result){
            //注入成功，增加pid到监听缓存和更新task的监听进程
            monitoringTask.getPids().add(pid);
            actionApplicationService.startActionScan(pid, monitoringTask.getTaskId());
            Task task = taskService.findStrictly(monitoringTask.getTaskId());
            task.setPids(monitoringTask.getPids());
            taskService.save(task);
        } else {
            monitoringTask.getErrorPids().add(pid);
            logger.error("注入进程失败，SoftwareId: {}, PID：{}", softwareId, pid);
        }

    }

    public void stopMonitor(SoftwareIdCommand command){
        String softwareId = command.getId();
        MonitoringTask monitoringTask = monitoringSoftwareTaskMap.get(softwareId);
        if(monitoringTask != null){
            List<String> successPids = new ArrayList<>();
            for(String pid : monitoringTask.getPids()){
                boolean result = systemOsService.removeHooks(Integer.valueOf(pid));
                if(result){
                    actionApplicationService.stopActionScan(pid);
                    successPids.add(pid);
                } else {
                    logger.error("解除注入进程错误，SoftwareId: {}, PID：{}", softwareId, pid);
                }
            }
            monitoringTask.getPids().removeAll(successPids);
        }

        //查询当前软件是否还有未完全的监听任务，把任务置为完成
        setTaskComplete(softwareId);
        monitoringSoftwareTaskMap.remove(command.getId());
    }

    public void startAndMonitor(SoftwareIdCommand command){
        String softwareId = command.getId();
        if(monitoringSoftwareTaskMap.containsKey(softwareId)){
            throw ServiceException.of(ErrorCode.MONITOR_ON_GOING);
        }
        //启动监听
        if(!CollectionUtils.isEmpty(softwareApplicationService.getProcessPids(softwareId))){
            throw ServiceException.of(ErrorCode.SOFTWARE_RUNING);
        }

        //查询当前软件是否还有未完全的监听任务，把任务置为完成
        setTaskComplete(softwareId);

        SoftwareDto software = softwareApplicationService.getSoftwareInfo(softwareId);
        if(software == null){
            throw ServiceException.of(ErrorCode.CODE_REQUESE_PARAM_ERROR);
        }
        int pid = systemOsService.startProcessWithHooks(software.getCommandLine(), software.getExecutePath());
        if(pid > 0){
            String pidStr = String.valueOf(pid);
            Task task = new Task();
            task.setStartTime(ZonedDateTime.now());
            task.setSoftwareId(softwareId);
            task.setSoftwareName(software.getSoftwareName());
            task.setExePath(software.getExePath());
            task.setPids(pidStr);
            task = taskService.save(task);
            MonitoringTask monitoringTask = new MonitoringTask();
            monitoringTask.setSoftwareId(softwareId);
            monitoringTask.getPids().add(pidStr);
            monitoringSoftwareTaskMap.put(softwareId, monitoringTask);
            monitoringTask.setTaskId(task.getId());

            actionApplicationService.startActionScan(pidStr, task.getId());
        } else {
            logger.error("启动并监听进程错误，软件启动命令：{}", software.getCommandLine());
            throw ServiceException.of(ErrorCode.START_AND_MONITOR_ERROR);
        }
    }

    public MonitoringTask getMonitoringTask(String softwareId){
        return monitoringSoftwareTaskMap.get(softwareId);
    }

    public boolean isMonitoring(String softwareId){
        return monitoringSoftwareTaskMap.containsKey(softwareId);
    }

    public boolean isPidMonitoring(String softwareId, String pid){
        MonitoringTask monitoringTask = monitoringSoftwareTaskMap.get(softwareId);
        if(monitoringTask != null){
            return monitoringTask.getPids().contains(pid);
        }
        return false;
    }

    public boolean isPidMonitoringError(String softwareId, String pid){
        MonitoringTask monitoringTask = monitoringSoftwareTaskMap.get(softwareId);
        if(monitoringTask != null){
            return monitoringTask.getErrorPids().contains(pid);
        }
        return false;
    }

    public Set<String> getMonitoringSoftwareIds(){
        return monitoringSoftwareTaskMap.keySet();
    }

    public Page<TaskDto> listTask(TaskListCommand command, Pageable pageable){
        Page<Task> tasks = taskService.findPage(Filters.query().eq(Task::getSoftwareId, command.getSoftwareId()).likeFuzzy(Task::getPids, command.getPid()).ge(Task::getStartTime, command.getStartTime()).le(Task::getStartTime, command.getEndTime()), pageable);
        return DtoTransformer.asPage(TaskDto.class).apply(tasks);
    }

    private void setTaskComplete(String softwareId){
        //查询当前软件是否还有未完全的监听任务，把任务置为完成
        List<Task> tasks = taskService.findList(Filters.query().eq(Task::getSoftwareId, softwareId).eq(Task::isComplete, false), null);
        for(Task task : tasks){
            task.setEndTime(ZonedDateTime.now());
            task.setComplete(true);
            taskService.save(task);
        }
    }
}
