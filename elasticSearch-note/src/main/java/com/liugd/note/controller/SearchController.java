package com.liugd.note.controller;

import com.liugd.note.dto.SearchEsDto;
import com.liugd.note.service.EsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Api(tags = "es操作")
@RestController
@RequestMapping("/es")
public class SearchController {


    public final static String INDEX_NAME ="index_test";

    @Resource
    EsService esService;

    @ApiOperation("查询")
    @GetMapping("/test")
    public List<SearchEsDto> searchEs(@RequestParam(value = "id",required = false) String id, @RequestParam(value = "name") String name) throws Exception {
        return esService.searchByIdAndName(id, name);
    }

    @ApiOperation("创建索引")
    @GetMapping("/createIndex")
    public Boolean createIndex() throws IOException {
       return esService.createIndex(INDEX_NAME);
    }
    @ApiOperation("插入数据")
    @PostMapping("/test")
    public SearchEsDto add(@RequestBody SearchEsDto searchEsDto) throws IOException {
        return esService.insert(searchEsDto);
    }

    @ApiOperation("批量插入数据")
    @PostMapping("/testBulk")
    public void addBulk(@RequestBody List<SearchEsDto> searchEsDto) throws IOException {
        esService.insertBulk(searchEsDto);
    }
    @ApiOperation("更新数据")
    @PutMapping("/test")
    public void update(@RequestBody SearchEsDto searchEsDto) throws IOException {
        esService.update(searchEsDto);
    }
    @ApiOperation("批量更新数据")
    @PutMapping("/testBulk")
    public void updateBulk(@RequestBody List<SearchEsDto> searchEsDtoList) throws IOException {
        esService.updateBulk(searchEsDtoList);
    }

    @ApiOperation("删除数据")
    @DeleteMapping("/test")
    public void delete(@RequestBody SearchEsDto searchEsDto) throws IOException {
        esService.update(searchEsDto);
    }
    @ApiOperation("批量删除数据")
    @DeleteMapping("/testBulk")
    public void deleteBulk(@RequestBody List<SearchEsDto> searchEsDtoList) throws IOException {
        esService.updateBulk(searchEsDtoList);
    }

}
