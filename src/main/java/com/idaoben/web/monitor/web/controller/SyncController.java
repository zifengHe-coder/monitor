package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiRequest;
import com.idaoben.web.common.api.bean.ApiResponse;
import com.idaoben.web.monitor.web.application.SyncApplicationService;
import com.idaoben.web.monitor.web.command.TaskSyncCommand;
import com.idaoben.web.monitor.web.dto.TaskDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Api(tags="数据同步相关接口")
@RestController
@RequestMapping("api/sync")
public class SyncController {

    @Resource
    private SyncApplicationService syncApplicationService;

    @ApiOperation("任务同步")
    @PostMapping("/syncTask")
    public ApiResponse<List<TaskDto>> syncTask(@RequestBody @Validated ApiRequest<TaskSyncCommand> request){
        List<TaskDto> result = syncApplicationService.syncTask(request.getPayload());
        return ApiResponse.createSuccess(result);
    }
}
