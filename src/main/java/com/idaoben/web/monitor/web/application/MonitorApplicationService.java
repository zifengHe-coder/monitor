package com.idaoben.web.monitor.web.application;

import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Task;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.JniService;
import com.idaoben.web.monitor.service.TaskService;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import com.idaoben.web.monitor.web.command.TaskListCommand;
import com.idaoben.web.monitor.web.dto.ProcessJsonDto;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import com.idaoben.web.monitor.web.dto.TaskDto;
import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MonitorApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(MonitorApplicationService.class);

    private Map<String, List<String>> monitoringSoftwarePidMap = new HashMap<>();

    @Resource
    private SoftwareApplicationService softwareApplicationService;

    @Resource
    private ActionApplicationService actionApplicationService;

    @Resource
    private TaskService taskService;

    @Resource
    private JniService jniService;

    public void startMonitor(SoftwareIdCommand command){
        String softwareId = command.getId();
        if(monitoringSoftwarePidMap.containsKey(softwareId)){
            throw ServiceException.of(ErrorCode.MONITOR_ON_GOING);
        }
        //启动监听
        if(CollectionUtils.isEmpty(softwareApplicationService.getProcessPids(softwareId))){
            throw ServiceException.of(ErrorCode.SOFTWARE_NOT_RUNING);
        }

        //查询当前软件是否还有未完全的监听任务，把任务置为完成
        List<Task> tasks = taskService.findList(Filters.query().eq(Task::getSoftwareId, softwareId).eq(Task::isComplete, false), null);
        for(Task task : tasks){
            task.setEndTime(ZonedDateTime.now());
            task.setComplete(true);
            taskService.save(task);
        }

        List<String> injectPids = new ArrayList<>();
        monitoringSoftwarePidMap.put(softwareId, injectPids);
        //创建一个监控任务
        Task task = new Task();
        task.setStartTime(ZonedDateTime.now());
        task.setSoftwareId(softwareId);
        SoftwareDto softwareDto = softwareApplicationService.getSoftwareInfo(softwareId);
        if(softwareDto != null){
            task.setSoftwareName(softwareDto.getSoftwareName());
            task.setExePath(softwareDto.getExecutePath());
        }
        //启动所有pid的监听线程
        List<ProcessJsonDto> processes = softwareApplicationService.getProcessPids(softwareId);
        if(processes != null){
            List<Integer> pids = processes.stream().map(processJsonDto -> processJsonDto.getPid()).collect(Collectors.toList());

            for(Integer pid : pids){
                logger.info("启动PID: {}的注入监听。", pid);
                boolean result = jniService.attachAndInjectHooks(pid);
                if(result){
                    injectPids.add(String.valueOf(pid));
                } else {
                    logger.error("注入进程失败，SoftwareId: {}, PID：{}", softwareId, pid);
                }
            }
            task.setPids(StringUtils.join(injectPids, ','));
            task = taskService.save(task);
            for(String pid : injectPids){
                actionApplicationService.startActionScan(pid, task.getId());
            }
        }
    }

    public void stopMonitor(SoftwareIdCommand command){
        String softwareId = command.getId();
        List<String> pids = monitoringSoftwarePidMap.get(softwareId);
        if(pids != null){
            for(String pid : pids){
                boolean result = jniService.removeHooks(Integer.valueOf(pid));
                if(result){
                    actionApplicationService.stopActionScan(pid);
                } else {
                    logger.error("解除注入进程错误，SoftwareId: {}, PID：{}", softwareId, pid);
                }
            }
        }

        List<Task> tasks = taskService.findList(Filters.query().eq(Task::getSoftwareId, softwareId).eq(Task::isComplete, false), null);
        for(Task task : tasks){
            task.setEndTime(ZonedDateTime.now());
            task.setComplete(true);
            taskService.save(task);
        }
        monitoringSoftwarePidMap.remove(command.getId());

    }

    public void startAndMonitor(SoftwareIdCommand command){

    }

    public Page<TaskDto> listTask(TaskListCommand command, Pageable pageable){
        Page<Task> tasks = taskService.findPage(Filters.query().likeFuzzy(Task::getPids, command.getPid()).ge(Task::getStartTime, command.getStartTime()).le(Task::getStartTime, command.getEndTime()), pageable);
        return DtoTransformer.asPage(TaskDto.class).apply(tasks);
    }
}
