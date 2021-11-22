package com.liugd.note.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * es mapping 结构
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Setter
@Getter
public class SearchEsDto {

    private String id;
    private String name;
    private List<TestDto> test;
    @Setter
    @Getter
    public static class TestDto {

        private String type;
    }

}
