package com.ivan.researchagent.main;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.vectorstore.elasticsearch.autoconfigure.ElasticsearchVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@Slf4j
@EnableKnife4j
@SpringBootApplication(scanBasePackages = {"com.ivan","org.springframework.ai","com.alibaba.cloud.ai"},
                                exclude = {DataSourceAutoConfiguration.class, ElasticsearchVectorStoreAutoConfiguration.class})
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
        log.info("                                    \n " +
                "          spppppw     rooooox        \n" +
                "         qppppppps   pooooooot       \n" +
                "        tpppp  wmmn oooz  ooooy      \n" +
                "        ppppw    voooy    poooo      \n" +
                "       spppp              yoooox     \n" +
                "       qpppp               oooor     \n" +
                "       zpppy               toopp     \n" +
                "                                     \n " +
                "      @Copyright Power by ivan!      \n " +
                "     Architect Agent service started! ");
    }

}
