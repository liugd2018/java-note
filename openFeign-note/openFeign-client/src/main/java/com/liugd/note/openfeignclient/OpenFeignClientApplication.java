package com.liugd.note.openfeignclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

//@EnableEurekaClient
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class OpenFeignClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenFeignClientApplication.class, args);
    }

}
