package eu.insidemind.elasticTraining

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.index.query.QueryBuilders

import static eu.insidemind.elasticTraining.SampleIndices.*

class ElastincClientTest extends EmeddedElasticTest {

    private def objectMapper = new ObjectMapper()

    def setup() {
        elastic.recreateIndices()
    }

    def cleanupSpec() {
        client.close()
        elastic.stop()
    }

    def 'should index data by client'() {
        def json = toJson(new Car(model: 'mondeo', brand: 'ford', owner: 'jaaa'))

        when:
        def indexResponse = client.prepareIndex(CARS_INDEX_NAME, CAR_INDEX_TYPE, '1')
                .setSource(json).get()

        then:
        indexResponse.index == CARS_INDEX_NAME
        indexResponse.type == CAR_INDEX_TYPE
        indexResponse.id == '1'
    }

    def 'should get the same data from elastic'() {
        def json = toJson(new Car(model: 'mondeo', brand: 'ford', owner: 'jaaa'))

        def indexResponse = client.prepareIndex(CARS_INDEX_NAME, CAR_INDEX_TYPE, '1')
                .setSource(json).get()

        when:
        def response = client.prepareGet()
                .setIndex(CARS_INDEX_NAME)
                .setId('1')
                .setType(CAR_INDEX_TYPE).get()

        then:
        response.exists
        response.sourceAsString == json
    }

    def 'should get the same data from elastic after deserialize it by object mapper'() {
        def car = new Car(model: 'mondeo', brand: 'ford', owner: 'jaaa')

        client.prepareIndex(CARS_INDEX_NAME, CAR_INDEX_TYPE, '1')
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                            .field('model', car.model)
                            .field('brand', car.brand)
                            .field('owner', car.owner)
                        .endObject()
        ).get()

        when:
        def response = client.prepareGet()
                .setIndex(CARS_INDEX_NAME)
                .setId('1')
                .setType(CAR_INDEX_TYPE).get()

        def mappedCar = objectMapper.readValue(response.sourceAsString, Car.class)

        then:
        car == mappedCar
    }


}
