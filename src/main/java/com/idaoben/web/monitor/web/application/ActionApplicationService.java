package com.idaoben.web.monitor.web.application;

import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idaoben.web.common.entity.Filters;
import com.idaoben.web.common.excel.ExcelTool;
import com.idaoben.web.common.exception.ServiceException;
import com.idaoben.web.common.util.DtoTransformer;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.entity.enums.*;
import com.idaoben.web.monitor.excel.*;
import com.idaoben.web.monitor.exception.ErrorCode;
import com.idaoben.web.monitor.service.ActionService;
import com.idaoben.web.monitor.service.MonitoringService;
import com.idaoben.web.monitor.service.SystemOsService;
import com.idaoben.web.monitor.service.TaskService;
import com.idaoben.web.monitor.utils.SystemUtils;
import com.idaoben.web.monitor.web.command.*;
import com.idaoben.web.monitor.web.dto.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
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

    @Resource
    private CachedUidGenerator cachedUidGenerator;

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

    @Value("classpath:/action_file.xlsx")
    private org.springframework.core.io.Resource actionFileTemplate;

    public Workbook exportByFileType(ActionFileListCommand command) throws Exception {
        Page<ActionFileDto> actions = listByFileType(command, Pageable.unpaged());
        List<ActionFileExcel> excels = new ArrayList<>();
        for(ActionFileDto action : actions){
            ActionFileExcel excel = new ActionFileExcel();
            BeanUtils.copyProperties(action, excel);
            excels.add(excel);
        }
        return ExcelTool.createXSSFExcel(excels, actionFileTemplate.getInputStream(), 1, 0);
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

    @Value("classpath:/action_registry.xlsx")
    private org.springframework.core.io.Resource actionRegistryTemplate;

    public Workbook exportByRegistryType(ActionRegistryListCommand command) throws Exception {
        Page<ActionRegistryDto> actions = listByRegistryType(command, Pageable.unpaged());
        List<ActionRegistryExcel> excels = new ArrayList<>();
        for(ActionRegistryDto action : actions){
            ActionRegistryExcel excel = new ActionRegistryExcel();
            BeanUtils.copyProperties(action, excel, "type");
            for(ActionRegistryType type : ActionRegistryType.values()){
                if(Objects.equals(type.value(), action.getType())){
                    excel.setType(type);
                }
            }
            excels.add(excel);
        }
        return ExcelTool.createXSSFExcel(excels, actionRegistryTemplate.getInputStream(), 1, 0);
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

    @Value("classpath:/action_process.xlsx")
    private org.springframework.core.io.Resource actionProcessTemplate;

    public Workbook exportByProcessType(ActionProcessListCommand command) throws Exception {
        Page<ActionProcessDto> actions = listByProcessType(command, Pageable.unpaged());
        List<ActionProcessExcel> excels = new ArrayList<>();
        for(ActionProcessDto action : actions){
            ActionProcessExcel excel = new ActionProcessExcel();
            BeanUtils.copyProperties(action, excel, "type");
            for(ActionProcessType type : ActionProcessType.values()){
                if(Objects.equals(type.value(), action.getType())){
                    excel.setType(type);
                }
            }
            excels.add(excel);
        }
        return ExcelTool.createXSSFExcel(excels, actionProcessTemplate.getInputStream(), 1, 0);
    }

    public Page<ActionNetworkDto> listByNetworkType(ActionNetworkListCommand command, Pageable pageable){
        Map<String, String> pidUsers = command.getTaskId() != null ? taskService.findStrictly(command.getTaskId()).getPidUsers() : null;
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.NETWORK).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getHost, command.getHost()).eq(Action::getPort, command.getPort()).eq(Action::getType, command.getType())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime())
                .ge(Action::getBytes, command.getBytesMin()).le(Action::getBytes, command.getBytesMax());
        addUserFilter(filters, pidUsers, command.getUser());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionNetworkDto.class).apply(actions, (domain, dto) -> {
            //????????????
            dto.setProtocol(getProtocol(domain));

            setActionUser(dto, pidUsers);
        });
    }

    private String getProtocol(Action action){
        String protocol = null;
        //?????????????????????
        if(action.getType() == ActionType.NETWORK_TCP_SEND || action.getType() == ActionType.NETWORK_TCP_RECEIVE){
            int port = action.getPort() == null ? -1 : action.getPort();
            if(port == 443){
                protocol = "HTTPS";
            } else if(port == 80) {
                protocol = "HTTP";
            } else if(port == 53){
                protocol = "DNS";
            } else {
                protocol = "TCP";
            }
        } else if(action.getType() == ActionType.NETWORK_UDP_SEND || action.getType() == ActionType.NETWORK_UDP_RECEIVE){
            protocol = "UDP";
        }
        return protocol;
    }

    @Value("classpath:/action_network.xlsx")
    private org.springframework.core.io.Resource actionNetworkTemplate;

    public Workbook exportByNetworkType(ActionNetworkListCommand command) throws Exception {
        Page<ActionNetworkDto> actions = listByNetworkType(command, Pageable.unpaged());
        List<ActionNetworkExcel> excels = new ArrayList<>();
        for(ActionNetworkDto action : actions){
            ActionNetworkExcel excel = new ActionNetworkExcel();
            BeanUtils.copyProperties(action, excel);
            for(ActionNetworkType type : ActionNetworkType.values()){
                if(Objects.equals(type.value(), action.getType())){
                    excel.setType(type);
                }
            }
            excels.add(excel);
        }
        return ExcelTool.createXSSFExcel(excels, actionNetworkTemplate.getInputStream(), 1, 0);
    }

    public Page<ActionDeviceDto> listByDeviceType(ActionDeviceListCommand command, Pageable pageable){
        Map<String, String> pidUsers = command.getTaskId() != null ? taskService.findStrictly(command.getTaskId()).getPidUsers() : null;
        Filters filters = Filters.query().eq(Action::getActionGroup, ActionGroup.DEVICE).eq(Action::getTaskId, command.getTaskId()).eq(Action::getPid, command.getPid())
                .likeFuzzy(Action::getDeviceName, command.getDeviceName())
                .ge(Action::getTimestamp, command.getStartTime()).le(Action::getTimestamp, command.getEndTime());
        addUserFilter(filters, pidUsers, command.getUser());
        Page<Action> actions = actionService.findPage(filters, pageable);
        return DtoTransformer.asPage(ActionDeviceDto.class).apply(actions, (domain, dto) -> {
            setActionUser(dto, pidUsers);
        });
    }

    @Value("classpath:/action_device.xlsx")
    private org.springframework.core.io.Resource actionDeviceTemplate;

    public Workbook exportByDeviceType(ActionDeviceListCommand command) throws Exception {
        Page<ActionDeviceDto> actions = listByDeviceType(command, Pageable.unpaged());
        List<ActionDeviceExcel> excels = new ArrayList<>();
        for(ActionDeviceDto action : actions){
            ActionDeviceExcel excel = new ActionDeviceExcel();
            BeanUtils.copyProperties(action, excel);
            excels.add(excel);
        }
        return ExcelTool.createXSSFExcel(excels, actionDeviceTemplate.getInputStream(), 1, 0);
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
            dto.setOwner(systemOsService.getUserById(domain.getOwner()));
            dto.setGroup(systemOsService.getGroupById(domain.getGroup()));
            //Linux security desc need to change to octal String
            if(SystemUtils.isLinux() && StringUtils.isNotEmpty(dto.getDaclSdString())){
                dto.setDaclSdString(Long.toOctalString(Long.parseLong(dto.getDaclSdString())));
            }
        });
    }

    @Value("classpath:/action_security.xlsx")
    private org.springframework.core.io.Resource actionSecurityTemplate;

    public Workbook exportBySecurityType(ActionSecurityListCommand command) throws Exception {
        Page<ActionSecurityDto> actions = listBySecurityType(command, Pageable.unpaged());
        List<ActionSecurityExcel> excels = new ArrayList<>();
        for(ActionSecurityDto action : actions){
            ActionSecurityExcel excel = new ActionSecurityExcel();
            BeanUtils.copyProperties(action, excel);
            excels.add(excel);
        }
        return ExcelTool.createXSSFExcel(excels, actionSecurityTemplate.getInputStream(), 1, 0);
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
                //???????????????????????????????????????
                filters.eq(Action::getUuid, "-1");
            }
        }
    }

    public Pair<File, String> getNetworkFile(String uuid){
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
        String protocol = getProtocol(action);
        if(Objects.equals("HTTP", protocol)){
            fileName = fileName + ".txt";
        } else {
            fileName = fileName + ".raw";
        }
        return Pair.of(file, fileName);
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
        //??????zip????????????????????????????????????????????????????????????
        if(zipFile.exists()){
            return zipFile;
        }

        if(StringUtils.isEmpty(action.getBackup())){
            //???????????????????????????????????????????????????
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
        //???write file??????????????????????????????????????????
        //WriteOffsets????????????????????????????????????????????????????????????
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
        if(action.getActionGroup() != ActionGroup.FILE || (action.getType() != ActionType.FILE_DELETE_WINDOWS && action.getType() != ActionType.FILE_DELETE_LINUX) || StringUtils.isEmpty(action.getBackup())){
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

    private final static int MAX_RETRY_TIME = 3;

    private void handlePidAction(String pid, Long taskId, ActionHanlderThread thread, int retry){
        File pidFolder = new File(ACTION_FOLDER, pid);
        if(!pidFolder.exists()){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            if(retry < MAX_RETRY_TIME && !thread.isFinish()){
                logger.error("??????action????????????????????? ????????????????????????PID: {}", pid);
                retry ++;
                handlePidAction(pid, taskId, thread, retry);
            } else {
                logger.error("??????action???????????????{}????????????????????? ????????????????????????????????????PID: {}", MAX_RETRY_TIME, pid);
                monitoringService.setMonitoringPidToError(pid);
                clearPidCache(pid);
            }
            return;
        }
        //??????action??????
        for(File file : pidFolder.listFiles()){
            String fileName = file.getName();
            String pidFileName = String.format("%s_%s", pid, fileName);
            if(fileName.startsWith("actions_") && fileName.endsWith(".log")){
                int readLine = 0;
                try {
                    RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
                    long skip = actionSkipMap.getOrDefault(pidFileName, 0l);
                    logger.info("?????????{}????????????????????????PID:{}???{}", skip, pid, fileName);
                    if(skip > 0){
                        randomAccessFile.seek(skip);
                    }
                    String line = randomAccessFile.readLine();
                    //?????????????????????????????????????????????????????????????????? ???????????????????????????????????????
                    while (line != null || !thread.isFinish()){
                        if(line != null){
                            //??????????????????json??????
                            //RandomAccessFile????????????????????????ISO-8859-1??????????????????????????????UTF-8
                            line = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                            //logger.info("???????????????{}?????????", readLine);
                            Action action = handleActionJson(line, pid, taskId, fileName);
                            //???????????????????????????
                            skip = randomAccessFile.getFilePointer();
                            actionSkipMap.put(pidFileName, skip);
                            readLine ++;

                            //????????????action??????????????????????????????
                            if(action != null && action.getType() == ActionType.STOP){
                                thread.finish();
                            }
                        } else {
                            //???n????????????
                            Thread.sleep(2000);
                            logger.info("PID:{}?????????{}??????????????????????????????{}???, ???????????????", pid, fileName, readLine);
                        }
                        line = randomAccessFile.readLine();
                    }
                    //???????????????????????????
                    randomAccessFile.close();
                    logger.info("?????????{}?????????????????????,????????????{}???", fileName, readLine - 1);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        //????????????????????????
        try {
            File destFolder = new File(String.format(ACTION_BACKUP_FOLDER_PATH, taskId, pid));
            logger.info("????????????????????????{}", destFolder.getPath());
            FileUtils.moveDirectory(pidFolder, destFolder);
            logger.info("????????????????????????{}", destFolder.getPath());
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
     * ???????????????json??????
     * @param json
     * @param pid
     */
    private Action handleActionJson(String json, String pid, Long taskId, String actionName){
        try {
            ActionJson actionJson = objectMapper.readValue(json, ActionJson.class);
            Action action = actionJson.getAction();
            action.setUid(cachedUidGenerator.getUID());
            action.setUuid(actionJson.getUuid());
            action.setTimestamp(actionJson.getTimestamp());
            action.setWithAttachment(actionJson.getWithAttachment());
            action.setTaskId(taskId);
            action.setPid(pid);

            if(ActionType.isNetworkType(action.getType())){
                //???????????????????????????????????????????????????
                action = setActionNetworkInfo(action, pid);
            } else if(ActionType.isFileType(action.getType())){
                //??????????????????
                action = setActionFileInfo(action, pid);
            } else if(ActionType.isDeviceType(action.getType())){
                action = setActionDeviceInfo(action, pid);
            } else if(ActionType.isRegistryType(action.getType())){
                action = setActionRegistryInfo(action, pid);
            } else if(ActionType.isProcessType(action.getType())){
                systemOsService.setActionProcessInfo(action, pid);
            } else if(ActionType.isSecurity(action.getType())){
                action = setActionSecurityInfo(action, pid);
            } else if(action.getType() == ActionType.FILE_SEEK && SystemUtils.isLinux()){
                //????????????linux?????????????????????????????????????????????????????????????????????
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
                //1 - ????????????????????????????????????????????????????????????????????????
                if(lastOffset != null && lastOffset.longValue() > 0){
                    offset = lastOffset.longValue() + offset;
                }
            } else if(action.getWhere() == 2){
                //2 - ?????????????????????????????? ???????????????????????????????????????
                logger.info("???????????????????????????????????????????????????????????????!!! TaskID: {}, PID: {}, UUID: {}", action.getTaskId(), pid, action.getUuid());
                offset = null;
            }
        }
        fdFileSeek.put(action.getFd(), offset);
        //logger.info("Linux??????????????????, OFFSET: {}, PID: {}", offset, pid);
    }

    private Action setActionFileInfo(Action action, String pid){
        if(action.getType() == ActionType.FILE_OPEN){
            //??????path???????????????path?????????????????????
            if(StringUtils.isEmpty(action.getPath())){
                return null;
            }

            //??????path?????????????????????
            action.setFileName(StringUtils.substringAfterLast(action.getPath(), SystemUtils.FILE_SEPARATOR));

            //??????????????????????????????action group???????????????????????????
            ActionGroup actionGroup = systemOsService.setActionFromFileInfo(action);
            if(actionGroup == null){
                return null;
            }
            action.setActionGroup(actionGroup);

            //???????????????????????????????????????
            if(actionGroup == ActionGroup.FILE){
                action.setSensitivity(systemOsService.getFileSensitivity(action.getPath()));
            }

            FileAccess fileAccess = systemOsService.getFileAccess(action.getAccess());
            //????????????????????????????????????map???
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
                    //?????????????????????????????????FD????????????????????????????????????????????????
                    Map<String, Action> actionMap = writeFileMap.computeIfAbsent(pid, p -> new HashMap<>());
                    Action originAction = actionMap.get(action.getFd());
                    Long offset = action.getOffset();
                    if(SystemUtils.isLinux()){
                        //Linux??????????????????????????????????????????????????????????????????
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

                        //????????????????????????????????????????????????????????????????????????????????????????????????
                        if(offset == null){
                            //????????????????????????????????????????????????????????????
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
                            //lastBytes??????????????????????????????lastBytes????????????
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
                    //????????????????????????????????????
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
            //Windows???????????????????????????????????????????????????
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
        //Windows???????????????????????????????????????host???port????????????????????????Linux???????????????
        if(SystemUtils.isLinux()){
            if(action.getType() == ActionType.NETWORK_OPEN){
                //Linux????????????????????????0???IP??????????????????????????????????????????
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
            //Linux??????????????????udp???????????????????????????IP???????????????????????????ping?????????????????????????????????????????????0????????????????????????
            if(action.getType() == ActionType.NETWORK_UDP_SEND || action.getType() == ActionType.NETWORK_UDP_RECEIVE){
                if(action.getPort() == null || action.getPort() == 0){
                    return action;
                }
            }
        }
        return action;
    }

    private Action setActionRegistryInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.REGISTRY);
        if(action.getType() == ActionType.REGISTRY_DELETE_KEY && StringUtils.isEmpty(action.getKey())){
            return null;
        }
        return action;
    }

    private Action setActionSecurityInfo(Action action, String pid){
        action.setActionGroup(ActionGroup.SECURITY);
        if(action.getType() == ActionType.SECURITY_FILE_UPDATE || action.getType() == ActionType.SECURITY_FILE_OWNER_UPDATE){
            //????????????fd???path?????????????????????open file?????????????????????path
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
            //???????????????????????????daclSdString??????????????????linux??????
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
