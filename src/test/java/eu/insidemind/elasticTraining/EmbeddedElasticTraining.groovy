package eu.insidemind.elasticTraining

import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.IndexNotFoundException
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.skyscreamer.jsonassert.JSONAssert
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static eu.insidemind.elasticTraining.SampleIndices.*

class EmbeddedElasticTraining extends Specification {

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
        index(ZENEK_CUSTOMER)

        then:
        final result = client
                .prepareSearch(CUSTOMERS_INDEX_NAME)
                .setTypes(CUSTOMER_INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery('city', ZENEK_CUSTOMER.city))
                .execute().get()

        result.hits.totalHits() == 1
        assertJsonEquals(toJson(ZENEK_CUSTOMER), result.hits.hits[0].sourceAsString)
    }

    def 'should index document with id'() {
        given:
        def id = 'some-id'
        def document = toJson(ZENEK_CUSTOMER)

        when:
        elastic.index(CUSTOMERS_INDEX_NAME, CUSTOMER_INDEX_TYPE, [(id): document])

        then:
        final result = client.prepareGet(CUSTOMERS_INDEX_NAME, CUSTOMER_INDEX_TYPE, id).execute().actionGet()

        result.exists
        assertJsonEquals(result.sourceAsString, document)
    }


    def 'should recreate all index'() {
        given: 'creating indices'
        index(ZENEK_CUSTOMER)
        index(ZENEK_CAR)

        when: 'recreate all indices'
        elastic.recreateIndices()

        then:
        client.prepareSearch(CARS_INDEX_NAME, CUSTOMERS_INDEX_NAME).execute().actionGet().hits.totalHits == 0
    }

    def 'should recreate specify index'() {
        given: 'creating indices'
        index(ZENEK_CUSTOMER)
        index(ZENEK_CAR)

        when: 'recreate only cars index'
        elastic.recreateIndex(CARS_INDEX_NAME)

        then: 'cars index should be empty'
        client.prepareSearch(CARS_INDEX_NAME).execute().actionGet().hits.totalHits == 0

        and: 'customer index should not have been purged'
        client.prepareSearch(CUSTOMERS_INDEX_NAME).execute().actionGet().hits.totalHits == 1
    }

    def 'should put document in proper index and type'() {
        when: 'creating indices'
        index(ZENEK_CUSTOMER)
        index(ZENEK_CAR)

        then: 'should find car type in cars index'
        def result = client.prepareSearch(CARS_INDEX_NAME)
                .setTypes(CAR_INDEX_TYPE)
                .execute().actionGet()

        result.hits.hits.size() == 1
        assertJsonEquals(toJson(ZENEK_CAR), result.hits.hits[0].sourceAsString)

        and: 'should find customer type in customers index'
        def customerResult = client.prepareSearch(CUSTOMERS_INDEX_NAME)
                .setTypes(CUSTOMER_INDEX_TYPE)
                .execute().actionGet()

        customerResult.hits.hits.size() == 1
        assertJsonEquals(toJson(ZENEK_CUSTOMER), customerResult.hits.hits[0].sourceAsString)
    }

    def 'should delete proper index'() {
        given:
        index(ZENEK_CAR)
        index(ZENEK_CUSTOMER)
        assert client.prepareSearch(CARS_INDEX_NAME).setTypes(CAR_INDEX_TYPE).execute().actionGet().hits.hits.size() == 1:'Invalid index size'
        assert client.prepareSearch(CUSTOMERS_INDEX_NAME).setTypes(CUSTOMER_INDEX_TYPE).execute().actionGet().hits.hits.size() == 1:'Invalid index size'

        elastic.deleteIndex(CARS_INDEX_NAME)
        when:
        client.prepareSearch(CARS_INDEX_NAME).setTypes(CAR_INDEX_TYPE).execute().actionGet()

        then:
        thrown(IndexNotFoundException)

        and:
        client.prepareSearch(CUSTOMERS_INDEX_NAME).setTypes(CUSTOMER_INDEX_TYPE).execute().actionGet().hits.hits.size() == 1
    }


    def 'should delete all indices'() {
        given:
        index(ZENEK_CAR)
        index(ZENEK_CUSTOMER)
        assert client.prepareSearch(CARS_INDEX_NAME).setTypes(CAR_INDEX_TYPE).execute().actionGet().hits.hits.size() == 1:'Invalid index size'
        assert client.prepareSearch(CUSTOMERS_INDEX_NAME).setTypes(CUSTOMER_INDEX_TYPE).execute().actionGet().hits.hits.size() == 1:'Invalid index size'

        when:
        elastic.deleteIndices()

        then:
        client.prepareSearch().execute().actionGet().hits.hits.size() == 0
    }


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
                .withEsJavaOpts('-Xms128m -Xmx512m')
                .withIndex(CUSTOMERS_INDEX_NAME, CUSTOMER_INDEX)
                .withIndex(CARS_INDEX_NAME, CAR_INDEX)
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
