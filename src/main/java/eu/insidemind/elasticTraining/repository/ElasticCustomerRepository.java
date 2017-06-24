package eu.insidemind.elasticTraining.repository;

import eu.insidemind.elasticTraining.model.Customer;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface ElasticCustomerRepository extends ElasticsearchCrudRepository<Customer, String> {
}
