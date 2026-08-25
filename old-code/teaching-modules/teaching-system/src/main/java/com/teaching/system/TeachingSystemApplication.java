package com.teaching.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.teaching.common.security.annotation.EnableCustomConfig;
import com.teaching.common.security.annotation.EnableRyFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 系统模块
 *
 * @author teaching
 */
@EnableAsync
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class TeachingSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeachingSystemApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  天津大学教学系统模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "            #      #      #              #             #  #   #     \n" +
                "   ###########      #     ##  #          ##            ## ##  ##    \n" +
                "       ##           ## #########         ##           # #  # ##  #  \n" +
                "       ##           ##    ## ##          ##          ############## \n" +
                "       ##         #   ###########        ##    #    ###         ##  \n" +
                "       ##    #     #      ## ##     #############    #       #  #   \n" +
                "  #############    ##  ########          ##             #######     \n" +
                "       ###         ## #   ## #           ###                ##      \n" +
                "      ## #            #   ##            ## #               ##       \n" +
                "      ## ##          # ########         ## ##              ##   #   \n" +
                "     ##   #       # ##    ##           ##   #        #############  \n" +
                "     ##   ##       ##     ##  #        ##   ##             ##       \n" +
                "    ##     ##      ## ##########      ##     ##            ##       \n" +
                "   ##      ###     ###    ##         ##       ###          ##       \n" +
                "  ##        ####    ##    ##        ##         ###       ####       \n" +
                " #            #     #     #        #            #          #        \n");
    }
}
