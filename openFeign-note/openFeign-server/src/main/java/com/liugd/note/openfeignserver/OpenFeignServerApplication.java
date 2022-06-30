package com.liugd.note.openfeignserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class OpenFeignServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenFeignServerApplication.class, args);
    }

}
