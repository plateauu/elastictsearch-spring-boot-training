package eu.insidemind.elasticTraining.customer

import eu.insidemind.elasticTraining.customer.domain.Customer
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import spock.lang.Shared

import java.util.concurrent.TimeUnit

class ElasticServiceTest extends SpringTestConfig {

    @Shared
    ElasticsearchTemplate elasticsearchTemplate

    @Shared
    ElasticService elasticService

    def 'should index document when customer is passed'() {
        def customer = new Customer(businessId: -1L, firstName: 'zenon', lastName: 'zenkiewicz', balance: BigDecimal.ONE)

        when:
        def isCreated = elasticService.indexDocument(customer)

        then:
        isCreated
        TimeUnit.SECONDS.sleep(4)
        elasticsearchTemplate.getClient()
                .prepareSearch(elasticService.CUSTOMER_INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .get().hits.hits.size() == 1

    }

    def getCustomerAsString() {
        """
        [
            {
                "businessId": 1,
                "firstName" : "mick",
                "lastName"  : "jagger",
                "balance"   : 12
            }
        ]
        """
    }
}
