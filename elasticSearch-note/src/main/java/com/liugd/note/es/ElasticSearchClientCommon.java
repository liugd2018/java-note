package com.liugd.note.es;

import com.liugd.note.common.constants.Constants;
import com.liugd.note.common.utils.JsonUtil;
import com.liugd.note.common.exception.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

/**
 * elasticsearch common方法
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
@Component
public class ElasticSearchClientCommon {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    RestHighLevelClient restHighLevelClient;

    /**
     * 查看索引是否存在
     *
     * @param indexName 索引名
     * @return 创建状态
     * @throws IOException 异常
     */
    public boolean existIndex(String indexName) throws IOException {
        GetIndexRequest request = new GetIndexRequest(indexName);
        return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }


    /**
     * @param indexName        索引名字
     * @param xContentBuilder  mapping
     * @param numberOfShards   分片
     * @param numberOfReplicas 备份
     * @throws IOException
     */
    public boolean createIndex(String indexName, XContentBuilder xContentBuilder,
                               int numberOfShards, int numberOfReplicas) throws IOException {
        if (!existIndex(indexName)) {
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            // settings部分
            request.settings(Settings.builder()
                    // 创建索引时，分配的主分片的数量
                    .put("index.number_of_shards", numberOfShards)
                    // 创建索引时，为每一个主分片分配的副本分片的数量
                    .put("index.number_of_replicas", numberOfReplicas)
            );
            // mapping部分 除了用json字符串来定义外，还可以使用Map或者XContentBuilder
            request.mapping(xContentBuilder);
            // 创建索引(同步的方式)
            CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            return checkResponse(response);

        } else {
            log.warn("index 已经存在. index:{}", indexName);
            return false;
        }
    }

    /**
     * check ES请求是否成功
     *
     * @param acknowledgedResponse 返回数据
     * @return {@link AcknowledgedResponse#isAcknowledged()} true if the response is acknowledged, false otherwise
     */
    boolean checkResponse(AcknowledgedResponse acknowledgedResponse) {

        if (Objects.isNull(acknowledgedResponse)) {
            return false;
        }

        return acknowledgedResponse.isAcknowledged();
    }


    /**
     * 修改Document
     *
     * @param indexName
     * @throws IOException
     */
    public boolean updateDocument(UpdateRequest request,String indexName, String documentId, Object builder) throws IOException {

        // 同步的方式发送更新请求
        UpdateResponse updateResponse = restHighLevelClient.update(request, RequestOptions.DEFAULT);
        if (Objects.isNull(updateResponse)) {
            log.info("更新document失败. index: {} documentId:{} builder:{}",
                    indexName, documentId, JsonUtil.toString(builder));
            return false;
        }
        if (updateResponse.status().getStatus() != RestStatus.OK.getStatus()) {
            log.info("更新document失败. index: {} documentId:{} builder:{} response:{}",
                    indexName, documentId, JsonUtil.toString(builder), JsonUtil.toString(updateResponse));
            return false;
        } else {
            log.info("更新document成功. index: {} documentId:{} builder:{} response:{}",
                    indexName, documentId, JsonUtil.toString(builder), JsonUtil.toString(updateResponse));

            return true;
        }

    }

    /**
     * 新增document common
     *
     * @param param
     * @param indexRequest
     * @return
     * @throws IOException
     */
    public boolean addDocument(Object param, IndexRequest indexRequest) throws IOException {
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        if (Objects.isNull(indexResponse)) {
            return false;
        }

        if ((indexResponse.status().getStatus() != RestStatus.OK.getStatus())
                && (indexResponse.status().getStatus() != RestStatus.CREATED.getStatus())) {
            log.warn("创建Document 失败. index:{} DocumentID:{} status:{} mapping:{} indexResponse:{}",
                    indexResponse.getIndex(), indexResponse.getId(), indexResponse.status().getStatus(),
                    JsonUtil.toString(param), JsonUtil.toString(indexResponse));
            return false;
        }

        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            //处理(如果需要)第一次创建文档的情况
            log.info("创建Document. index:{} DocumentID:{} mapping:{} indexResponse:{}",
                    indexResponse.getIndex(), indexResponse.getId(),
                    JsonUtil.toString(param), JsonUtil.toString(indexResponse));
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            //处理(如果需要)将文档重写为已经存在的情况
            log.info("更新已有的Document. index:{} DocumentID:{} mapping:{} indexResponse:{}",
                    indexResponse.getIndex(), indexResponse.getId(),
                    JsonUtil.toString(param), JsonUtil.toString(indexResponse));
        } else {
            log.info("更新已有的Document. index:{} DocumentID:{} mapping:{} indexResponse:{}",
                    indexResponse.getIndex(), indexResponse.getId(),
                    JsonUtil.toString(param), JsonUtil.toString(indexResponse));
        }
        return true;
    }

    public boolean deleteDocument(Object param,DeleteRequest deleteRequest) throws IOException {

        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        if (Objects.isNull(deleteResponse)) {
            return false;
        }

        if (deleteResponse.status().getStatus() != RestStatus.OK.getStatus()) {
            log.warn("创建Document 失败. index:{} DocumentID:{} status:{} mapping:{} indexResponse:{}",
                    deleteResponse.getIndex(), deleteResponse.getId(), deleteResponse.status().getStatus(),
                    JsonUtil.toString(param), JsonUtil.toString(deleteResponse));
            return false;
        }
        return true;
    }

    /**
     * 重置别名 (原子操作)
     *
     * @param aliasName 别名
     * @param indexName index name
     * @return
     * @throws IOException
     */
    public boolean changAlias(String aliasName, String indexName) throws IOException {

        IndicesAliasesRequest request = new IndicesAliasesRequest();
        // 添加别名
        IndicesAliasesRequest.AliasActions aliasActionAdd = new IndicesAliasesRequest
                .AliasActions(IndicesAliasesRequest.AliasActions.Type.ADD)
                .index(indexName)
                .alias(aliasName);
        request.addAliasAction(aliasActionAdd);

        // 获取别名下的index、解除别名的绑定
        Map<String, Set<AliasMetaData>> aliases = getAliases(aliasName);
        aliases.forEach((key, aliasMetaDataSet) -> {
            IndicesAliasesRequest.AliasActions aliasActionRemove = new IndicesAliasesRequest
                    .AliasActions(IndicesAliasesRequest.AliasActions.Type.REMOVE)
                    .index(key)
                    .alias(aliasName);
            request.addAliasAction(aliasActionRemove);
        });
        AcknowledgedResponse indicesAliasesResponse =
                restHighLevelClient.indices().updateAliases(request, RequestOptions.DEFAULT);

        return checkResponse(indicesAliasesResponse);
    }


    /**
     * 获取别名
     *
     * @param aliasName 别名
     * @return 别名下的全部index
     * @throws IOException
     */
    public Map<String, Set<AliasMetaData>> getAliases(String aliasName) throws IOException {
        GetAliasesRequest request = new GetAliasesRequest().aliases(aliasName);
        GetAliasesResponse response = restHighLevelClient.indices().getAlias(request, RequestOptions.DEFAULT);
        return response.getAliases();
    }

    /**
     * 查询es共通
     *
     * @param indexName
     * @param sourceBuilder
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> List<T> searchDocumentCommon(String indexName, SearchSourceBuilder sourceBuilder,
                                     Class<T> clazz) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);

        // 同步的方式发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<T> list = searchResolve(clazz, searchResponse);
        return list;
    }

    <T> List<T> searchResolve(Class<T> clazz, SearchResponse searchResponse) {
        if (Objects.isNull(searchResponse) || Objects.isNull(searchResponse.getHits())){
            log.warn("查询ES失败. searchResponse:{}", JsonUtil.toString(searchResponse));
            throw new BusinessException(Constants.ExceptionCode.SEARCH_ES_FAIL,"查询ES失败.");
        }
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        List<T> list = new ArrayList<>();
        esResultConvert(clazz, searchHits, list);
        return list;
    }


    /**
     * 使用游标 查询es
     *
     * @param indexName     index
     * @param sourceBuilder 检索条件
     * @param clazz
     * @param <T>
     * @param scrollTTL     游标存储时间
     * @return 门店信息
     * @throws Exception
     */
    <T> List<T> searchDocumentScroll(String indexName, SearchSourceBuilder sourceBuilder,
                                     Class<T> clazz, long scrollTTL) throws Exception {
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(scrollTTL));
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);
        // 游标保存时间
        searchRequest.scroll(scroll);
        // 同步的方式发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        if (Objects.isNull(searchResponse) || Objects.isNull(searchResponse.getHits())){
            log.warn("查询ES失败. searchResponse:{}",JsonUtil.toString(searchResponse));
            throw new BusinessException(100,"查询ES失败.");
        }
        // 获取游标ID
        String scrollId = searchResponse.getScrollId();
        SearchHit[] searchHits = searchResponse.getHits().getHits();

        List<T> list = new ArrayList<>();
        while (Objects.nonNull(searchHits) && searchHits.length > 0) {
            esResultConvert(clazz, searchHits, list);
            // 根据游标去查询
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            // 获取游标ID
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
        }

        // 删除游标
        if (!clearScroll(scrollId)) {
            log.error("删除游标失败. scrollId:{}", scrollId);
        }

        return list;
    }


    /**
     * es 返回结构转换
     * @param clazz
     * @param searchHits
     * @param list
     * @param <T>
     */
    private <T> void esResultConvert(Class<T> clazz, SearchHit[] searchHits, List<T> list) {
        for (SearchHit searchHit : searchHits) {
            list.add(JsonUtil.parseObject(searchHit.getSourceAsString(),clazz));
        }
    }

    /**
     * 删除游标
     *
     * @param scrollId 游标ID
     * @return
     * @throws IOException
     */
    private boolean clearScroll(String scrollId) throws IOException {
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        return clearScrollResponse.isSucceeded();
    }

    /**
     * bulk api批量添加document操作
     *
     * @param indexRequestList
     * @throws IOException
     */
    public boolean bulkAddDocuments(List<IndexRequest> indexRequestList) throws IOException {
        BulkRequest request = new BulkRequest();

        for (IndexRequest indexRequest : indexRequestList) {
            request.add(indexRequest);
        }
        // 调用批量的处理
        return bulkOperationCommon(request);
    }

    /**
     * bulk api批量更新document操作
     *
     * @param updateRequests
     * @throws IOException
     */
    public boolean bulkUpdateDocuments(List<UpdateRequest> updateRequests) throws IOException {
        BulkRequest request = new BulkRequest();

        for (UpdateRequest updateRequest : updateRequests) {
            request.add(updateRequest);
        }
        // 调用批量的处理
        return bulkOperationCommon(request);
    }


    /**
     * bulk api批量删除document操作
     *
     * @param deleteRequests
     * @throws IOException
     */
    public boolean bulkDeleteDocuments(List<DeleteRequest> deleteRequests) throws IOException {
        BulkRequest request = new BulkRequest();

        for (DeleteRequest indexRequest : deleteRequests) {
            request.add(indexRequest);
        }
        // 调用批量的处理
        return bulkOperationCommon(request);
    }

    /**
     * bulk api批量删除document操作
     *
     * @param deleteRequests
     * @throws IOException
     */
    public boolean bulkDeleteIndexes(List<DeleteIndexRequest> deleteRequests) throws IOException {

        for (DeleteIndexRequest indexRequest : deleteRequests) {
            restHighLevelClient.indices().delete(indexRequest, RequestOptions.DEFAULT);
        }

        return true;
    }

    /**
     * 批量操作共通
     *
     * @param request
     * @return
     * @throws IOException
     */
    private boolean bulkOperationCommon(BulkRequest request) throws IOException {
        BulkResponse bulkResponse = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        if (Objects.isNull(bulkResponse)) {
            log.warn("批量操作失败. 未返回信息.");
            return false;
        }
        // 打印批量操作日志信息
        bulkResponseInfo(bulkResponse);

        // 判断是否执行成功
        if (bulkResponse.hasFailures()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 打印批量操作日志信息
     *
     * @param bulkResponse
     */
    private void bulkResponseInfo(BulkResponse bulkResponse) {
        for (BulkItemResponse bulkItemResponse : bulkResponse.getItems()) {
            if (bulkItemResponse.isFailed()) {
                BulkItemResponse.Failure failure = bulkItemResponse.getFailure();
                log.warn("批量操作失败. index:{} id:{} context: {}", failure.getIndex(), failure.getId(), JsonUtil.toString(failure));
            } else {
                DocWriteResponse itemResponse = bulkItemResponse.getResponse();
                if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                        || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                    IndexResponse indexResponse = (IndexResponse) itemResponse; // index 操作后的响应结果
                    log.info("批量操作成功. index:{} id:{} opType:{} context: {}",
                            bulkItemResponse.getIndex(), bulkItemResponse.getId(),
                            bulkItemResponse.getOpType(), indexResponse.toString());

                } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                    UpdateResponse updateResponse = (UpdateResponse) itemResponse; // update 操作后的响应结果
                    log.info("批量操作成功. index:{} id:{} opType:{} context: {}",
                            bulkItemResponse.getIndex(), bulkItemResponse.getId(),
                            bulkItemResponse.getOpType(), updateResponse.toString());

                } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
                    DeleteResponse deleteResponse = (DeleteResponse) itemResponse; // delete 操作后的响应结果

                    log.info("批量操作成功. index:{} id:{} opType:{} context: {}",
                            bulkItemResponse.getIndex(), bulkItemResponse.getId(),
                            bulkItemResponse.getOpType(), deleteResponse.toString());

                } else {
                    log.info("批量操作成功. index:{} id:{} opType:{} context: {}",
                            bulkItemResponse.getIndex(), bulkItemResponse.getId(),
                            bulkItemResponse.getOpType(), JsonUtil.toString(itemResponse));
                }
            }
        }
    }
    /**
     * 获取index 信息
     *
     * @param indexName index name 可以适用模糊匹配方式获取
     * @return index 详细信息
     * @throws IOException
     */
    public GetIndexResponse getIndexInfo(String indexName) throws IOException {

        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        return restHighLevelClient.indices().get(getIndexRequest, RequestOptions.DEFAULT);
    }

    /**
     * 新增Document (使用Map)
     *
     * @param indexName
     * @throws IOException
     */
    public boolean addDocument(String indexName, String documentId, Map<String, Object> mappingMap) throws IOException {
        IndexRequest indexRequest = new IndexRequest(indexName)
                .id(documentId)
                .source(mappingMap);

        return addDocument(mappingMap, indexRequest);
    }

    /**
     * 新增Document (使用xContentBuilder)
     *
     * @param indexName
     * @throws IOException
     */
    public boolean addDocument(String indexName, String documentId, XContentBuilder xContentBuilder) throws IOException {
        IndexRequest indexRequest = new IndexRequest(indexName)
                .id(documentId)
                .source(xContentBuilder);

        return addDocument(xContentBuilder, indexRequest);
    }



    /**
     * must 共通
     * @param boolQueryBuilder
     * @param queryBuilders
     */
    void must(BoolQueryBuilder boolQueryBuilder, List<QueryBuilder> queryBuilders) {

        if (CollectionUtils.isNotEmpty(queryBuilders)) {
            for (QueryBuilder queryBuilder : queryBuilders) {
                boolQueryBuilder.must(queryBuilder);
            }
        }
    }

    /**
     * filter 共通
     * @param boolQueryBuilder
     * @param queryBuilders
     */
    void filter(BoolQueryBuilder boolQueryBuilder, List<QueryBuilder> queryBuilders) {

        if (CollectionUtils.isNotEmpty(queryBuilders)) {
            for (QueryBuilder queryBuilder : queryBuilders) {
                boolQueryBuilder.filter(queryBuilder);
            }
        }
    }



    /**
     * should 共通
     * @param boolQueryBuilder
     * @param queryBuilders
     */
    void should(BoolQueryBuilder boolQueryBuilder, List<QueryBuilder> queryBuilders) {
        if (CollectionUtils.isNotEmpty(queryBuilders)) {
            for (QueryBuilder queryBuilder : queryBuilders) {
                boolQueryBuilder.should(queryBuilder);
            }
            boolQueryBuilder.minimumShouldMatch("1");
        }
    }

    /**
     * mustNot 共通
     * @param boolQueryBuilder
     * @param queryBuilders
     */
    void mustNot(BoolQueryBuilder boolQueryBuilder, List<QueryBuilder> queryBuilders) {
        if (CollectionUtils.isNotEmpty(queryBuilders)) {
            for (QueryBuilder queryBuilder : queryBuilders) {
                boolQueryBuilder.mustNot(queryBuilder);
            }
        }
    }





}
