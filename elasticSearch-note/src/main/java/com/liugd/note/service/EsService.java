package com.liugd.note.service;

import com.liugd.note.dto.SearchEsDto;

import java.io.IOException;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
public interface EsService {

    /**
     * 根据id查询 es结构
     * @param id
     * @param name
     *
     * @return
     * @throws Exception
     */
    List<SearchEsDto> searchByIdAndName(String id, String name) throws Exception;


    /**
     * 创建 es 索引
     * @param indexName
     * @return
     * @throws IOException
     */
    Boolean createIndex(String indexName) throws IOException;

    /**
     * 单条数据存入es
     * @param searchEsDto
     * @return
     * @throws IOException
     */
    SearchEsDto insert(SearchEsDto searchEsDto) throws IOException;

    /**
     * 批量数据存入es
     * @param searchEsDtoList
     * @return
     * @throws IOException
     */
    Boolean insertBulk(List<SearchEsDto> searchEsDtoList) throws IOException;


    /**
     * 更新
     * @param searchEsDto
     * @return
     * @throws IOException
     */
    Boolean update(SearchEsDto searchEsDto) throws IOException;

    /**
     * 批量更新
     * @param searchEsDtoList
     * @return
     * @throws IOException
     */
    Boolean updateBulk(List<SearchEsDto> searchEsDtoList) throws IOException;

    /**
     * 删除
     * @param searchEsDto
     * @return
     * @throws IOException
     */
    Boolean delete(SearchEsDto searchEsDto) throws IOException;

    /**
     * 批量删除
     * @param searchEsDtoList
     * @return
     * @throws IOException
     */
    Boolean deleteBulk(List<SearchEsDto> searchEsDtoList) throws IOException;


}
