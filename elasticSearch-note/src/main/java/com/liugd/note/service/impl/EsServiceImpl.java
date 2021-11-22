package com.liugd.note.service.impl;

import com.liugd.note.common.utils.JsonUtil;
import com.liugd.note.config.ElasticSearchFactoryProperties;
import com.liugd.note.controller.SearchController;
import com.liugd.note.dto.SearchEsDto;
import com.liugd.note.es.ElasticSearchClientCommon;
import com.liugd.note.es.mapping.MappingCommon;
import com.liugd.note.service.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Slf4j
@Service
public class EsServiceImpl implements EsService {

    @Resource
    private ElasticSearchClientCommon elasticSearchClientCommon;

    @Resource
    private ElasticSearchFactoryProperties properties;

    @Override
    public List<SearchEsDto> searchByIdAndName(String id, String name) throws Exception {


        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(id)){
            boolQueryBuilder.filter(QueryBuilders.termQuery("id",id));
        }

        boolQueryBuilder.filter(QueryBuilders.matchPhraseQuery("name", name));
        searchSourceBuilder.query(boolQueryBuilder);
        // page size
//        searchSourceBuilder.size(50);
        // page num
//        searchSourceBuilder.from(0);


        return elasticSearchClientCommon.searchDocumentCommon(SearchController.INDEX_NAME, searchSourceBuilder, SearchEsDto.class);
    }

    @Override
    public Boolean createIndex(String indexName) throws IOException {

        if (!elasticSearchClientCommon.existIndex(SearchController.INDEX_NAME)){
            return elasticSearchClientCommon.createIndex(indexName, MappingCommon.createMapping(),
                    properties.getNumberOfShards(), properties.getNumberOfReplicas());
        }

        return true;
    }

    @Override
    public Boolean insertBulk(List<SearchEsDto> searchEsDtoList) throws IOException {

        List<IndexRequest> indexRequests = searchEsDtoList.stream()
                .filter(Objects::nonNull)
                .map(searchEsDto -> {
                    IndexRequest indexRequest = new IndexRequest(SearchController.INDEX_NAME);
                    indexRequest.id(UUID.randomUUID().toString());
                    indexRequest.source(searchEsDto);
                    return indexRequest;
                }).collect(Collectors.toList());

        return elasticSearchClientCommon.bulkAddDocuments(indexRequests);
    }

    @Override
    public Boolean update(SearchEsDto searchEsDto) throws IOException {

        UpdateRequest request = generateUpdateRequest(searchEsDto);

        return elasticSearchClientCommon.updateDocument(request,SearchController.INDEX_NAME, searchEsDto.getId(), searchEsDto);
    }

    /**
     * 生成 {@link UpdateRequest}
     * @param searchEsDto
     * @return
     * @throws IOException
     */
    private UpdateRequest generateUpdateRequest(SearchEsDto searchEsDto) throws IOException {
        UpdateRequest request = new UpdateRequest(SearchController.INDEX_NAME, searchEsDto.getId());

        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
        xContentBuilder.startObject();
        {
            xContentBuilder.field("name", searchEsDto.getName());
            xContentBuilder.startArray("test");
            {
                for (SearchEsDto.TestDto testDto : searchEsDto.getTest()) {
                    xContentBuilder.startObject();
                    xContentBuilder.field("type", testDto.getType());

                    xContentBuilder.endObject();
                }
            }
            xContentBuilder.endArray();
        }
        xContentBuilder.endObject();

        request.doc(xContentBuilder);
        return request;
    }

    @Override
    public Boolean updateBulk(List<SearchEsDto> searchEsDtoList) throws IOException {

        List<UpdateRequest> updateRequests = searchEsDtoList.stream()
                .map(searchEsDto -> {
                    try {
                        return generateUpdateRequest(searchEsDto);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
        return elasticSearchClientCommon.bulkUpdateDocuments(updateRequests);
    }

    @Override
    public Boolean delete(SearchEsDto searchEsDto) throws IOException {

        DeleteRequest deleteRequest = new DeleteRequest(SearchController.INDEX_NAME, searchEsDto.getId());

        return elasticSearchClientCommon.deleteDocument(searchEsDto,deleteRequest);
    }

    @Override
    public Boolean deleteBulk(List<SearchEsDto> searchEsDtoList) throws IOException {

        List<DeleteRequest> deleteRequests = searchEsDtoList.stream()
                .map(searchEsDto -> new DeleteRequest(SearchController.INDEX_NAME, searchEsDto.getId()))
                .collect(Collectors.toList());
        return elasticSearchClientCommon.bulkDeleteDocuments(deleteRequests);
    }

    @Override
    public SearchEsDto insert(SearchEsDto searchEsDto) throws IOException {

        IndexRequest indexRequest = new IndexRequest(SearchController.INDEX_NAME);
        String id = UUID.randomUUID().toString();
        indexRequest.id(id);

        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
        xContentBuilder.startObject();
        {
            xContentBuilder.field("id", id);
            xContentBuilder.field("name", searchEsDto.getName());
            xContentBuilder.startArray("test");
            if (CollectionUtils.isNotEmpty(searchEsDto.getTest())) {
                for (SearchEsDto.TestDto testDto : searchEsDto.getTest()) {

                    xContentBuilder.startObject();
                    xContentBuilder.field("type", testDto.getType());
                    xContentBuilder.endObject();
                }
            }
            xContentBuilder.endArray();
        }
        xContentBuilder.endObject();
        indexRequest.source(xContentBuilder);

        searchEsDto.setId(id);

        boolean document = elasticSearchClientCommon.addDocument(SearchController.INDEX_NAME, indexRequest);
        if (document) {
            return searchEsDto;
        } else {
            return null;
        }
    }
}
