package org.sud.mod3.repository;

import org.sud.mod3.model.Customer;

import java.util.Map;
import java.util.Optional;

/** In-memory customer data used by the example tools. */
public class CustomerRepository
{
    private final Map<String, Customer> customers = Map.of(
            "CUST-101", new Customer("CUST-101", "Avery Chen", Customer.Status.ACTIVE,
                    Customer.RiskLevel.LOW),
            "CUST-202", new Customer("CUST-202", "Jordan Patel", Customer.Status.ACTIVE,
                    Customer.RiskLevel.LOW),
            "CUST-303", new Customer("CUST-303", "Morgan Lee", Customer.Status.ACTIVE,
                    Customer.RiskLevel.MEDIUM),
            "CUST-404", new Customer("CUST-404", "Taylor Smith", Customer.Status.SUSPENDED,
                    Customer.RiskLevel.HIGH)
    );

    public Optional<Customer> findById(String customerId)
    {
        return Optional.ofNullable(customers.get(customerId));
    }
}
