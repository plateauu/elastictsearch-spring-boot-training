package eu.insidemind.elasticTraining.customer

import eu.insidemind.elasticTraining.customer.domain.Customer
import eu.insidemind.elasticTraining.customer.repository.CustomerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CustomerControllerTest extends SpringTestConfig {

    @Autowired
    WebApplicationContext webctx

    MockMvc mockMvc

    def setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webctx).build()
    }

    def 'should return 200 status'() {
        expect:
        mockMvc.perform(get("/all"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
    }

    def 'should return customer list'() {
        given:
        def customer = [new Customer(businessId: 1, firstName: 'mick', lastName: 'jagger', balance: BigDecimal.valueOf(12L))] as List
        def customerRepository = Mock(CustomerRepository) {
            findAll() >> customer
        }
        def elasticService = new ElasticConfiguration().elasticService()
        def controller = new CustomerJpaController(customerRepository, elasticService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        expect:
        mockMvc.perform(get("/all")).andDo(MockMvcResultHandlers.print())
        mockMvc.perform(get("/all")).andExpect(content().json(getCustomerResponseJson()))
    }

    def 'should save customer'() {
        when:
        def request = mockMvc.perform(post("/save")
                .content(getCustomerRequestJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())

        then:
        request.andExpect(content().string("success"))
    }

    def 'should save customer into database just once'() {
        def customer = new Customer()
        def customerRepository = Mock(CustomerRepository)
        def elasticService = new ElasticConfiguration().elasticService()
        def controller = new CustomerJpaController(customerRepository, elasticService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()

        when:
        mockMvc.perform(post("/save")
                .content(getCustomerRequestJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())

        then:
        1 * customerRepository.save(_ as Customer) >> customer
    }

    def 'should return specific customer'() {
        expect:
        mockMvc.perform(get("/user/mick")).andExpect(status().isOk())
    }

    def 'should return specific customer by param request'() {
        expect:
        mockMvc.perform(get("/user?busiId=1")).andExpect(status().isOk())
    }

    def 'should delete specific customer by param request'() {
        expect:
        mockMvc.perform(get("/delete")
                .param("busiId", "1"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string('success'))
    }

    def 'should call repository delete after delete account request'() {
        def customerRepository = Mock(CustomerRepository)
        def elasticService = new ElasticConfiguration().elasticService()
        def controller = new CustomerJpaController(customerRepository, elasticService)

        when:
        controller.deleteUser(-1L)

        then:
        1 * customerRepository.delete(-1L)
    }

    def 'should call elasticService.deleteDocument  after delete request'() {
        def customerRepository = Mock(CustomerRepository)
        def elasticService = new ElasticConfiguration().elasticService()
        def controller = new CustomerJpaController(customerRepository, elasticService)

        when:
        controller.deleteUser(-1L)

        then:
        !elasticService.searchQuery(-1L).isPresent()
    }


    static getCustomerResponseJson() {
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

    static getCustomerRequestJson() {
        """
            {
                "firstName" : "mick",
                "lastName"  : "jagger",
                "balance"   : 12
            }
        """
    }

}
