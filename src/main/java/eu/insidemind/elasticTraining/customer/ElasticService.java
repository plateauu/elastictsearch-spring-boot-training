package eu.insidemind.elasticTraining.customer;

import eu.insidemind.elasticTraining.customer.domain.Customer;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
class ElasticService {

    private final Logger log = LoggerFactory.getLogger(ElasticService.class);

    private static final String CUSTOMER_INDEX_NAME = "customers";
    private static final String CUSTOMER_INDEX_TYPE = "customer";

    private final ElasticsearchOperations elasticsearchTemplate;

    private final JsonManager jsonManager;

    ElasticService(ElasticsearchTemplate elasticsearchTemplate, JsonManager jsonManager) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.jsonManager = jsonManager;
        createIndex(CUSTOMER_INDEX_NAME, ClassLoader.getSystemResourceAsStream("elastic-settings.json"));
    }

    private Client getClient() {
        return elasticsearchTemplate.getClient();
    }

    private void createIndex(String name, Object settings) {
        if (elasticsearchTemplate.indexExists(CUSTOMER_INDEX_NAME)) {
            elasticsearchTemplate.deleteIndex(CUSTOMER_INDEX_NAME);
        }
        log.info("Creating new index: [{}]", name);
        elasticsearchTemplate.createIndex(name, settings);
    }

    boolean indexDocument(Customer customer) {
        log.info("Creating new document for customer {}", customer);
        return getClient().prepareIndex(CUSTOMER_INDEX_NAME, CUSTOMER_INDEX_TYPE)
                .setSource(jsonManager.serialize(customer))
                .get()
                .isCreated();
    }

    void deleteDocument(Long businessId) {
        foundToDelete(businessId).ifPresent(id -> {
                    getClient()
                            .prepareDelete(CUSTOMER_INDEX_NAME, CUSTOMER_INDEX_TYPE, id)
                            .get().isFound();
                }
        );
    }

    private Optional<String> foundToDelete(Long businessId) {
        return Arrays.stream(getClient().prepareSearch(CUSTOMER_INDEX_NAME)
                .setQuery(QueryBuilders.termQuery("businessId", businessId))
                .execute()
                .actionGet()
                .getHits()
                .hits())
                .findFirst()
                .map(SearchHit::getId);
    }


}
