package cn.bupt.edu.zfq.reggietakeout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class ReggieTakeoutApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieTakeoutApplication.class, args);
    }
}
