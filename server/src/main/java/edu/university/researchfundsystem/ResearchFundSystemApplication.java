package edu.university.researchfundsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("edu.university.researchfundsystem.mapper")
public class ResearchFundSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResearchFundSystemApplication.class, args);
    }

}
