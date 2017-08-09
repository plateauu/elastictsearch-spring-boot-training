package eu.insidemind.elasticTraining.customer;

import eu.insidemind.elasticTraining.customer.domain.Customer;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
class ElasticService {

    private static final String CUSTOMER_INDEX_NAME = "customers";
    private static final String CUSTOMER_INDEX_TYPE = "customer";

    private final Logger log = LoggerFactory.getLogger(ElasticService.class);

    private final ElasticsearchOperations elasticsearchTemplate;
    private final JsonManager jsonManager;

    ElasticService(ElasticsearchOperations elasticsearchTemplate, JsonManager jsonManager) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.jsonManager = jsonManager;
        createIndex(CUSTOMER_INDEX_NAME, ClassLoader.getSystemResourceAsStream("elastic-settings.json"));
    }

    boolean indexDocument(Customer customer) {
        try {
            String serializedCustomer = jsonManager.serialize(customer);
            log.info("Creating new document for customer {} : {}", customer, serializedCustomer);
            return getClient().prepareIndex(CUSTOMER_INDEX_NAME, CUSTOMER_INDEX_TYPE)
                    .setSource(serializedCustomer)
                    .execute()
                    .get()
                    .isCreated();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    void deleteDocument(Long businessId) {
        searchQuery(businessId).ifPresent(this::toDelete);
    }

    private boolean toDelete(String id) {
        log.info("Deleting customer document for id {}", id);
        return getClient()
                .prepareDelete(CUSTOMER_INDEX_NAME, CUSTOMER_INDEX_TYPE, id)
                .get().isFound();
    }

    private Client getClient() {
        return elasticsearchTemplate.getClient();
    }

    private void createIndex(String name, Object settings) {
        if (elasticsearchTemplate.indexExists(name)) {
            elasticsearchTemplate.deleteIndex(name);
        }
        log.info("Creating new index: [{}]", name);
        elasticsearchTemplate.createIndex(name, settings);
    }

    private Optional<String> searchQuery(Long businessId) {
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
