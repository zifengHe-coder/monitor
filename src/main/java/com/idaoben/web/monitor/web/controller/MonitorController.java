package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiPageRequest;
import com.idaoben.web.common.api.bean.ApiPageResponse;
import com.idaoben.web.common.api.bean.ApiRequest;
import com.idaoben.web.common.api.bean.ApiResponse;
import com.idaoben.web.monitor.web.application.MonitorApplicationService;
import com.idaoben.web.monitor.web.command.IdCommand;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import com.idaoben.web.monitor.web.command.TaskListCommand;
import com.idaoben.web.monitor.web.dto.TaskDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Set;

@Api(tags="监控进程相关")
@RestController
@RequestMapping("api/monitor")
public class MonitorController {

    @Resource
    private MonitorApplicationService monitorApplicationService;

    @ApiOperation("开始监控软件")
    @PostMapping("/startMonitor")
    public ApiResponse<Void> startMonitor(@RequestBody @Validated ApiRequest<SoftwareIdCommand> request){
        monitorApplicationService.startMonitor(request.getPayload());
        return ApiResponse.createSuccess();
    }

    @ApiOperation("停止监控软件")
    @PostMapping("/stopMonitor")
    public ApiResponse<Void> stopMonitor(@RequestBody @Validated ApiRequest<SoftwareIdCommand> request){
        monitorApplicationService.stopMonitor(request.getPayload());
        return ApiResponse.createSuccess();
    }

    @ApiOperation("开启并监听软件")
    @PostMapping("/startAndMonitor")
    public ApiResponse<Void> startAndMonitor(@RequestBody @Validated ApiRequest<SoftwareIdCommand> request){
        monitorApplicationService.startAndMonitor(request.getPayload());
        return ApiResponse.createSuccess();
    }

    @ApiOperation("获取当前正在监听的软件ID")
    @GetMapping("/getMonitoringIds")
    public ApiResponse<Set<String>> getMonitoringIds(){
        Set<String> result = monitorApplicationService.getMonitoringSoftwareIds();
        return ApiResponse.createSuccess(result);
    }

    @ApiOperation("历史监控任务查询")
    @PostMapping("/listTask")
    public ApiPageResponse<TaskDto> listTask(@RequestBody @Validated ApiPageRequest<TaskListCommand> request) {
        Page<TaskDto> result = monitorApplicationService.listTask(request.getPayload(), request.getPageable(Sort.by(Sort.Direction.DESC, "startTime")));
        return ApiPageResponse.createPageSuccess(result);
    }

    @ApiOperation("删除任务")
    @PostMapping("/deleteTask")
    public ApiResponse<Void> deleteTask(@RequestBody @Validated ApiRequest<IdCommand> request){
        monitorApplicationService.deleteTask(request.getPayload());
        return ApiResponse.createSuccess();
    }
}
