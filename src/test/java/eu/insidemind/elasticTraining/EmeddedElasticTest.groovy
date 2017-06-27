package eu.insidemind.elasticTraining

import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.skyscreamer.jsonassert.JSONAssert
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import spock.lang.Specification

import static eu.insidemind.elasticTraining.SampleIndices.*
import static java.util.concurrent.TimeUnit.MINUTES

class EmeddedElasticTest extends Specification {

    static CLUSTER_NAME_VALUE = 'plateCluster'
    static TRANSPORT_TCP_PORT_VALUE = 9850
    static ELASTIC_SERVER_PORT = 9250
    static ELASTIC_VERSION = '2.4.5'

    static EmbeddedElastic elastic = createElastic()
    static Client client = createClient()

    void index(Customer customer) {
        elastic.index(CUSTOMERS_INDEX_NAME, CUSTOMER_INDEX_TYPE, toJson(customer))
    }

    void index(Car car) {
        elastic.index(CARS_INDEX_NAME, CAR_INDEX_TYPE, toJson(car))
    }

    void assertJsonEquals(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, false)
    }

    static EmbeddedElastic createElastic() throws IOException {
        return EmbeddedElastic.builder()
                .withElasticVersion(ELASTIC_VERSION)
                .withSetting(PopularProperties.TRANSPORT_TCP_PORT, TRANSPORT_TCP_PORT_VALUE)
                .withSetting(PopularProperties.CLUSTER_NAME, CLUSTER_NAME_VALUE)
                .withSetting(PopularProperties.HTTP_PORT, ELASTIC_SERVER_PORT)
                .withEsJavaOpts('-Xms128m -Xmx512m')
                .withIndex(CUSTOMERS_INDEX_NAME, CUSTOMER_INDEX)
                .withIndex(CARS_INDEX_NAME, CAR_INDEX)
                .withStartTimeout(1, MINUTES)
                .build()
                .start()
    }

    static Client createClient() throws UnknownHostException {
        Settings settings = Settings.builder().put('cluster.name', CLUSTER_NAME_VALUE).build()
        def client = TransportClient.builder().settings(settings).build()
        client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName('127.0.0.1'), TRANSPORT_TCP_PORT_VALUE))
        return client
    }

}
