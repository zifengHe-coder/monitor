package com.idaoben.web.monitor.dao.entity;

import com.idaoben.web.common.entity.Description;
import com.idaoben.web.common.entity.TrackableObject;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.*;

@Entity
@Table(name = "t_task")
@Description("任务")
public class Task extends TrackableObject {

    @Description("启动时间")
    @Column(nullable = false)
    private ZonedDateTime startTime;

    @Description("结束时间")
    @Column
    private ZonedDateTime endTime;

    @Description("软件ID")
    @Column(nullable = false)
    private String softwareId;

    @Description("软件名称")
    @Column
    private String softwareName;

    @Description("路径")
    @Column
    private String exePath;

    @Description("用户进程列表，格式pid1:user1,pid2:user2")
    @Column(length = 1000)
    private String pids;

    @Description("是否已完成")
    @Column(nullable = false)
    private boolean complete;

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public String getSoftwareId() {
        return softwareId;
    }

    public void setSoftwareId(String softwareId) {
        this.softwareId = softwareId;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public String getPids() {
        return pids;
    }

    public void setPids(String pids) {
        this.pids = pids;
    }

    public void setPidUsers(Collection<Integer> pids, Collection<String> users){
        Iterator<Integer> pidIt = pids.iterator();
        Iterator<String> userIt = users.iterator();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        while (pidIt.hasNext() && userIt.hasNext()){
            Integer pid = pidIt.next();
            String user = userIt.next();
            if(first){
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(pid.toString()).append(':').append(user);

        }
        this.pids = sb.toString();
    }

    public void addPidUser(String pid, String user){
        if(StringUtils.isEmpty(pids)){
            setPidUsers(Collections.singletonList(Integer.valueOf(pid)),  Collections.singletonList(user));
        } else {
            Set<String> pids = getPidUsers().keySet();
            if(!pids.contains(pid)){
                this.pids += String.format(",%s:%s", pid, user);
            }
        }
    }

    public Map<String, String> getPidUsers(){
        Map<String, String> pidUsers = new HashMap<>();
        if (!StringUtils.isEmpty(pids)) {
            String[] pidUserStrs = pids.split(",");
            for (String pidUserStr : pidUserStrs) {
                String[] tempPidUser = pidUserStr.split( ":");
                if(tempPidUser.length > 1){
                    pidUsers.put(tempPidUser[0], tempPidUser[1]);
                }
            }
        }
        return pidUsers;
    }

    public String getExePath() {
        return exePath;
    }

    public void setExePath(String exePath) {
        this.exePath = exePath;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
