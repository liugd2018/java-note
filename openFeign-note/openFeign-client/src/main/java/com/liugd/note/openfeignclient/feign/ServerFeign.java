package com.liugd.note.openfeignclient.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@FeignClient("server-0")
public interface ServerFeign {


    @PostMapping("/server/test01")
    public String test(@RequestBody String st);


}
