package eu.insidemind.elasticTraining.customer

import eu.insidemind.elasticTraining.customer.domain.Customer
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate

import java.util.concurrent.TimeUnit

class ElasticServiceTest extends SpringTestConfig {

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate

    @Autowired
    ElasticService elasticService

    @Autowired
    JsonManager jsonManager

    def cleanup() {
        elasticsearchTemplate.deleteIndex(elasticService.CUSTOMER_INDEX_NAME)
        elasticsearchTemplate.createIndex(elasticService.CUSTOMER_INDEX_NAME)
    }

    def 'should index document when customer is passed'() {
        given:
        def customer = new Customer(businessId: -1L, firstName: 'zenon', lastName: 'zenkiewicz', balance: BigDecimal.ONE)

        when:
        def isCreated = elasticService.indexDocument(customer)

        then:
        isCreated
        TimeUnit.SECONDS.sleep(1)
        def hits = elasticsearchTemplate.getClient()
                .prepareSearch(elasticService.CUSTOMER_INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .get().hits.hits

        hits.size() == 1
        jsonManager.deserialize(hits[0].sourceAsString()) == customer
    }

    def 'should delete document when business id is passed'() {
        given:
        def customer = new Customer(businessId: -23L, firstName: 'mietek', lastName: 'mietkiewicz', balance: BigDecimal.TEN)
        elasticService.indexDocument(customer)
        TimeUnit.SECONDS.sleep(1)

        when:
        elasticService.deleteDocument(-23L)
        elasticsearchTemplate.refresh(elasticService.CUSTOMER_INDEX_NAME)
        TimeUnit.SECONDS.sleep(1)

        then:
        elasticsearchTemplate.getClient()
                .prepareSearch(elasticService.CUSTOMER_INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .get().hits.hits().size() == 0
    }
}
