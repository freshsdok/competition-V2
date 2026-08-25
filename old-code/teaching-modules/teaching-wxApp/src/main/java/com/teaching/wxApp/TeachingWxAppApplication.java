package com.teaching.wxApp;

import com.teaching.common.security.annotation.EnableCustomConfig;
import com.teaching.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 微信小程序模块
 *
 * @author teaching
 */
@EnableAsync
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class TeachingWxAppApplication {
    public static void main( String[] args ) {
        SpringApplication.run(TeachingWxAppApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  天津大学微信小程序模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
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
