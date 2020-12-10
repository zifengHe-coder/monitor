package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiRequest;
import com.idaoben.web.common.api.bean.ApiResponse;
import com.idaoben.web.monitor.dao.entity.enums.SystemOs;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.FilePathCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLConnection;
import java.net.URLEncoder;

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
    public ApiResponse<Void> openFileFolder(@RequestBody @Validated ApiRequest<FilePathCommand> request){
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

    @ApiOperation("文件下载")
    @GetMapping("downloadFile")
    public void download(String path, HttpServletResponse response) throws IOException {
        File file = new File(path);
        if(file.exists()){
            String name = file.getName();
            String type = URLConnection.getFileNameMap().getContentTypeFor(name);
            response.setContentType(type);
            response.setContentLength((int)file.length());
            if(StringUtils.isNotEmpty(name)){
                response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", URLEncoder.encode(name, "UTF-8")));
            }

            BufferedInputStream in = null;
            BufferedOutputStream out = null;
            try{
                in = new BufferedInputStream(new FileInputStream(file));
                out = new BufferedOutputStream(response.getOutputStream());
                IOUtils.copy(in, out);
                out.close();
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
