package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiPageRequest;
import com.idaoben.web.common.api.bean.ApiPageResponse;
import com.idaoben.web.monitor.web.application.ActionApplicationService;
import com.idaoben.web.monitor.web.command.ActionFileListCommand;
import com.idaoben.web.monitor.web.dto.ActionFileDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags="软件行为相关")
@RestController
@RequestMapping("api/action")
public class ActionController {

    @Resource
    private ActionApplicationService actionApplicationService;

    @ApiOperation("查询文件读写")
    @PostMapping("/listByFileType")
    public ApiPageResponse<ActionFileDto> listByFileType(@RequestBody @Validated ApiPageRequest<ActionFileListCommand> request) {
        Page<ActionFileDto> result = actionApplicationService.listByFileType(request.getPayload(), request.getPageable(Sort.by(Sort.Direction.DESC, "timestamp")));
        return ApiPageResponse.createPageSuccess(result);
    }
}
