package com.liugd.note.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * JSON 转换工具
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
    }

    public static <T> T parseObject(String jsonString, Class<T> clazz){

        try {
            return mapper.readValue(jsonString, clazz);
        }catch (IOException e){
           log.error("json序列化失败.",e);
           throw new RuntimeException("json序列化失败.");
        }


    }

    public static String toString(Object object) {

        try {
            return mapper.writeValueAsString(object);
        }catch (IOException e){
            log.error("Json序列化失败.", e);
            throw new RuntimeException("Json序列化失败.");
        }

    }
}
