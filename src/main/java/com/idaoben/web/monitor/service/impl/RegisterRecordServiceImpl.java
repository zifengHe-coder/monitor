package com.idaoben.web.monitor.service.impl;

import com.idaoben.web.common.service.impl.BaseServiceImpl;
import com.idaoben.web.monitor.dao.entity.RegisterRecord;
import com.idaoben.web.monitor.dao.repository.RegisterRecordRepository;
import com.idaoben.web.monitor.service.RegisterRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
*
* 注册记录服务实现serviceImpl
* Generated by DAOBEN CODE GENERATOR
* @author  Daoben Code Generator
*/
@Service
public class RegisterRecordServiceImpl extends BaseServiceImpl<RegisterRecord, Long> implements RegisterRecordService {
    @Resource
    private RegisterRecordRepository repository;

    @Override
    public RegisterRecord findByCompanyAndCT(String companyName, String creationTime) {
        RegisterRecord record = repository.findByCompanyAndCT(companyName, creationTime);
        return record;
    }

    @Override
    public void consumeRegisterFile(Long id, Integer number) {
        repository.consumeRegisterFile(id, number);
    }

}