package com.idaoben.web.monitor.web.application;

import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Task;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.TaskService;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import com.idaoben.web.monitor.web.command.TaskListCommand;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import com.idaoben.web.monitor.web.dto.TaskDto;
import net.sf.ehcache.util.concurrent.ConcurrentHashMap;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.ZonedDateTime;
import java.util.*;

@Component
public class MonitorApplicationService {

    private Set<String> monitoringSoftwares = Collections.synchronizedSet(new HashSet<>());

    private Map<String, List<String>> processMaps = new ConcurrentHashMap<>();

    @Resource
    private SoftwareApplicationService softwareApplicationService;

    @Resource
    private ActionApplicationService actionApplicationService;

    @Resource
    private TaskService taskService;

    @PostConstruct
    public void initTestData(){
        //TODO: 初始化测试数据，正式情况下去掉
        List<String> pids = Arrays.asList("2556", "10160");
        processMaps.put("123456", pids);
    }


    public void startMonitor(SoftwareIdCommand command){
        String softwareId = command.getId();
        if(monitoringSoftwares.contains(softwareId)){
            throw ServiceException.of(ErrorCode.MONITOR_ON_GOING);
        }
        //启动监听
        if(!processMaps.containsKey(softwareId)){
            throw ServiceException.of(ErrorCode.SOFTWARE_NOT_RUNING);
        }

        //查询当前软件是否还有未完全的监听任务，把任务置为完成
        List<Task> tasks = taskService.findList(Filters.query().eq(Task::getSoftwareId, softwareId).eq(Task::isComplete, false), null);
        for(Task task : tasks){
            task.setEndTime(ZonedDateTime.now());
            task.setComplete(true);
            taskService.save(task);
        }

        int result = 0;
        if(result == 0){
            monitoringSoftwares.add(softwareId);

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
            List<String> pids = processMaps.get(softwareId);
            if(pids != null){
                task.setPids(StringUtils.join(pids, ','));
                task = taskService.save(task);
                for(String pid : pids){
                    actionApplicationService.startActionScan(pid, task.getId());
                }
            }
        }
    }

    public void stopMonitor(SoftwareIdCommand command){
        String softwareId = command.getId();
        List<String> pids = processMaps.get(softwareId);
        for(String pid : pids){
            //TODO:停止软件对应的pid线程
            actionApplicationService.stopActionScan(pid);
        }

        List<Task> tasks = taskService.findList(Filters.query().eq(Task::getSoftwareId, softwareId).eq(Task::isComplete, false), null);
        for(Task task : tasks){
            task.setEndTime(ZonedDateTime.now());
            task.setComplete(true);
            taskService.save(task);
        }
        monitoringSoftwares.remove(command.getId());

    }

    public void startAndMonitor(SoftwareIdCommand command){

    }

    public Page<TaskDto> listTask(TaskListCommand command, Pageable pageable){
        Page<Task> tasks = taskService.findPage(Filters.query().likeFuzzy(Task::getPids, command.getPid()).ge(Task::getStartTime, command.getStartTime()).le(Task::getStartTime, command.getEndTime()), pageable);
        return DtoTransformer.asPage(TaskDto.class).apply(tasks);
    }
}
