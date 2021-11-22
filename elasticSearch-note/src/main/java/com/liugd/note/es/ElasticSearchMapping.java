package com.liugd.note.es;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import java.io.IOException;

/**
 * es mapping 信息
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
public class ElasticSearchMapping {


    /**
     * test mapping信息
     * @return
     * @throws IOException
     */
    public static XContentBuilder createTestMapping() throws IOException{

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.field("dynamic", "strict");
            builder.startObject("properties");
            {
                builder.startObject("testId");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

            }
            builder.endObject();
        }
        builder.endObject();

        return builder;

    }


    public static XContentBuilder setTestIndexRequest() throws IOException {
        XContentBuilder xContentBuilder = XContentFactory.jsonBuilder();
        xContentBuilder.startObject();
        {
            xContentBuilder.field("testId","test01");

        }
        xContentBuilder.endObject();

        return xContentBuilder;
    }
}
