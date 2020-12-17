package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiRequest;
import com.idaoben.web.common.api.bean.ApiResponse;
import com.idaoben.web.monitor.web.application.SoftwareApplicationService;
import com.idaoben.web.monitor.web.command.FileListCommand;
import com.idaoben.web.monitor.web.command.SoftwareAddCommand;
import com.idaoben.web.monitor.web.command.SoftwareCmdAddCommand;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import com.idaoben.web.monitor.web.dto.FileDto;
import com.idaoben.web.monitor.web.dto.SoftwareDetailDto;
import com.idaoben.web.monitor.web.dto.SoftwareDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Api(tags="软件进程相关")
@RestController
@RequestMapping("api/software")
public class SoftwareController {

    @Resource
    private SoftwareApplicationService softwareApplicationService;

    @ApiOperation("查询系统软件列表")
    @GetMapping("/getSystemSoftware")
    public ApiResponse<List<SoftwareDto>> getSystemSoftware() {
        List<SoftwareDto> result = softwareApplicationService.getSystemSoftware();
        return ApiResponse.createSuccess(result);
    }

    @ApiOperation("软件详情")
    @PostMapping("/detailSoftware")
    public ApiResponse<SoftwareDetailDto> detailSoftware(@RequestBody @Validated ApiRequest<SoftwareIdCommand> request){
        SoftwareDetailDto result = softwareApplicationService.detailSoftware(request.getPayload());
        return ApiResponse.createSuccess(result);
    }

    @ApiOperation("添加软件")
    @PostMapping("/addSoftware")
    public ApiResponse<Void> addSoftware(@RequestBody @Validated ApiRequest<SoftwareAddCommand> request){
        softwareApplicationService.addSoftware(request.getPayload());
        return ApiResponse.createSuccess();
    }

    @ApiOperation("通过命令行添加软件")
    @PostMapping("/addCmdSoftware")
    public ApiResponse<Void> addCmdSoftware(@RequestBody @Validated ApiRequest<SoftwareCmdAddCommand> request){
        softwareApplicationService.addCmdSoftware(request.getPayload());
        return ApiResponse.createSuccess();
    }

    @ApiOperation("添加收藏")
    @PostMapping("/addFavorite")
    public ApiResponse<Void> addFavorite(@RequestBody @Validated ApiRequest<SoftwareIdCommand> request){
        softwareApplicationService.addFavorite(request.getPayload());
        return ApiResponse.createSuccess();
    }

    @ApiOperation("移出收藏")
    @PostMapping("/removeFavorite")
    public ApiResponse<Void> removeFavorite(@RequestBody @Validated ApiRequest<SoftwareIdCommand> request){
        softwareApplicationService.removeFavorite(request.getPayload());
        return ApiResponse.createSuccess();
    }

    @ApiOperation("查询系统文件目录")
    @PostMapping("/listFiles")
    public ApiResponse<List<FileDto>> listFiles(@RequestBody @Validated ApiRequest<FileListCommand> request) {
        List<FileDto> result = softwareApplicationService.listFiles(request.getPayload());
        return ApiResponse.createSuccess(result);
    }

}
