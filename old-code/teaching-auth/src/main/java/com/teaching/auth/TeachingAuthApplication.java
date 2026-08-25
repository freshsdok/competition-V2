package com.teaching.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import com.teaching.common.security.annotation.EnableRyFeignClients;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证授权中心
 *
 * @author teaching
 */
@EnableRyFeignClients
@EnableFeignClients(basePackages = "com.teaching.system.api")
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class , RedisAutoConfiguration.class, RedisReactiveAutoConfiguration.class })
public class TeachingAuthApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(TeachingAuthApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  天津大学教学认证授权中心启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
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
