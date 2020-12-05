package com.idaoben.web.monitor.web.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.ActionType;
import com.idaoben.web.monitor.service.ActionService;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.dto.ActionJsonDto;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActionApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ActionApplicationService.class);

    private static final File ACTION_FOLDER = new File(SystemUtils.getOsHome() + "WinMonitor");

    private static final String ACTION_BACKUP_FOLDER_PATH = SystemUtils.getOsHome() + "WinMonitor" + SystemUtils.FILE_SEPARATOR + "data" + SystemUtils.FILE_SEPARATOR + "%s" + SystemUtils.FILE_SEPARATOR + "%s";

    @Resource
    private ActionService actionService;

    @Resource
    private ObjectMapper objectMapper;

    private Map<String, ActionHanlderThread> handlingThreads = new ConcurrentHashMap<>();

    private Map<String, Long> actionSkipMap = new ConcurrentHashMap<>();

    /**
     * 扫描进程的action文件
     */
    //@Scheduled(cron = "*/1 * * * * ?")
    public void scanAction() {
        if(!ACTION_FOLDER.exists()){
            return;
        }
        for(File pidFolder : ACTION_FOLDER.listFiles()){
            if(pidFolder.isDirectory()){
                String pid = pidFolder.getName();
                //开启线程处理
                //onGoingPids.add(pid);
                //actionTaskExecutor.execute(() -> handlePidAction(pidFolder));
            }
        }
    }

    public void startActionScan(String pid, Long taskId){
        ActionHanlderThread thread = new ActionHanlderThread(pid, taskId) {
            @Override
            void actionHandle(String pid, Long taskId) {
                handlePidAction(pid, taskId, this);
            }
        };
        thread.start();
        handlingThreads.put(pid, thread);
    }

    public void stopActionScan(String pid){
        ActionHanlderThread thread = handlingThreads.get(pid);
        if(thread != null){
            thread.finish();
        }
    }

    private void handlePidAction(String pid, Long taskId, ActionHanlderThread thread){
        File pidFolder = new File(ACTION_FOLDER, pid);
        if(!pidFolder.exists()){
            return;
        }
        //查询action文件
        for(File file : pidFolder.listFiles()){
            String fileName = file.getName();
            String pidFileName = String.format("%s_%s", pid, fileName);
            if(fileName.startsWith("actions_") && fileName.endsWith(".log")){
                int readLine = 0;
                try {
                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                    long skip = actionSkipMap.getOrDefault(pidFileName, 0l);
                    logger.info("开始从{}偏移值处理文件：{}", skip, fileName);
                    if(skip > 0){
                        randomAccessFile.seek(skip);
                    }
                    String line = randomAccessFile.readLine();
                    //判断当前文件是否读写完成，并且监听是否完成。 读写完成且监听也结束才结束
                    while (line != null || !thread.isFinish()){
                        if(line != null){
                            //处理当前行的json内容
                            //logger.info("正在处理第{}行记录", readLine);
                            Action action = handleActionJson(line, pid, taskId, fileName);
                            //更新已处理文件指针
                            skip = randomAccessFile.getFilePointer();
                            actionSkipMap.put(pidFileName, skip);
                            readLine ++;

                            //如果发现action已结束，主动完成线程
                            if(action != null && Objects.equals(action.getType(), ActionType.STOP.value())){
                                thread.finish();
                            }
                        } else {
                            //停n秒再读取
                            Thread.sleep(2000);
                            logger.info("文件{}没有新内容，当前读取{}行, 待下次读取", fileName, readLine);
                        }
                        line = randomAccessFile.readLine();
                    }
                    //设置已完成文件处理
                    randomAccessFile.close();
                    logger.info("已完成{}文件本次的处理,处理到第{}行", fileName, readLine - 1);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        //将文件夹备份移除
        try {
            File destFolder = new File(String.format(ACTION_BACKUP_FOLDER_PATH, taskId, pid));
            logger.info("开始备份到文件夹{}", destFolder.getPath());
            FileUtils.moveDirectory(pidFolder, destFolder);
            logger.info("结束备份到文件夹{}", destFolder.getPath());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 处理单行的json内容
     * @param json
     * @param pid
     */
    private Action handleActionJson(String json, String pid, Long taskId, String actionName){
        try {
            ActionJsonDto actionJson = objectMapper.readValue(json, ActionJsonDto.class);
            Action action = actionJson.getAction();
            action.setUuid(actionJson.getUuid());
            action.setTimestamp(actionJson.getTimestamp());
            action.setWithAttachment(actionJson.getWithAttachment());
            action.setTaskId(taskId);
            action.setPid(pid);
            return actionService.save(action);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
