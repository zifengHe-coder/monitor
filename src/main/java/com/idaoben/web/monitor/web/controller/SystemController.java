package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiRequest;
import com.idaoben.web.common.api.bean.ApiResponse;
import com.idaoben.web.monitor.dao.entity.enums.SystemOs;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.FolderOpenCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@Api(tags="系统相关接口")
@RestController
@RequestMapping("api/system")
public class SystemController {

    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @ApiOperation("查询当前系统")
    @GetMapping("/getSystemOs")
    public ApiResponse<SystemOs> getSystemOs() {
        SystemOs result = SystemUtils.getSystemOs();
        return ApiResponse.createSuccess(result);
    }

    @ApiOperation("打开文件位置")
    @PostMapping("/openFileFolder")
    public ApiResponse<Void> openFileFolder(@RequestBody @Validated ApiRequest<FolderOpenCommand> request){
        File file = new File(request.getPayload().getPath());
        String cmd;
        if(file.exists()){
            cmd = "explorer /e,/select," + file.getPath();
        } else {
            cmd = "explorer /e,/root," + StringUtils.substringBeforeLast(request.getPayload().getPath(), SystemUtils.FILE_SEPARATOR);
        }
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return ApiResponse.createSuccess();
    }
}
