package eu.insidemind.elasticTraining

import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.skyscreamer.jsonassert.JSONAssert
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class ElasticTrainingApplicationTest extends Specification {

    static CLUSTER_NAME_VALUE = 'plateCluster'
    static TRANSPORT_TCP_PORT_VALUE = 9850
    static ELASTIC_VERSION = '5.0.0'

    static EmbeddedElastic elastic = createElastic()

    static Client client = createClient()

    def setup() {
        elastic.recreateIndices()
    }

    def cleanupSpec() {
        client.close()
        elastic.stop()
    }

    def 'should index document'() {
        when:
        elastic.index(SampleIndices.CUSTOMERS_INDEX_NAME, SampleIndices.CUSTOMER_INDEX_TYPE, SampleIndices.toJson(SampleIndices.ZENEK_CUSTOMER))

        then:
        final result = client
                .prepareSearch(SampleIndices.CUSTOMERS_INDEX_NAME)
                .setTypes(SampleIndices.CUSTOMER_INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery('city', SampleIndices.ZENEK_CUSTOMER.city))
                .execute().get()

        result.hits.totalHits() == 1
        assertJsonEquals(SampleIndices.toJson(SampleIndices.ZENEK_CUSTOMER), result.hits.hits[0].sourceAsString)
    }


    void assertJsonEquals(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, false)
    }

    static EmbeddedElastic createElastic() throws IOException {
        return EmbeddedElastic.builder()
                .withElasticVersion(ELASTIC_VERSION)
                .withSetting(PopularProperties.TRANSPORT_TCP_PORT, TRANSPORT_TCP_PORT_VALUE)
                .withSetting(PopularProperties.CLUSTER_NAME, CLUSTER_NAME_VALUE)
                .withEsJavaOpts('-Xms128m -Xmx512m')
                .withIndex(SampleIndices.CUSTOMERS_INDEX_NAME, SampleIndices.CUSTOMER_INDEX)
                .withStartTimeout(1, TimeUnit.MINUTES)
                .build()
                .start()
    }

    static Client createClient() throws UnknownHostException {
        Settings settings = Settings.builder().put('cluster.name', CLUSTER_NAME_VALUE).build()
        return new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName('127.0.0.1'), TRANSPORT_TCP_PORT_VALUE))
    }

}
