package com.idaoben.web.monitor.web.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.*;
import com.idaoben.web.monitor.service.ActionService;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.ActionFileListCommand;
import com.idaoben.web.monitor.web.command.ActionProcessListCommand;
import com.idaoben.web.monitor.web.command.ActionRegistryListCommand;
import com.idaoben.web.monitor.web.dto.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
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

    private Map<String, Map<String, NetworkInfo>> socketFdNetworkMap = new HashMap<>();

    private Map<String, Map<String, NetworkInfo>> refNetworkMap = new HashMap<>();

    private Map<String, Map<String, FileInfo>> fdFileMap = new HashMap<>();

    public Page<ActionFileDto> listByFileType(ActionFileListCommand command, Pageable pageable){
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.FILE).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getFileName, command.getFileName()).eq(Action::getSensitivity, command.getSensitivity())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        if(command.getOpType() != null){
            if(command.getOpType() == FileOpType.WRITE){
                filters.eq(Action::getType, ActionType.FILE_WRITE);
            } else {
                filters.eq(Action::getType, ActionType.FILE_OPEN);
            }
        }
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionFileDto.class).apply(actions, (domain, dto) -> {
            dto.setOpType(domain.getType() == ActionType.FILE_WRITE ? FileOpType.WRITE : FileOpType.WRITE);
        });
    }

    public Page<ActionRegistryDto> listByRegistryType(ActionRegistryListCommand command, Pageable pageable){
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.REGISTRY).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getKey, command.getKey()).likeFuzzy(Action::getValueName, command.getValueName()).eq(Action::getValueType, command.getValueType())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionRegistryDto.class).apply(actions);
    }

    public Page<ActionProcessDto> listByProcessType(ActionProcessListCommand command, Pageable pageable){
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.PROCESS).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getCommandLine, command.getCommandLine()).eq(Action::getType, command.getType())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionProcessDto.class).apply(actions);
    }

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
                    logger.info("开始从{}偏移值处理文件：PID:{}，{}", skip, pid, fileName);
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
                            if(action != null && action.getType() == ActionType.STOP){
                                thread.finish();
                            }
                        } else {
                            //停n秒再读取
                            Thread.sleep(2000);
                            logger.info("PID:{}，文件{}没有新内容，当前读取{}行, 待下次读取", pid, fileName, readLine);
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

    private void clearPidCache(String pid){
        handlingThreads.remove(pid);
        socketFdNetworkMap.remove(pid);
        refNetworkMap.remove(pid);
        fdFileMap.remove(pid);
    }

    /**
     * 处理单行的json内容
     * @param json
     * @param pid
     */
    private Action handleActionJson(String json, String pid, Long taskId, String actionName){
        try {
            ActionJson actionJson = objectMapper.readValue(json, ActionJson.class);
            Action action = actionJson.getAction();
            action.setUuid(actionJson.getUuid());
            action.setTimestamp(actionJson.getTimestamp());
            action.setWithAttachment(actionJson.getWithAttachment());
            action.setTaskId(taskId);
            action.setPid(pid);

            if(ActionType.isNetworkType(action.getType())){
                //如果是发起网络链接的，缓存网络信息
                setActionNetworkInfo(action, pid);
            } else if(ActionType.isFileType(action.getType())){
                //设置文件信息
                action = setActionFileInfo(action, pid);
            } else if(ActionType.isRegistryType(action.getType())){
                setActionRegistryInfo(action, pid);
            } else if(ActionType.isProcessType(action.getType())){
                setActionProcessInfo(action, pid);
            }

            if(action != null){
                return actionService.save(action);
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private static final String FILE_TYPE_SEPARATOR = "\\??\\";

    private Action setActionFileInfo(Action action, String pid){
        if(action.getType() == ActionType.FILE_OPEN){
            String path = action.getPath();
            //获取path对应的文件名称
            action.setFileName(StringUtils.substringAfterLast(path, SystemUtils.FILE_SEPARATOR));
            //根据操作系统判断系统敏感性
            if(path.startsWith(SystemUtils.getSensitivityPath())){
                action.setSensitivity(FileSensitivity.HIGH);
            }
            FileAccess fileAccess = getFileAccess(action);
            //只有含写操作的才会记录到map中
            if(fileAccess == FileAccess.WRITE || fileAccess == FileAccess.READ_AND_WRITE){
                Map<String, FileInfo> fdFileInfoMap = fdFileMap.computeIfAbsent(pid, p -> new HashMap<>());
                if(!fdFileInfoMap.containsKey(action.getFd())){
                    fdFileInfoMap.put(action.getFd(), new FileInfo(action.getFd(), action.getFileName(), action.getPath(), action.getSensitivity()));
                }
            }
            if(fileAccess == null){
                return null;
            }
        }
        if(action.getType() == ActionType.FILE_WRITE){
            Map<String, FileInfo> fdFileInfoMap = fdFileMap.get(pid);
            if(fdFileInfoMap != null){
                FileInfo fileInfo = fdFileInfoMap.get(action.getFd());
                if(fileInfo != null){
                    action.setFileName(fileInfo.getFileName());
                    action.setSensitivity(fileInfo.getSensitivity());
                    action.setPath(fileInfo.getPath());
                } else {
                    //找不到对应文件，不做记录
                    return null;
                }
            } else {
                return null;
            }
        }

        //设置正式的文件路径
        String path = action.getPath();
        if(path.startsWith(FILE_TYPE_SEPARATOR)){
            path = StringUtils.substringAfter(path, FILE_TYPE_SEPARATOR);
            action.setPath(path);
            action.setActionGroup(ActionGroup.FILE);
        } else {
            //这不是一个文件，是一个设备
            action.setActionGroup(ActionGroup.DEVICE);
        }
        return action;
    }

    private FileAccess getFileAccess(Action action){
        String accessStr = action.getAccess();
        if(accessStr != null){
            long access = Long.parseLong(accessStr);
            boolean read = access >> 31 == 1;
            boolean write = access << 1 >> 31 == 1;
            if(read && write){
                return FileAccess.READ_AND_WRITE;
            } else if(read){
                return FileAccess.READ;
            } else if(write){
                return FileAccess.WRITE;
            }
        }
        return null;
    }

    private void setActionNetworkInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.NETWORK);
        if(action.getType() == ActionType.NETWORK_OPEN){
            Map<String, NetworkInfo> socketFdNetworkInfoMap = socketFdNetworkMap.computeIfAbsent(pid, p -> new HashMap<>());
            if(!socketFdNetworkInfoMap.containsKey(action.getSocketFd())){
                socketFdNetworkInfoMap.put(action.getSocketFd(), new NetworkInfo(action.getHost(), action.getPort()));
            }

            Map<String, NetworkInfo> refNetworkInfoMap = socketFdNetworkMap.computeIfAbsent(pid, p -> new HashMap<>());
            if(!refNetworkInfoMap.containsKey(action.getRef())){
                refNetworkInfoMap.put(action.getRef(), new NetworkInfo(action.getHost(), action.getPort()));
            }
        }
        if(action.getType() == ActionType.NETWORK_TCP_SEND){
            Map<String, NetworkInfo> socketFdNetworkInfoMap = socketFdNetworkMap.get(pid);
            if(socketFdNetworkInfoMap != null){
                NetworkInfo networkInfo = socketFdNetworkInfoMap.get(action.getSocketFd());
                if(networkInfo != null){
                    action.setHost(networkInfo.getHost());
                    action.setPort(networkInfo.getPort());
                }
            } else {
                Map<String, NetworkInfo> refNetworkInfoMap = socketFdNetworkMap.get(pid);
                if(refNetworkInfoMap != null){
                    NetworkInfo networkInfo = refNetworkInfoMap.get(action.getRef());
                    if(networkInfo != null){
                        action.setHost(networkInfo.getHost());
                        action.setPort(networkInfo.getPort());
                    }
                }
            }
        }
    }

    private void setActionRegistryInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.REGISTRY);
    }

    private void setActionProcessInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.PROCESS);
    }
}
