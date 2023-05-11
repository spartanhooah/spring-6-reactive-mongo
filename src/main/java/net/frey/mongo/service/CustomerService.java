package net.frey.mongo.service;

import net.frey.mongo.model.CustomerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Flux<CustomerDTO> listCustomers();

    Mono<CustomerDTO> getById(String id);

    Mono<CustomerDTO> findFirstByName(String customerName);

    Mono<CustomerDTO> saveCustomer(Mono<CustomerDTO> customer);

    Mono<CustomerDTO> updateCustomer(String id, CustomerDTO customer);

    Mono<CustomerDTO> patchCustomer(String id, CustomerDTO customer);

    Mono<Void> deleteById(String id);
}
