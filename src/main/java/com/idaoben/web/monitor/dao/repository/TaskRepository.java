package com.idaoben.web.monitor.dao.repository;

import com.idaoben.web.common.dao.BaseRepository;
import com.idaoben.web.monitor.dao.entity.Task;

/**
*
* 任务 Dao接口
* Generated by DAOBEN CODE GENERATOR
* @author  Daoben Code Generator
*/
public interface TaskRepository extends BaseRepository<Task, Long> {

    void deleteBySoftwareId(String softwareId);
}