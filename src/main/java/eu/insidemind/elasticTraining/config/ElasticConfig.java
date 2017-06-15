package eu.insidemind.elasticTraining.config;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class ElasticConfig {

    public final static String CLUSTER_NAME_VALUE = "my_cluster";

    public final static int TRANSPORT_TCP_PORT_VALUE = 9350;

    @Bean
    public  elastic() throws IOException {
        return EmbeddedElastic.builder()
                .withElasticVersion("5.0.0")
                .withSetting(PopularProperties.TRANSPORT_TCP_PORT, TRANSPORT_TCP_PORT_VALUE)
                .withSetting(PopularProperties.CLUSTER_NAME, CLUSTER_NAME_VALUE)
                .build();
    }

    @Bean
    public Client client() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME_VALUE).build();
        return new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getLocalHost(), TRANSPORT_TCP_PORT_VALUE));

    }
}
