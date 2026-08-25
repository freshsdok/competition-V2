package com.teaching.course;

import com.teaching.common.security.annotation.EnableCustomConfig;
import com.teaching.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Administrator
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class TeachingCourseApplication {
    public static void main(String[] args) {
        SpringApplication.run(TeachingCourseApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  天津大学课程模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
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
