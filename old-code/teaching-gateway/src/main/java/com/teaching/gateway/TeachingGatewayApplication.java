package com.teaching.gateway;

import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 网关启动程序
 *
 * @author teaching
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class, RedisReactiveAutoConfiguration.class , RedissonAutoConfiguration.class })
public class TeachingGatewayApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(TeachingGatewayApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  天津大学教学网关启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
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
