package eu.insidemind.elasticTraining.controller;

import eu.insidemind.elasticTraining.model.Customer;
import eu.insidemind.elasticTraining.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CustomerController {

    CustomerRepository repository;

    @Autowired
    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/home")
    public List<Customer> all() {
        return repository.findAll();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/save")
    public String save(@RequestBody Customer customer) {
        repository.save(customer);
        return "success";
    }
}
