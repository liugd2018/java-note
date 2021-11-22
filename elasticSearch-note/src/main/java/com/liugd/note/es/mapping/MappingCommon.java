package com.liugd.note.es.mapping;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

/**
 * TODO
 *
 * @author <a href="mailto:liugd2020@gmail.com">liuguodong</a>
 * @since 1.0
 */
public class MappingCommon {

    public static XContentBuilder createMapping() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            // 设置为不能动态更改field
            builder.field("dynamic", "strict");
            builder.startObject("properties");
            {
                builder.startObject("id");
                {
                    // keyword 不知道分词
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("name");
                {
                    // test支持分词
                    builder.field("type", "text");
                }
                builder.endObject();

                builder.startObject("test");
                {
                    // 设置为不能动态更改field
                    builder.field("dynamic", "strict");
                    builder.startObject("properties");
                    {
                        builder.startObject("type");
                        {
                            builder.field("type", "keyword");
                        }
                        builder.endObject();
                    }
                    builder.endObject();

                }
                builder.endObject();

            }
            builder.endObject();
        }
        builder.endObject();
        return builder;
    }
}
