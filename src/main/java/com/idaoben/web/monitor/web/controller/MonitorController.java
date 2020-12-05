package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiRequest;
import com.idaoben.web.common.api.bean.ApiResponse;
import com.idaoben.web.monitor.web.application.MonitorApplicationService;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
