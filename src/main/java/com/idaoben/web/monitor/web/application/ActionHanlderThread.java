package com.idaoben.web.monitor.web.application;

public abstract class ActionHanlderThread extends Thread{

    private boolean finish;

    private String pid;

    private Long taskId;

    public ActionHanlderThread(String pid, Long taskId) {
        this.pid = pid;
        this.taskId = taskId;
    }

    public String getPid() {
        return pid;
    }

    public void finish(){
        finish = true;
    }

    public boolean isFinish(){
        return finish;
    }

    @Override
    public void run() {
        actionHandle(pid, taskId);
    }

    abstract void actionHandle(String pid, Long taskId);
}
