package eu.insidemind.elasticTraining.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "eu.insidemind.elasticTraining.customer.repository")
class ElasticConfiguration {

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(NodeBuilder.nodeBuilder()
                .local(true)
                .node()
                .client());
    }

    @Bean
    public ElasticService elasticService() {
        JsonManager jsonManager = new JsonManager(new ObjectMapper());

        ElasticsearchOperations elasticsearchTemplate = new ElasticsearchTemplate(NodeBuilder.nodeBuilder()
                .settings(Settings.builder().put("path.home", "/tmp/embedded-elasticsearch-temp-dir").build())
                .local(true)
                .node()
                .client());

        return new ElasticService(elasticsearchTemplate, jsonManager);
    }
}
