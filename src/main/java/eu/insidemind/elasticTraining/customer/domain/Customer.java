package eu.insidemind.elasticTraining.customer.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue
    private Long businessId;

    private String firstName;

    private String lastName;

    private BigDecimal balance;

}
