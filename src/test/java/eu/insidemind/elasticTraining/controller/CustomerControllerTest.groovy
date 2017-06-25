package eu.insidemind.elasticTraining.controller

import eu.insidemind.elasticTraining.model.Customer
import eu.insidemind.elasticTraining.repository.CustomerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@WebAppConfiguration
class CustomerControllerTest extends Specification {

    @Autowired
    WebApplicationContext webctx

    MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webctx).build()
    }

    def 'should return 200 status'() {
        expect:
        mockMvc.perform(get("/home")).andExpect(status().isOk())
    }

    def 'should return customer list'() {
        given:
        def customer = [new Customer(businessId: 1, firstName: 'mick', lastName: 'jagger', balance: BigDecimal.valueOf(12L))] as List
        def customerRepository = Mock(CustomerRepository) {
            findAll() >> customer
        }
        def controller = new CustomerController(customerRepository)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        expect:
        mockMvc.perform(get("/home")).andDo(MockMvcResultHandlers.print())
        mockMvc.perform(get("/home")).andExpect(content().json(getCustomerJson()))
    }

    static getCustomerJson() {
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
