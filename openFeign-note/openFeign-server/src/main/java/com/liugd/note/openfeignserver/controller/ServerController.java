package com.liugd.note.openfeignserver.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@RestController
@RequestMapping("/server")
public class ServerController {

    @PostMapping("/test01")
    public String test01(@RequestBody String st){
        System.out.println(st);
        return st;
    }

}
