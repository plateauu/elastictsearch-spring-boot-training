package eu.insidemind.elasticTraining.customer;

import eu.insidemind.elasticTraining.customer.domain.Customer;
import eu.insidemind.elasticTraining.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
class CustomerJpaController {

    private final Logger log = LoggerFactory.getLogger(CustomerJpaController.class);

    private final CustomerRepository repository;

    private final ElasticService elasticService;

    @Autowired
    public CustomerJpaController(CustomerRepository repository, ElasticService elasticService) {
        this.repository = repository;
        this.elasticService = elasticService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public List<Customer> all() {
        log.info("REST API: Fetch all customers");
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/save")
    public String save(@RequestBody Customer customer) {
        log.info("REST API: saving customer: {}", customer);
        Customer saved = repository.save(customer);
        if(elasticService.indexDocument(saved)){
            return "success";
        }
        return "fail";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{firstName}")
    public Customer getCustomer(@PathVariable  String firstName) {
        log.info("REST API: fetching customer with name: {}", firstName);
        return repository.findByFirstName(firstName).orElse(null);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user")
    public Customer getCustomerByParam(@RequestParam("busiId") long businessId) {
        log.info("REST API: fetching customer by id: {}", businessId);
        return repository.findOne(businessId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/delete")
    public String deleteUser(@RequestParam("busiId") long businessId) {
        log.info("REST API: delete customer by id: {}", businessId);
        repository.delete(businessId);
        elasticService.deleteDocument(businessId);
        return "success";
    }
}
