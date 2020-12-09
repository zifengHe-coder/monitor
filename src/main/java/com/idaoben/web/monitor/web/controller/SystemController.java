package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiResponse;
import com.idaoben.web.monitor.dao.entity.enums.SystemOs;
import com.idaoben.web.monitor.utils.SystemUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags="系统相关接口")
@RestController
@RequestMapping("api/system")
public class SystemController {

    @ApiOperation("查询当前系统")
    @GetMapping("/getSystemOs")
    public ApiResponse<SystemOs> getSystemOs() {
        SystemOs result = SystemUtils.getSystemOs();
        return ApiResponse.createSuccess(result);
    }
}
