package com.idaoben.web.monitor.web.controller;

import com.idaoben.web.common.api.bean.ApiPageRequest;
import com.idaoben.web.common.api.bean.ApiPageResponse;
import com.idaoben.web.common.api.bean.ApiRequest;
import com.idaoben.web.common.api.bean.ApiResponse;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.utils.AESUtils;
import com.idaoben.web.monitor.web.application.MonitorApplicationService;
import com.idaoben.web.monitor.web.command.IdCommand;
import com.idaoben.web.monitor.web.command.SoftwareIdCommand;
import com.idaoben.web.monitor.web.command.TaskListCommand;
import com.idaoben.web.monitor.web.dto.TaskDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Set;

@Api(tags="监控进程相关")
@RestController
@RequestMapping("api/monitor")
public class MonitorController {

    @Resource
    private MonitorApplicationService monitorApplicationService;

    @Resource(name = "registerPath")
    private String registerPath;

    @Value("${encode-rules}")
    private String encodeRules;

    @ApiOperation("开始监控软件")
    @PostMapping("/startMonitor")
    public ApiResponse<Void> startMonitor(@RequestBody @Validated ApiRequest<SoftwareIdCommand> request){
        consumeRegisterFile(encodeRules, registerPath);
        monitorApplicationService.startMonitor(request.getPayload());
        return ApiResponse.createSuccess();
    }

    //读取注册文件，并更新注册文件使用次数
    private void consumeRegisterFile(String rules,String filePath) {
        File registerFile = new File(filePath);
        if (!registerFile.exists()) {
            throw new RuntimeException("注册码文件不存在，请重启系统!");
        }
        try {
            long fileSize = registerFile.length();
            if (fileSize > Integer.MAX_VALUE) {
                throw new RuntimeException("file too big");
            }
            FileInputStream file = new FileInputStream(registerFile);
            byte[] buffer= new byte[(int) fileSize];
            int offset = 0;
            int numRead = 0;
            while (offset < buffer.length && (numRead = file.read(buffer, offset, buffer.length - offset)) >= 0) {
                offset += numRead;
            }
            if (offset != buffer.length) {
                file.close();
                throw new RuntimeException("could not completely read file" + filePath);
            }
            //注册码格式{companyName};{cpuId};{mac};{count};{number}
            String registerCode = AESUtils.AESDecodeByBytes(rules, buffer);
            String strPrefix = registerCode.substring(0, registerCode.lastIndexOf(";") + 1);
            String[] split = registerCode.split(";");
            int count = Integer.parseInt(split[3]);
            int number = Integer.parseInt(split[4]);
            number++;
            if (count < number) {
                throw new RuntimeException("监控次数已用完，请重新申请激活码!");
            } else {
                strPrefix += number;
                byte[] aesEncode = AESUtils.AESEncode(rules, strPrefix);
                OutputStream fos = new FileOutputStream(registerFile);
                fos.write(aesEncode);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @ApiOperation("停止监控软件")
    @PostMapping("/stopMonitor")
    public ApiResponse<Void> stopMonitor(@RequestBody @Validated ApiRequest<SoftwareIdCommand> request){
        boolean isAllSuccess = monitorApplicationService.stopMonitor(request.getPayload());
        if(!isAllSuccess){
            throw ServiceException.of(ErrorCode.MONITOR_STOP_PARTIAL_ERROR);
        }
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
