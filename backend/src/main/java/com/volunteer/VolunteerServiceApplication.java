package com.volunteer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.volunteer.modules.**.mapper")
public class VolunteerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VolunteerServiceApplication.class, args);
    }
}
