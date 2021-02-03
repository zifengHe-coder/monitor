package com.idaoben.web.monitor.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.common.api.bean.ApiPageRequest;
import com.idaoben.web.common.api.bean.ApiPageResponse;
import com.idaoben.web.common.excel.ExcelTool;
import com.idaoben.web.monitor.utils.DownloadUtils;
import com.idaoben.web.monitor.web.application.ActionApplicationService;
import com.idaoben.web.monitor.web.command.*;
import com.idaoben.web.monitor.web.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@Api(tags="软件行为相关")
@RestController
@RequestMapping("api/action")
public class ActionController {

    @Resource
    private ActionApplicationService actionApplicationService;

    @Resource
    private ObjectMapper objectMapper;

    @ApiOperation("查询文件读写行为")
    @PostMapping("/listByFileType")
    public ApiPageResponse<ActionFileDto> listByFileType(@RequestBody @Validated ApiPageRequest<ActionFileListCommand> request) {
        Page<ActionFileDto> result = actionApplicationService.listByFileType(request.getPayload(), request.getPageable(Sort.by(Sort.Direction.DESC, "timestamp")));
        return ApiPageResponse.createPageSuccess(result);
    }

    @ApiOperation("导出文件读写行为")
    @GetMapping("/exportByFileType")
    public void exportByFileType(@ApiParam("与查询一样的参数json格式") @RequestParam String json, HttpServletResponse response) throws Exception {
        ActionFileListCommand command = objectMapper.readValue(json, ActionFileListCommand.class);
        Workbook workbook = actionApplicationService.exportByFileType(command);
        String fileName = "文件读写行为" + System.currentTimeMillis() + ".xlsx";
        ExcelTool.exportWorkbook(workbook, fileName, response);
    }

    @ApiOperation("查询注册表行为")
    @PostMapping("/listByRegistryType")
    public ApiPageResponse<ActionRegistryDto> listByRegistryType(@RequestBody @Validated ApiPageRequest<ActionRegistryListCommand> request) {
        Page<ActionRegistryDto> result = actionApplicationService.listByRegistryType(request.getPayload(), request.getPageable(Sort.by(Sort.Direction.DESC, "timestamp")));
        return ApiPageResponse.createPageSuccess(result);
    }

    @ApiOperation("导出注册表行为")
    @GetMapping("/exportByRegistryType")
    public void exportByRegistryType(@ApiParam("与查询一样的参数json格式") @RequestParam String json, HttpServletResponse response) throws Exception {
        ActionRegistryListCommand command = objectMapper.readValue(json, ActionRegistryListCommand.class);
        Workbook workbook = actionApplicationService.exportByRegistryType(command);
        String fileName = "注册表行为" + System.currentTimeMillis() + ".xlsx";
        ExcelTool.exportWorkbook(workbook, fileName, response);
    }

    @ApiOperation("查询进程调用行为")
    @PostMapping("/listByProcessType")
    public ApiPageResponse<ActionProcessDto> listByProcessType(@RequestBody @Validated ApiPageRequest<ActionProcessListCommand> request) {
        Page<ActionProcessDto> result = actionApplicationService.listByProcessType(request.getPayload(), request.getPageable(Sort.by(Sort.Direction.DESC, "timestamp")));
        return ApiPageResponse.createPageSuccess(result);
    }

    @ApiOperation("导出进程调用行为")
    @GetMapping("/exportByProcessType")
    public void exportByProcessType(@ApiParam("与查询一样的参数json格式") @RequestParam String json, HttpServletResponse response) throws Exception {
        ActionProcessListCommand command = objectMapper.readValue(json, ActionProcessListCommand.class);
        Workbook workbook = actionApplicationService.exportByProcessType(command);
        String fileName = "进程调用行为" + System.currentTimeMillis() + ".xlsx";
        ExcelTool.exportWorkbook(workbook, fileName, response);
    }

    @ApiOperation("查询网络访问行为")
    @PostMapping("/listByNetworkType")
    public ApiPageResponse<ActionNetworkDto> listByNetworkType(@RequestBody @Validated ApiPageRequest<ActionNetworkListCommand> request) {
        Page<ActionNetworkDto> result = actionApplicationService.listByNetworkType(request.getPayload(), request.getPageable(Sort.by(Sort.Direction.DESC, "timestamp")));
        return ApiPageResponse.createPageSuccess(result);
    }

    @ApiOperation("查询设备访问行为")
    @PostMapping("/listByDeviceType")
    public ApiPageResponse<ActionDeviceDto> listByDeviceType(@RequestBody @Validated ApiPageRequest<ActionDeviceListCommand> request) {
        Page<ActionDeviceDto> result = actionApplicationService.listByDeviceType(request.getPayload(), request.getPageable(Sort.by(Sort.Direction.DESC, "timestamp")));
        return ApiPageResponse.createPageSuccess(result);
    }

    @ApiOperation("导出设备访问行为")
    @GetMapping("/exportByDeviceType")
    public void exportByDeviceType(@ApiParam("与查询一样的参数json格式") @RequestParam String json, HttpServletResponse response) throws Exception {
        ActionDeviceListCommand command = objectMapper.readValue(json, ActionDeviceListCommand.class);
        Workbook workbook = actionApplicationService.exportByDeviceType(command);
        String fileName = "设备访问行为" + System.currentTimeMillis() + ".xlsx";
        ExcelTool.exportWorkbook(workbook, fileName, response);
    }

    @ApiOperation("查询对象权限行为")
    @PostMapping("/listBySecurityType")
    public ApiPageResponse<ActionSecurityDto> listBySecurityType(@RequestBody @Validated ApiPageRequest<ActionSecurityListCommand> request) {
        Page<ActionSecurityDto> result = actionApplicationService.listBySecurityType(request.getPayload(), request.getPageable(Sort.by(Sort.Direction.DESC, "timestamp")));
        return ApiPageResponse.createPageSuccess(result);
    }

    @ApiOperation("网络包下载")
    @GetMapping("downloadNetworkPackage")
    public void downloadNetworkPackage(String uuid, HttpServletResponse response) throws IOException {
        try {
            Pair<File, String> file = actionApplicationService.getNetworkFile(uuid);
            DownloadUtils.sendFileToClient(file.getLeft(), file.getRight(), response);
        } catch (Exception e){
            DownloadUtils.sendErrorFileToClient(e.getMessage(), response);
        }
    }

    @ApiOperation("写对比文件下载")
    @GetMapping("downloadWriteFilePackage")
    public void downloadWriteFilePackage(String uuid, HttpServletResponse response) throws IOException {
        try {
            File file = actionApplicationService.getWriteFile(uuid);
            DownloadUtils.sendFileToClient(file, response);
        } catch (Exception e){
            DownloadUtils.sendErrorFileToClient(e.getMessage(), response);
        }
    }

    @ApiOperation("删除文件的备份下载")
    @GetMapping("downloadDeleteFile")
    public void downloadDeleteFile(String uuid, HttpServletResponse response) throws IOException {
        try {
            Pair<File, String> result = actionApplicationService.downloadDeleteFile(uuid, response);
            DownloadUtils.sendFileToClient(result.getLeft(), result.getRight(), response);
        } catch (Exception e){
            DownloadUtils.sendErrorFileToClient(e.getMessage(), response);
        }
    }
}
