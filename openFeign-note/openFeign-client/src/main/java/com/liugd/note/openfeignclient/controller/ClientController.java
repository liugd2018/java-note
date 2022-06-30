package com.liugd.note.openfeignclient.controller;

import com.liugd.note.openfeignclient.feign.ServerFeign;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */

@RestController
@RequestMapping("/client")
public class ClientController {

    @Resource
    ServerFeign serverFeign;

    @PostMapping("/test")
    public String test(@RequestBody String st){
        System.out.println(st);
        return serverFeign.test(st);
    }
}
