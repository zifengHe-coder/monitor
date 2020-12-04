package com.idaoben.web.monitor;

import com.idaoben.common.generator.config.Configuration;
import com.idaoben.common.generator.framework.Application;
import com.idaoben.common.generator.task.*;
import com.idaoben.web.monitor.dao.entity.Favorite;

public class GeneratorApplication {

    public static void main(String[] args) {
        Configuration.daoPackageName = "dao.repository";
        Configuration.entityPackageName = "dao.entity";
        Application application = new Application(GeneratorApplication.class);
        application//.addApplicationTask(ScanEntityTask.class) //扫描entity类，必须加上
                .addApplicationTask(EntityTask.class) //扫描指定的entity类
                .addApplicationTask(ServiceTask.class) //生成service接口代码
                .addApplicationTask(ServiceImplTask.class) //生成serviceImpl实现代码
                .addApplicationTask(DaoTask.class) //生成Dao接口代码
                .addApplicationTask(DtoTask.class) //生成Dto接口代码
                //.addApplicationTask(CommandCreateTask.class) //生成CommandCreate相关代码
                //.addApplicationTask(CommandUpdateTask.class) //生成CommandUpdate相关代码
                .work();
    }

    public class EntityTask extends SimpleEntityTask {
        @Override
        protected Class<?>[] getEntityClasses() {
            return new Class[]{Favorite.class};
        }
    }
}
