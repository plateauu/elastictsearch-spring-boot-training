package eu.insidemind.elasticTraining.customer

import org.elasticsearch.index.IndexNotFoundException
import org.elasticsearch.index.query.QueryBuilders

import static SampleIndices.*

class EmbeddedElasticTraining extends EmeddedElasticTest {

    def setup() {
        elastic.recreateIndices()
    }

    def 'should index document'() {
        when:
        index(ZENEK_CUSTOMER)

        then:
        final result = client
                .prepareSearch(CUSTOMERS_INDEX_NAME)
                .setTypes(CUSTOMER_INDEX_TYPE)
                .setQuery(QueryBuilders.matchQuery('city', ZENEK_CUSTOMER.city))
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
        assert client.prepareSearch(CARS_INDEX_NAME).setTypes(CAR_INDEX_TYPE).execute().actionGet().hits.hits.size() == 1: 'Invalid index size'
        assert client.prepareSearch(CUSTOMERS_INDEX_NAME).setTypes(CUSTOMER_INDEX_TYPE).execute().actionGet().hits.hits.size() == 1: 'Invalid index size'

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
        assert client.prepareSearch(CARS_INDEX_NAME).setTypes(CAR_INDEX_TYPE).execute().actionGet().hits.hits.size() == 1: 'Invalid index size'
        assert client.prepareSearch(CUSTOMERS_INDEX_NAME).setTypes(CUSTOMER_INDEX_TYPE).execute().actionGet().hits.hits.size() == 1: 'Invalid index size'

        when:
        elastic.deleteIndices()

        then:
        client.prepareSearch().execute().actionGet().hits.hits.size() == 0
    }

}
