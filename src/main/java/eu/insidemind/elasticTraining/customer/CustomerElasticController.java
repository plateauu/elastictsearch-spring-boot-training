package eu.insidemind.elasticTraining.customer;

import eu.insidemind.elasticTraining.customer.domain.Customer;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
class CustomerElasticController {

    private final ElasticsearchOperations elasticTemplate;

    private final JsonManager jsonManager;

    CustomerElasticController(ElasticsearchOperations elasticTemplate, JsonManager jsonManager) {
        this.elasticTemplate = elasticTemplate;
        this.jsonManager = jsonManager;
    }

    @RequestMapping(value = "/elastic", method = RequestMethod.GET)
    public List<Customer> matchAll() {
        SearchResponse searchResponse = elasticTemplate.getClient().prepareSearch().execute().actionGet();
        return Stream.of(searchResponse.getHits().getHits()).map(SearchHit::getSourceAsString)
                .map(jsonManager::deserialize)
                .collect(Collectors.toList());
    }
}