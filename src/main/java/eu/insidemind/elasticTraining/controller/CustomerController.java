package eu.insidemind.elasticTraining.controller;

import eu.insidemind.elasticTraining.model.Customer;
import eu.insidemind.elasticTraining.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CustomerController {

    private final Logger log = LoggerFactory.getLogger(CustomerController.class);

    CustomerRepository repository;

    @Autowired
    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/home")
    public List<Customer> all() {
        log.info("REST API: Fetch all customers");
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/save")
    public String save(@RequestBody Customer customer) {
        log.info("REST API: saving customer: {}", customer);
        repository.save(customer);
        return "success";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{firstName}")
    public Customer getCustomer(@PathVariable  String firstName) {
        log.info("REST API: fetching customer with name: {}", firstName);
        return repository.findByFirstName(firstName).orElse(null);
    }
}
