package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.common.service.impl.BaseServiceExImpl;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.repository.ActionRepository;
import com.idaoben.web.monitor.service.ActionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
*
* 应用行为服务实现serviceImpl
* Generated by DAOBEN CODE GENERATOR
* @author  Daoben Code Generator
*/
@Service
public class ActionServiceImpl extends BaseServiceExImpl<ActionRepository, Action, String> implements ActionService {

    @Override
    public void deleteByTaskIds(List<Long> taskIds) {
        getRepository().deleteByTaskIdIn(taskIds);
    }

}