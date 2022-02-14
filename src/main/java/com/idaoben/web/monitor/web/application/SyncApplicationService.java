package com.idaoben.web.monitor.web.application;

import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.Task;
import com.idaoben.web.monitor.service.ActionService;
import com.idaoben.web.monitor.service.TaskService;
import com.idaoben.web.monitor.web.command.ActionSyncCommand;
import com.idaoben.web.monitor.web.command.TaskSyncCommand;
import com.idaoben.web.monitor.web.dto.ActionSyncDto;
import com.idaoben.web.monitor.web.dto.TaskDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class SyncApplicationService {

    @Resource
    private TaskService taskService;

    @Resource
    private ActionService actionService;

    public List<TaskDto> syncTask(TaskSyncCommand command){
        List<Task> tasks = taskService.listSyncTask(command.getLastSyncTime());
        return DtoTransformer.asList(TaskDto.class).apply(tasks);
    }

    public List<ActionSyncDto> syncAction(ActionSyncCommand command){
        List<Action> actions = actionService.findList(Filters.query().eq(Action::getTaskId, command.getTaskId()).gt(Action::getTimestamp, command.getLastSyncTime()), null);
        return DtoTransformer.asList(ActionSyncDto.class).apply(actions);
    }
}
