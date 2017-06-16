package eu.insidemind.elasticTraining

import groovy.transform.Immutable
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.skyscreamer.jsonassert.JSONAssert
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.IndexSettings
import pl.allegro.tech.embeddedelasticsearch.PopularProperties
import spock.lang.Specification

import java.util.concurrent.TimeUnit

import static java.lang.ClassLoader.getSystemResourceAsStream

class ElasticTrainingApplicationTest extends Specification {

    static CUSTOMERS_INDEX_NAME = "customers"
    static CUSTOMER_INDEX_TYPE = "cust"
    static CLUSTER_NAME_VALUE = 'plate_testing_cluster'
    static TRANSPORT_TCP_PORT_VALUE = 9850

    static CUSTOMER_INDEX = createIndex(CUSTOMERS_INDEX_NAME, getSystemResourceAsStream("customer-mapping.json"))

    static EmbeddedElastic elastic = createElastic()

    static Client client = createClient()

    def setup() {
        elastic.recreateIndices()
    }

    def cleanupSpec() {
        elastic.stop()
        client.close()
    }

    def "should index document"() {
        when:
        elastic.index(CUSTOMERS_INDEX_NAME, CUSTOMER_INDEX_TYPE, toJson(ZENEK_CUSTOMER))

        then:
        final result = client
                .prepareSearch(CUSTOMERS_INDEX_NAME)
                .setTypes(CUSTOMER_INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery('city', ZENEK_CUSTOMER.city))
                .execute().actionGet()

        result.hits.totalHits() == 1
        assertJsonEquals(toJson(ZENEK_CUSTOMER), result.hits.hits[0].sourceAsString)
    }

    static String toJson(Customer customer) {
        """
        {
            "account_number": "$customer.accountNumber",
            "balance": "$customer.balance",
            "firstname":"$customer.firstName",
            "lastname":"$customer.lastName",
            "age":"$customer.age",
            "address":"$customer.address",
            "employer":"$customer.employer",
            "email":"$customer.email",
            "state":"$customer.state",
            "city":"$customer.city",
        }
        """
    }

    static final ZENEK_CUSTOMER = new Customer(
            accountNumber: 11111,
            balance: 3333,
            firstName: 'zenek',
            lastName: 'pospieszalski',
            age: 32,
            address: 'Warsaw, Mokotów',
            employer: 'Pyrami',
            email: 'email@dot.con',
            state: 'mazowieckie',
            city: 'Warsaw')

    @Immutable
    static class Customer {
        int accountNumber
        int balance
        String firstName
        String lastName
        int age
        String address
        String employer
        String email
        String state
        String city
    }

    void assertJsonEquals(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, false)
    }

    static EmbeddedElastic createElastic() throws IOException {
        return EmbeddedElastic.builder()
                .withElasticVersion("5.0.0")
                .withSetting(PopularProperties.TRANSPORT_TCP_PORT, TRANSPORT_TCP_PORT_VALUE)
                .withSetting(PopularProperties.CLUSTER_NAME, CLUSTER_NAME_VALUE)
                .withEsJavaOpts("-Xms128m -Xmx512m")
//                .withPlugin("analysis-stempel")
                .withIndex(CUSTOMERS_INDEX_NAME, CUSTOMER_INDEX)
                .withStartTimeout(1, TimeUnit.MINUTES)
                .build()
                .start()
    }

    static Client createClient() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", CLUSTER_NAME_VALUE).build()
        return new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), TRANSPORT_TCP_PORT_VALUE))
    }

    static IndexSettings createIndex(String indexName, InputStream mapping) {
        IndexSettings.builder()
                .withType(indexName, mapping)
                .withSettings(getSystemResourceAsStream("elastic-settings.json"))
                .build()
    }
}
