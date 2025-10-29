package com.simul_tech.netgenius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class NetGeniusApplication {
    public static void main(String[] args) {
        SpringApplication.run(NetGeniusApplication.class, args);
    }

}
