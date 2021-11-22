# Elasticsearch日常使用

## ES依赖

es client使用的是es官网依赖RestHighLevelClient。

es版本：7.6.2

```xml
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.6.2</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-client</artifactId>
            <version>7.6.2</version>
        </dependency>
        <dependency>
            <groupId>org.elasticsearch</groupId>
            <artifactId>elasticsearch</artifactId>
            <version>7.6.2</version>
        </dependency>
```



## 创建ES RestHighLevelClient

```java
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
```

