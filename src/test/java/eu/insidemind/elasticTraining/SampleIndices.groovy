package eu.insidemind.elasticTraining

import groovy.transform.Immutable
import pl.allegro.tech.embeddedelasticsearch.IndexSettings

class SampleIndices {

    protected static CUSTOMERS_INDEX_NAME = 'customers'
    protected static CUSTOMER_INDEX_TYPE = 'customer'
    protected static CUSTOMER_INDEX = IndexSettings.builder()
            .withType(CUSTOMER_INDEX_TYPE, ClassLoader.getSystemResourceAsStream('customer-mapping.json'))
            .withSettings(ClassLoader.getSystemResourceAsStream('elastic-settings.json'))
            .build()

    static final ZENEK_CUSTOMER = new Customer(
            accountNumber: 11111, balance: 3333, firstName: 'Zenek',
            lastName: 'pospieszalski', gender: "M", age: 32, address: 'Mokotów',
            employer: 'Pyrami', email: 'email@dot.con', state: 'mazowieckie', city: 'Warsaw')

    static String toJson(Customer customer) {
        """
        {
            "account_number": "$customer.accountNumber",
            "balance": "$customer.balance",
            "firstname":"$customer.firstName",
            "lastname":"$customer.lastName",
            "age":"$customer.age",
            "gender":"$customer.gender",
            "address":"$customer.address",
            "employer":"$customer.employer",
            "email":"$customer.email",
            "state":"$customer.state",
            "city":"$customer.city"
        }
        """
    }


    protected static CARS_INDEX_NAME = 'cars'
    protected static CAR_INDEX_TYPE = 'car'
    protected static CAR_INDEX = IndexSettings.builder()
            .withType(CAR_INDEX_TYPE, ClassLoader.getSystemResourceAsStream('car-mapping.json'))
            .withSettings(ClassLoader.getSystemResourceAsStream('elastic-settings.json'))
            .build()

    protected static final ZENEK_CAR = new Car(
            model: 'Cinquocento', brand: 'FIAT', owner: 'ZENEK')


    static String toJson(Car car) {
        """
        {
            "domain": "$car.model",
            "brand": "$car.brand",
            "owner":"$car.owner"
        }
        """
    }

    @Immutable
    static class Car {
        String model
        String brand
//        String productionDate
        String owner
    }

    @Immutable
    static class Customer {
        int accountNumber
        int balance
        String firstName
        String lastName
        int age
        String gender
        String address
        String employer
        String email
        String state
        String city
    }
}
