package eu.insidemind.elasticTraining.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

@Configuration
public class ElasticConfig {

    @Bean
    public EmbeddedElastic elastic() {
        return EmbeddedElastic.builder()
                .withElasticVersion("5.0.0")
                .withSetting(PopularProperties.TRANSPORT_TCP_PORT, 9350)
                .withSetting(PopularProperties.CLUSTER_NAME, "my_cluster")
                .withPlugin("analysis-stempel")
                .build();
    }
}
