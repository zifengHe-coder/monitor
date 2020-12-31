package com.idaoben.web.monitor.web.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.ActionGroup;
import com.idaoben.web.monitor.dao.entity.enums.ActionType;
import com.idaoben.web.monitor.dao.entity.enums.FileAccess;
import com.idaoben.web.monitor.dao.entity.enums.FileOpType;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.ActionService;
import com.idaoben.web.monitor.service.MonitoringService;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.service.TaskService;
import com.idaoben.web.monitor.utils.DownloadUtils;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.*;
import com.idaoben.web.monitor.web.dto.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Resource
    private MonitoringService monitoringService;

    @Resource
    private TaskService taskService;

    private Map<String, ActionHanlderThread> handlingThreads = new ConcurrentHashMap<>();

    private Map<String, Long> actionSkipMap = new ConcurrentHashMap<>();

    private Map<String, Map<Integer, NetworkInfo>> socketFdNetworkMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, FileInfo>> fdFileMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, Action>> writeFileMap = new ConcurrentHashMap<>();

    private Map<String, Map<String, Long>> fdFileSeekMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init(){
        ACTION_FOLDER = new File(systemOsService.getActionFolderPath());
        ACTION_BACKUP_FOLDER_PATH = systemOsService.getActionFolderPath() + SystemUtils.FILE_SEPARATOR + "data" + SystemUtils.FILE_SEPARATOR + "%s" + SystemUtils.FILE_SEPARATOR + "%s";
    }

    public Page<ActionFileDto> listByFileType(ActionFileListCommand command, Pageable pageable){
        Map<String, String> pidUsers = command.getTaskId() != null ? taskService.findStrictly(command.getTaskId()).getPidUsers() : null;
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.FILE).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getFileName, command.getFileName()).eq(Action::getSensitivity, command.getSensitivity())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        if(command.getOpType() != null){
            if(command.getOpType() == FileOpType.WRITE){
                filters.eq(Action::getType, ActionType.FILE_WRITE);
            } else if(command.getOpType() == FileOpType.READ){
                filters.eq(Action::getType, ActionType.FILE_OPEN);
            } else if(command.getOpType() == FileOpType.DELETE){
                filters.in(Action::getType, ActionType.FILE_DELETE_LINUX, ActionType.FILE_DELETE_WINDOWS);
            }
        }
        addUserFilter(filters, pidUsers, command.getUser());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionFileDto.class).apply(actions, (domain, dto) -> {
            switch (domain.getType()){
                case ActionType.FILE_WRITE:
                    dto.setOpType(FileOpType.WRITE);
                    break;
                case ActionType.FILE_OPEN:
                    dto.setOpType(FileOpType.READ);
                    break;
                case ActionType.FILE_DELETE_LINUX:
                case ActionType.FILE_DELETE_WINDOWS:
                    dto.setOpType(FileOpType.DELETE);
                    break;
            }
            setActionUser(dto, pidUsers);
        });
    }

    public Page<ActionRegistryDto> listByRegistryType(ActionRegistryListCommand command, Pageable pageable){
        Map<String, String> pidUsers = command.getTaskId() != null ? taskService.findStrictly(command.getTaskId()).getPidUsers() : null;
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.REGISTRY).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .eq(Action::getType, command.getType()).likeFuzzy(Action::getKey, command.getKey()).likeFuzzy(Action::getValueName, command.getValueName()).eq(Action::getValueType, command.getValueType())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        addUserFilter(filters, pidUsers, command.getUser());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionRegistryDto.class).apply(actions, (domain, dto) -> {
            setActionUser(dto, pidUsers);
        });
    }

    public Page<ActionProcessDto> listByProcessType(ActionProcessListCommand command, Pageable pageable){
        Map<String, String> pidUsers = command.getTaskId() != null ? taskService.findStrictly(command.getTaskId()).getPidUsers() : null;
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.PROCESS).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getCmdLine, command.getCmdLine()).eq(Action::getType, command.getType())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        addUserFilter(filters, pidUsers, command.getUser());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionProcessDto.class).apply(actions, (domain, dto) -> {
            setActionUser(dto, pidUsers);
        });
    }

    public Page<ActionNetworkDto> listByNetworkType(ActionNetworkListCommand command, Pageable pageable){
        Map<String, String> pidUsers = command.getTaskId() != null ? taskService.findStrictly(command.getTaskId()).getPidUsers() : null;
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.NETWORK).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getHost, command.getHost()).eq(Action::getPort, command.getPort()).eq(Action::getType, command.getType())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        addUserFilter(filters, pidUsers, command.getUser());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionNetworkDto.class).apply(actions, (domain, dto) -> {
            //简单的协议分析
            if(domain.getType() == ActionType.NETWORK_TCP_SEND || domain.getType() == ActionType.NETWORK_TCP_RECEIVE){
                int port = domain.getPort() == null ? -1 : domain.getPort();
                if(port == 443){
                    dto.setProtocol("HTTPS");
                } else if(port == 80) {
                    dto.setProtocol("HTTP");
                } else if(port == 53){
                    dto.setProtocol("DNS");
                } else {
                    dto.setProtocol("TCP");
                }
            } else if(domain.getType() == ActionType.NETWORK_UDP_SEND || domain.getType() == ActionType.NETWORK_UDP_RECEIVE){
                dto.setProtocol("UDP");
            }

            setActionUser(dto, pidUsers);
        });
    }

    public Page<ActionDeviceDto> listByDeviceType(ActionDeviceListCommand command, Pageable pageable){
        Map<String, String> pidUsers = command.getTaskId() != null ? taskService.findStrictly(command.getTaskId()).getPidUsers() : null;
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.DEVICE).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getCmdLine, command.getDeviceName())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        addUserFilter(filters, pidUsers, command.getUser());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionDeviceDto.class).apply(actions, (domain, dto) -> {
            setActionUser(dto, pidUsers);
        });
    }

    public Page<ActionSecurityDto> listBySecurityType(ActionSecurityListCommand command, Pageable pageable){
        Map<String, String> pidUsers = command.getTaskId() != null ? taskService.findStrictly(command.getTaskId()).getPidUsers() : null;
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.SECURITY).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getTarget, command.getTarget())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        addUserFilter(filters, pidUsers, command.getUser());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionSecurityDto.class).apply(actions, (domain, dto) -> {
            setActionUser(dto, pidUsers);
        });
    }

    private void setActionUser(ActionBaseDto dto, Map<String, String> pidUsers){
        if(pidUsers != null && pidUsers.containsKey(dto.getPid())){
            dto.setUser(pidUsers.get(dto.getPid()));
        }
    }

    private void addUserFilter(Filters filters, Map<String, String> pidUsers, String user){
        if(StringUtils.isNotEmpty(user) && pidUsers != null){
            List<String> pids = new ArrayList<>();
            for(Map.Entry<String, String> pidUser : pidUsers.entrySet()){
                if(pidUser.getValue().contains(user)){
                    pids.add(pidUser.getKey());
                }
            }

            if(!CollectionUtils.isEmpty(pids)){
                filters.in(Action::getPid, pids);
            } else {
                //设置一个肯定查询不到的结果
                filters.eq(Action::getUuid, "-1");
            }
        }
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
            return zipFile;
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
        //WriteOffsets有可能是空字符串，表示只有一个写到最后的
        if(action.getWriteOffsets() != null && StringUtils.isNotEmpty(action.getWriteBytes())){
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

    public Pair<File, String> downloadDeleteFile(String uuid, HttpServletResponse response) throws IOException{
        Action action = actionService.findStrictly(uuid);
        if(action.getActionGroup() != ActionGroup.FILE || (action.getType() != ActionType.FILE_DELETE_WINDOWS && action.getType() != ActionType.FILE_DELETE_LINUX)){
            throw ServiceException.of(ErrorCode.CODE_REQUESE_PARAM_ERROR);
        }
        File folder = new File(String.format(ACTION_BACKUP_FOLDER_PATH, action.getTaskId(), action.getPid()));
        if(!folder.exists()){
            folder = new File(ACTION_FOLDER, action.getPid());
        }
        File backupFile = new File(folder, StringUtils.substringAfterLast(action.getBackup(), SystemUtils.FILE_SEPARATOR));
        return Pair.of(backupFile, action.getFileName());
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
        zos.finish();
        IOUtils.closeQuietly(zos);
        IOUtils.closeQuietly(zipFileOs);
        IOUtils.closeQuietly(writeFileIs);
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
        handlePidAction(pid, taskId, thread, 0);
    }

    private final static int MAX_RETRY_TIME = 10;

    private void handlePidAction(String pid, Long taskId, ActionHanlderThread thread, int retry){
        File pidFolder = new File(ACTION_FOLDER, pid);
        if(!pidFolder.exists()){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            if(retry < MAX_RETRY_TIME && !thread.isFinish()){
                logger.error("进程action文件夹未生成， 尝试等待后重试，PID: {}", pid);
                retry ++;
                handlePidAction(pid, taskId, thread, retry);
            } else {
                logger.error("进程action文件夹重试{}次后仍未生成， 停止监听并置为监听失败，PID: {}", MAX_RETRY_TIME, pid);
                monitoringService.setMonitoringPidToError(pid);
                clearPidCache(pid);
            }
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
                            //RandomAccessFile读取的字符串是按ISO-8859-1编码读的，需要转回去UTF-8
                            line = new String(line.getBytes("ISO-8859-1"), "UTF-8");
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
        fdFileSeekMap.remove(pid);
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
                action = setActionNetworkInfo(action, pid);
            } else if(ActionType.isFileType(action.getType())){
                //设置文件信息
                action = setActionFileInfo(action, pid);
            } else if(ActionType.isDeviceType(action.getType())){
                action = setActionDeviceInfo(action, pid);
            } else if(ActionType.isRegistryType(action.getType())){
                setActionRegistryInfo(action, pid);
            } else if(ActionType.isProcessType(action.getType())){
                systemOsService.setActionProcessInfo(action, pid);
            } else if(ActionType.isSecurity(action.getType())){
                action = setActionSecurityInfo(action, pid);
            } else if(action.getType() == ActionType.FILE_SEEK && SystemUtils.isLinux()){
                //暂时只有linux有这种文件偏移操作，并且这种操作不用保存数据库
                setActionFileSeekInfo(action, pid);
                return null;
            }

            if(action != null){
                return actionService.save(action);
            }
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private void setActionFileSeekInfo(Action action, String pid){
        Map<String, Long> fdFileSeek = fdFileSeekMap.computeIfAbsent(pid, p -> new HashMap<>());
        Long lastOffset = fdFileSeek.get(action.getFd());
        Long offset = action.getOffset() == null ? 0 : action.getOffset();
        if(action.getWhere() != null){
            if(action.getWhere() == 1){
                //1 - 从当前位置偏移（即上一次读写操作后的文件偏移量）
                if(lastOffset != null && lastOffset.longValue() > 0){
                    offset = lastOffset.longValue() + offset;
                }
            } else if(action.getWhere() == 2){
                //2 - 从文件末尾向前偏移， 暂时不支持，默认为末尾写入
                logger.error("出现从文件末尾向前偏移，暂时不支持这种记录!!! TaskID: {}, PID: {}, UUID: {}", action.getTaskId(), pid, action.getUuid());
                offset = null;
            }
        }
        fdFileSeek.put(action.getFd(), offset);
        //logger.info("Linux文件读写偏移, OFFSET: {}, PID: {}", offset, pid);
    }

    private Action setActionFileInfo(Action action, String pid){
        if(action.getType() == ActionType.FILE_OPEN){
            //存在path空的情况，path为空时不做处理
            if(StringUtils.isEmpty(action.getPath())){
                return null;
            }

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
                    Long offset = action.getOffset();
                    if(SystemUtils.isLinux()){
                        //Linux下的偏移量要从“文件读写偏移定位”操作中读取
                        Map<String, Long> fdOffsetMap = fdFileSeekMap.get(pid);
                        if(fdOffsetMap != null){
                            offset = fdOffsetMap.get(action.getFd());
                        }
                    }
                    if(originAction == null){
                        originAction = action;
                        originAction.setFileName(fileInfo.getFileName());
                        originAction.setSensitivity(fileInfo.getSensitivity());
                        originAction.setPath(fileInfo.getPath());
                        originAction.setBackup(fileInfo.getBackup());
                        originAction.setDeviceName(fileInfo.getDeviceName());
                        originAction.setActionGroup(fileInfo.getActionGroup());
                        originAction.setWriteOffsets(offset == null ? "" : String.valueOf(offset));
                        originAction.setWriteBytes(String.valueOf(action.getBytes()));
                        actionMap.put(originAction.getFd(), originAction);
                    } else {
                        originAction.setTimestamp(action.getTimestamp());

                        //空的时候是写到最后，写到最后时仅仅直接修改已写入量，不再追加记录
                        if(offset == null){
                            //取最后写入量，并且进行写入量重新计算填充
                            String writeBytes = action.getWriteBytes() == null ? "" :  action.getWriteBytes();
                            int lastIndex = writeBytes.lastIndexOf(',');
                            String preBytes = null;
                            String lastBytes;
                            if(lastIndex >= 0){
                                preBytes = writeBytes.substring(0, lastIndex + 1);
                                lastBytes = writeBytes.substring(lastIndex + 1);
                            } else {
                                lastBytes = writeBytes;
                            }
                            //lastBytes为空是异常情况，正常lastBytes闭定有值
                            if(lastBytes.length() > 0) {
                                long bytes = Long.parseLong(lastBytes);
                                bytes += action.getBytes();
                                if(preBytes == null){
                                    action.setWriteBytes(String.valueOf(bytes));
                                } else {
                                    action.setWriteBytes(String.format("%s,%d", preBytes, bytes));
                                }
                            }
                        } else {
                            originAction.setWriteOffsets(String.format("%s,%d", originAction.getWriteOffsets(), offset));
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

        if(action.getType() == ActionType.FILE_DELETE_LINUX){
            action.setActionGroup(ActionGroup.FILE);
            action.setPath(action.getFile());
            action.setSensitivity(systemOsService.getFileSensitivity(action.getPath()));
            action.setFileName(new File(action.getPath()).getName());
        } else if(action.getType() == ActionType.FILE_DELETE_WINDOWS){
            //Windows平台还需要从打开文件中找回备份文件
            Map<String, FileInfo> fdFileInfoMap = fdFileMap.get(pid);
            if(fdFileInfoMap != null) {
                FileInfo fileInfo = fdFileInfoMap.get(action.getFd());
                if (fileInfo != null) {
                    action.setActionGroup(ActionGroup.FILE);
                    action.setPath(fileInfo.getPath());
                    action.setBackup(fileInfo.getBackup());
                    action.setSensitivity(fileInfo.getSensitivity());
                    action.setFileName(fileInfo.getFileName());
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        return action;
    }

    private Action setActionNetworkInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.NETWORK);
        //Windows平台所有发送和接收都带上了host和port，不用处理。只有Linux下需要处理
        if(SystemUtils.isLinux()){
            if(action.getType() == ActionType.NETWORK_OPEN){
                //Linux下会有大量端口是0的IP可达链接信息，这部分不做记录
                if(StringUtils.isNotEmpty(action.getHost()) && action.getPort() != null && action.getPort() != 0){
                    Map<Integer, NetworkInfo> socketFdNetworkInfoMap = socketFdNetworkMap.computeIfAbsent(pid, p -> new HashMap<>());
                    socketFdNetworkInfoMap.put(action.getSocketFd(), new NetworkInfo(action.getHost(), action.getPort()));
                } else {
                    return null;
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
            //Linux平台下获取的udp数据可能包括其他非IP协议的数据包，例如ping命令的协议。这部分数据包端口为0，暂时都先过滤了
            if(action.getType() == ActionType.NETWORK_UDP_SEND || action.getType() == ActionType.NETWORK_UDP_RECEIVE){
                if(action.getPort() == null || action.getPort() == 0){
                    return action;
                }
            }
        }
        return action;
    }

    private void setActionRegistryInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.REGISTRY);
    }

    private Action setActionSecurityInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.SECURITY);
        if(action.getType() == ActionType.SECURITY_FILE_UPDATE || action.getType() == ActionType.SECURITY_FILE_OWNER_UPDATE){
            //处理只有fd无path出现的情况，从open file事件中获取对应path
            if(StringUtils.isEmpty(action.getPath()) && StringUtils.isNotEmpty(action.getFd())){
                Map<String, FileInfo> fdFileInfoMap = fdFileMap.get(pid);
                if(fdFileInfoMap != null) {
                    FileInfo fileInfo = fdFileInfoMap.get(action.getFd());
                    if (fileInfo != null) {
                        action.setPath(fileInfo.getPath());
                    }
                }
            }
            action.setTarget(action.getPath());
            if(StringUtils.isEmpty(action.getTarget())){
                return null;
            }

        }
        if(action.getType() == ActionType.SECURITY_FILE_UPDATE){
            //将权限掩码也设置到daclSdString字段，用于与linux一致
            action.setDaclSdString(action.getMode());
        }
        return action;
    }

    private Action setActionDeviceInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.DEVICE);
        DeviceInfoJson deviceInfo = systemOsService.getDeviceInfo(action.getPath());
        if(deviceInfo != null){
            action.setDeviceName(deviceInfo.getFriendlyName());
            return action;
        }
        return null;
    }
}
