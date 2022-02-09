package com.idaoben.web.monitor.web.application;

import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Task;
import com.idaoben.web.monitor.service.TaskService;
import com.idaoben.web.monitor.web.command.TaskSyncCommand;
import com.idaoben.web.monitor.web.dto.TaskDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SyncApplicationService {

    @Resource
    private TaskService taskService;

    public List<TaskDto> syncTask(TaskSyncCommand command){
        List<Task> tasks = taskService.listSyncTask(command.getLastSyncTime());
        return DtoTransformer.asList(TaskDto.class).apply(tasks);
    }
}
