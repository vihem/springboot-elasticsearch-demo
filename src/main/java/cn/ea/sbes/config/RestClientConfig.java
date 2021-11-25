package cn.ea.sbes.config;


import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import java.time.Duration;

//@Configuration
public class RestClientConfig extends AbstractElasticsearchConfiguration {

    @Bean
    @Override
    public RestHighLevelClient elasticsearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("192.168.25.137:9200")
//                .usingSsl()
//                .withProxy("localhost:8888")
//                .withPathPrefix("ela")
                .withConnectTimeout(Duration.ofSeconds(5))
                .withSocketTimeout(Duration.ofSeconds(3))
//                . // ... other options
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
}
