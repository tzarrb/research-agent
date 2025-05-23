package com.ivan.researchagent.main;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication(scanBasePackages = {"com.ivan","org.springframework.ai","com.alibaba.cloud.ai"})
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
        log.info("                                    \n " +
                "           tqt         rqw           \n" +
                "          ypppppv     qooooo         \n" +
                "           pppppy     rooooo         \n" +
                "            zwy         xwz          \n" +
                "            zx          zxz          \n" +
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
