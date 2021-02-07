package com.idaoben.web.monitor;

import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.google.common.base.Predicates;
import com.idaoben.web.common.dao.impl.BaseRepositoryImpl;
import com.idaoben.web.monitor.dao.entity.Action;
import com.idaoben.web.monitor.dao.repository.ActionRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableJpaRepositories(basePackageClasses = {ActionRepository.class}, repositoryBaseClass = BaseRepositoryImpl.class)
@EntityScan(basePackageClasses = {Action.class})
@EnableScheduling
public class MonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }

    @Bean
    public Docket api() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.or(
                        PathSelectors.ant("/api/**")))
                .build();
        return docket;
    }

    @Bean
    public CachedUidGenerator cachedUidGenerator(){
        CachedUidGenerator cachedUidGenerator = new CachedUidGenerator();
        cachedUidGenerator.setTimeBits(29);
        cachedUidGenerator.setWorkerBits(21);
        cachedUidGenerator.setSeqBits(13);
        cachedUidGenerator.setEpochStr("2021-02-07");
        cachedUidGenerator.setBoostPower(3);
        cachedUidGenerator.setWorkerIdAssigner(new MyWorkerIdAssigner());
        return cachedUidGenerator;
    }

}
