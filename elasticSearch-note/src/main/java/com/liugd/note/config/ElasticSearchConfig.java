package com.liugd.note.config;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * elasticSearch 配置类
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since
 */
@Configuration
public class ElasticSearchConfig {

    protected Logger log = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private ElasticSearchFactoryProperties elasticSearchFactoryProperties;

    @Bean
    public RestHighLevelClient highLevelClient(){

        log.info("加载ES-CLIENT配置.");

        List<String> nodes = elasticSearchFactoryProperties.getNodes();

        if (CollectionUtils.isEmpty(nodes)){
            throw new RuntimeException("加载ES-CLIENT配置失败.未获取到ES服务节点信息.");
        }

        HttpHost[] httpHosts = new HttpHost[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            // ip:port
            String[] address = nodes.get(i).split(":");
            httpHosts[i] = new HttpHost(address[0],Integer.parseInt(address[1]),elasticSearchFactoryProperties.getSchema());
        }

        RestClientBuilder restClientBuilder = RestClient.builder(httpHosts);

        // 配置用户名,密码
        if (StringUtils.isNotBlank(elasticSearchFactoryProperties.getUserName()) || StringUtils.isNotBlank(elasticSearchFactoryProperties.getPassWord())){
            restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.disableAuthCaching();
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(elasticSearchFactoryProperties.getUserName(), elasticSearchFactoryProperties.getPassWord()));
                return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            });
        }
        log.info("加载ES-CLIENT配置成功.");

       return new RestHighLevelClient(restClientBuilder);
    }



}
