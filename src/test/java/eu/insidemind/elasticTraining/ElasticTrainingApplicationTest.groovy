package eu.insidemind.elasticTraining

import groovy.transform.Immutable
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic
import pl.allegro.tech.embeddedelasticsearch.IndexSettings
import spock.lang.Shared
import spock.lang.Specification

import static java.lang.ClassLoader.getSystemResourceAsStream

@SpringBootTest
class ElasticTrainingApplicationTest extends Specification {

    @Autowired
    EmbeddedElastic elastic

    @Autowired
    Client client

    @Shared
    EmbeddedElastic server

    static CUSTOMERS_INDEX_NAME = 'customers'
    static CUSTOMER_INDEX_TYPE = 'customer'
    static CUSTOMER_INDEX = IndexSettings.builder()
            .withType(CUSTOMERS_INDEX_NAME, getSystemResourceAsStream("customer-mapping.json"))
            .build()


    def setup() {
        server = elastic.start()
        server.createIndex(CUSTOMERS_INDEX_NAME)
        server.recreateIndices()
    }

    def "should index document"() {
        when:
        server.index(CUSTOMERS_INDEX_NAME, CUSTOMER_INDEX_TYPE, toJson(ZENEK_CUSTOMER))

        then:
        final result = client
                .prepareSearch(CUSTOMERS_INDEX_NAME)
                .setTypes(CUSTOMER_INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery("city", ZENEK_CUSTOMER.city))
                .execute().actionGet()

        result.hits.totalHits() == 1
        assertJsonEquals(toJson(ZENEK_CUSTOMER), result.hits.hits[0].sourceAsString)
    }

    static String toJson(Customer customer) {
        """
        {
            "account_number": "$customer.accountNumber",
            "balance": "$customer.balance",
            "firstName":"$customer.firstName",
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

    void assertJsonEquals(String expected, String actual) {
        JSONAssert.assertEquals(expected, actual, false)
    }
}
