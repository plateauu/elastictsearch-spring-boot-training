package eu.insidemind.elasticTraining.customer.repository;

import eu.insidemind.elasticTraining.customer.domain.Customer;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface ElasticCustomerRepository extends ElasticsearchCrudRepository<Customer, String> {
}
