package com.idaoben.web.monitor.web.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.*;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.ActionService;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.*;
import com.idaoben.web.monitor.web.dto.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ActionApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ActionApplicationService.class);

    private File ACTION_FOLDER;

    private String ACTION_BACKUP_FOLDER_PATH;

    @Resource
    private ActionService actionService;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private SystemOsService systemOsService;

    private Map<String, ActionHanlderThread> handlingThreads = new ConcurrentHashMap<>();

    private Map<String, Long> actionSkipMap = new ConcurrentHashMap<>();

    private Map<String, Map<Integer, NetworkInfo>> socketFdNetworkMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, FileInfo>> fdFileMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, Action>> writeFileMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        ACTION_FOLDER = new File(systemOsService.getActionFolderPath());
        ACTION_BACKUP_FOLDER_PATH = systemOsService.getActionFolderPath() + SystemUtils.FILE_SEPARATOR + "data" + SystemUtils.FILE_SEPARATOR + "%s" + SystemUtils.FILE_SEPARATOR + "%s";
    }

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
            dto.setOpType(domain.getType() == ActionType.FILE_WRITE ? FileOpType.WRITE : FileOpType.READ);
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
                .likeFuzzy(Action::getCmdLine, command.getCmdLine()).eq(Action::getType, command.getType())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionProcessDto.class).apply(actions);
    }

    public Page<ActionNetworkDto> listByNetworkType(ActionNetworkListCommand command, Pageable pageable){
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.NETWORK).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getHost, command.getHost()).eq(Action::getPort, command.getPort()).eq(Action::getType, command.getType())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionNetworkDto.class).apply(actions, (domain, dto) -> {
            //简单的协议分析
            if(domain.getType() == ActionType.NETWORK_TCP_SEND || domain.getType() == ActionType.NETWORK_TCP_RECEIVE){
                int port = domain.getPort() == null ? -1 : domain.getPort();
                if(port == 443){
                    dto.setProtocol("HTTPS");
                } else if(port == 80){
                    dto.setProtocol("HTTP");
                } else {
                    dto.setProtocol("TCP");
                }
            } else if(domain.getType() == ActionType.NETWORK_UDP_SEND || domain.getType() == ActionType.NETWORK_UDP_RECEIVE){
                dto.setProtocol("UDP");
            }
        });
    }

    public Page<ActionDeviceDto> listByDeviceType(ActionDeviceListCommand command, Pageable pageable){
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.DEVICE).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getCmdLine, command.getDeviceName())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionDeviceDto.class).apply(actions);
    }

    public File getNetworkFile(String uuid){
        Action action = actionService.findStrictly(uuid);
        if(action.getActionGroup() != ActionGroup.NETWORK || action.getType() == ActionType.NETWORK_OPEN){
            throw ServiceException.of(ErrorCode.CODE_REQUESE_PARAM_ERROR);
        }
        File folder = new File(String.format(ACTION_BACKUP_FOLDER_PATH, action.getTaskId(), action.getPid()));
        if(!folder.exists()){
            folder = new File(ACTION_FOLDER, action.getPid());
        }
        String fileName = String.valueOf(action.getSocketFd());
        if(action.getType() == ActionType.NETWORK_TCP_RECEIVE || action.getType() == ActionType.NETWORK_UDP_RECEIVE){
            fileName = fileName + "_recv";
        }
        File file = new File(folder, fileName);
        return file;
    }

    public File getWriteFile(String uuid) throws IOException{
        Action action = actionService.findStrictly(uuid);
        if(action.getActionGroup() != ActionGroup.FILE || action.getType() != ActionType.FILE_WRITE){
            throw ServiceException.of(ErrorCode.CODE_REQUESE_PARAM_ERROR);
        }

        File folder = new File(String.format(ACTION_BACKUP_FOLDER_PATH, action.getTaskId(), action.getPid()));
        if(!folder.exists()){
            folder = new File(ACTION_FOLDER, action.getPid());
        }
        File offsetFile = new File(folder, action.getFd());
        File zipFile = new File(folder, uuid + ".zip");
        //如果zip文件已存在，表示曾经生成过，直接下载即可
        if(zipFile.exists()){
            return zipFile;
        }

        if(StringUtils.isEmpty(action.getBackup())){
            //没有备份表示是新文件，直接下载即可
            compressZipFile(null, offsetFile, action.getFileName(), zipFile);
        }

        File backupFile = new File(folder, StringUtils.substringAfterLast(action.getBackup(), SystemUtils.FILE_SEPARATOR));
        if(!backupFile.exists()){
            throw ServiceException.of(ErrorCode.BACKUP_FILE_NOT_FOUND);
        }
        File newFile = new File(folder, uuid);
        FileUtils.copyFile(backupFile, newFile);
        RandomAccessFile randomAccessFile = new RandomAccessFile(newFile, "rw");
        FileInputStream offsetInputStream = new FileInputStream(offsetFile);
        //对write file进行偏移量计算并重新写入文件
        if(StringUtils.isNotEmpty(action.getWriteOffsets()) && StringUtils.isNotEmpty(action.getWriteBytes())){
            String[] offsets = action.getWriteOffsets().split(",");
            String[] bytes = action.getWriteBytes().split(",");
            for(int i = 0, size = bytes.length; i < size; i++){
                long offset = StringUtils.isNotEmpty(offsets[i]) ? Long.parseLong(offsets[i]) : -1;
                int writeByte = Integer.parseInt(bytes[i]);
                byte[] content = new byte[writeByte];
                offsetInputStream.read(content);
                if(offset != -1){
                    randomAccessFile.seek(offset);
                } else {
                    randomAccessFile.seek(randomAccessFile.length());
                }
                randomAccessFile.write(content);
            }
        }
        IOUtils.closeQuietly(offsetInputStream);

        compressZipFile(backupFile, newFile, action.getFileName(), zipFile);
        return zipFile;
    }

    private void compressZipFile(File backupFile, File writeFile, String fileName, File zipFile) throws IOException{
        if(!zipFile.exists()) {
            zipFile.createNewFile();
        }
        FileInputStream writeFileIs = new FileInputStream(writeFile);
        FileOutputStream zipFileOs = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(zipFileOs);
        if(backupFile != null){
            FileInputStream backupFileIs = new FileInputStream(backupFile);
            zos.putNextEntry(new ZipEntry("backup_" + fileName));
            IOUtils.copy(backupFileIs, zos);
            zos.closeEntry();
            IOUtils.closeQuietly(backupFileIs);
        }

        zos.putNextEntry(new ZipEntry("new_" + fileName));
        IOUtils.copy(writeFileIs, zos);
        zos.closeEntry();

        IOUtils.closeQuietly(writeFileIs);
        IOUtils.closeQuietly(zipFileOs);
        IOUtils.closeQuietly(zos);
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

    void handlePidAction(String pid, Long taskId, ActionHanlderThread thread){
        File pidFolder = new File(ACTION_FOLDER, pid);
        if(!pidFolder.exists()){
            logger.error("进程action文件夹未生成， 尝试等待后重试，PID: {}", pid);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            handlePidAction(pid, taskId, thread);
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

        clearPidCache(pid);
    }

    private void clearPidCache(String pid){
        handlingThreads.remove(pid);
        socketFdNetworkMap.remove(pid);
        fdFileMap.remove(pid);
        writeFileMap.remove(pid);
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
                systemOsService.setActionProcessInfo(action, pid);
            }

            if(action != null){
                return actionService.save(action);
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    private Action setActionFileInfo(Action action, String pid){
        if(action.getType() == ActionType.FILE_OPEN){
            //获取path对应的文件名称
            action.setFileName(StringUtils.substringAfterLast(action.getPath(), SystemUtils.FILE_SEPARATOR));

            //设置正式的文件路径和action group，并且判断是否设备
            ActionGroup actionGroup = systemOsService.setActionFromFileInfo(action);
            if(actionGroup == null){
                return null;
            }
            action.setActionGroup(actionGroup);

            //根据操作系统判断系统敏感性
            if(actionGroup == ActionGroup.FILE){
                action.setSensitivity(systemOsService.getFileSensitivity(action.getPath()));
            }

            FileAccess fileAccess = systemOsService.getFileAccess(action.getAccess());
            //只有含写操作的才会记录到map中
            if(fileAccess == FileAccess.WRITE || fileAccess == FileAccess.READ_AND_WRITE){
                Map<String, FileInfo> fdFileInfoMap = fdFileMap.computeIfAbsent(pid, p -> new HashMap<>());
                if(!fdFileInfoMap.containsKey(action.getFd())){
                    fdFileInfoMap.put(action.getFd(), new FileInfo(action.getFd(), action.getFileName(), action.getPath(), action.getBackup(), action.getSensitivity(), action.getDeviceName(), action.getActionGroup()));
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
                    //查下是否有对上一个相同FD写入记录，有记录的话只做更新操作
                    Map<String, Action> actionMap = writeFileMap.computeIfAbsent(pid, p -> new HashMap<>());
                    Action originAction = actionMap.get(action.getFd());
                    if(originAction == null){
                        originAction = action;
                        originAction.setFileName(fileInfo.getFileName());
                        originAction.setSensitivity(fileInfo.getSensitivity());
                        originAction.setPath(fileInfo.getPath());
                        originAction.setBackup(fileInfo.getBackup());
                        originAction.setDeviceName(fileInfo.getDeviceName());
                        originAction.setActionGroup(fileInfo.getActionGroup());
                        originAction.setWriteOffsets(action.getOffset() == null ? "" : String.valueOf(action.getOffset()));
                        originAction.setWriteBytes(String.valueOf(action.getBytes()));
                        actionMap.put(originAction.getFd(), originAction);
                    } else {
                        originAction.setTimestamp(action.getTimestamp());
                        //空的时候是写到最后，写到最后时仅仅直接修改已写入量，不再追加记录
                        if(action.getOffset() == null){
                            //取最后写入量，并且进行写入量重新计算填充
                            String writeOffsets = action.getWriteOffsets() == null ? "" :  action.getWriteOffsets();
                            int lastIndex = writeOffsets.lastIndexOf(',');
                            String preOffset = null;
                            String currentOffset;
                            if(lastIndex >= 0){
                                preOffset = writeOffsets.substring(0, lastIndex + 1);
                                currentOffset = writeOffsets.substring(lastIndex + 1);
                            } else {
                                currentOffset = writeOffsets;
                            }
                            //currentOffset为空表示原来的都是写入到最后的，不用做更新了
                            if(currentOffset.length() > 0) {
                                long offset = Long.parseLong(currentOffset);
                                offset += action.getOffset();
                                if(preOffset == null){
                                    action.setWriteOffsets(String.valueOf(offset));
                                } else {
                                    action.setWriteOffsets(String.format("%s,%d", preOffset, action.getOffset()));
                                }
                            }
                        } else {
                            originAction.setWriteOffsets(String.format("%s,%d", originAction.getWriteOffsets(), action.getOffset()));
                            originAction.setWriteBytes(String.format("%s,%d", originAction.getWriteBytes(), action.getBytes()));
                        }
                    }
                    action = originAction;
                } else {
                    //找不到对应文件，不做记录
                    return null;
                }
            } else {
                return null;
            }
        }
        return action;
    }

    private void setActionNetworkInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.NETWORK);
        //Windows平台所有发送和接收都带上了host和port，不用处理。只有Linux下需要处理
        if(SystemUtils.getSystemOs() == SystemOs.LINUX){
            if(action.getType() == ActionType.NETWORK_OPEN){
                if(StringUtils.isNotEmpty(action.getHost())){
                    Map<Integer, NetworkInfo> socketFdNetworkInfoMap = socketFdNetworkMap.computeIfAbsent(pid, p -> new HashMap<>());
                    socketFdNetworkInfoMap.put(action.getSocketFd(), new NetworkInfo(action.getHost(), action.getPort()));
                }
            }
            if(action.getType() == ActionType.NETWORK_TCP_SEND || action.getType() == ActionType.NETWORK_TCP_RECEIVE){
                Map<Integer, NetworkInfo> socketFdNetworkInfoMap = socketFdNetworkMap.get(pid);
                if(socketFdNetworkInfoMap != null){
                    NetworkInfo networkInfo = socketFdNetworkInfoMap.get(action.getSocketFd());
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
}
