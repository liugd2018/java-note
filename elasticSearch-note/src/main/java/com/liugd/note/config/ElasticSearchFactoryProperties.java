package com.liugd.note.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
/**
 * es 配置信息
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.elasticsearch")
public class ElasticSearchFactoryProperties {

    /** es节点信息 */
    private List<String> nodes;

    /** schema */
    private  String schema;

    private  String userName;

    private  String passWord;

    private int numberOfShards;

    private int numberOfReplicas;
}
