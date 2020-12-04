package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiRequest;
import com.idaoben.web.common.api.bean.ApiResponse;
import com.idaoben.web.monitor.web.application.SoftwareApplicationService;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
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

}
