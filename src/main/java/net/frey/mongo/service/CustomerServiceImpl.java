package net.frey.mongo.service;

import lombok.RequiredArgsConstructor;
import net.frey.mongo.mapper.CustomerMapper;
import net.frey.mongo.model.CustomerDTO;
import net.frey.mongo.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerMapper mapper;
    private final CustomerRepository repository;

    @Override
    public Flux<CustomerDTO> listCustomers() {
        return repository.findAll().map(mapper::entityToDto);
    }

    @Override
    public Mono<CustomerDTO> getById(String id) {
        return repository.findById(id).map(mapper::entityToDto);
    }

    @Override
    public Mono<CustomerDTO> findFirstByName(String customerName) {
        return repository.findFirstByCustomerName(customerName).map(mapper::entityToDto);
    }

    @Override
    public Mono<CustomerDTO> saveCustomer(Mono<CustomerDTO> customerDto) {
        return customerDto.map(mapper::dtoToEntity).flatMap(repository::save).map(mapper::entityToDto);
    }

    @Override
    public Mono<CustomerDTO> updateCustomer(String id, CustomerDTO customer) {
        return repository
                .findById(id)
                .map(foundCustomer -> {
                    foundCustomer.setCustomerName(customer.getCustomerName());

                    return foundCustomer;
                })
                .flatMap(repository::save)
                .map(mapper::entityToDto);
    }

    @Override
    public Mono<CustomerDTO> patchCustomer(String id, CustomerDTO customer) {
        return repository
                .findById(id)
                .map(foundCustomer -> {
                    if (customer.getCustomerName() != null) {
                        foundCustomer.setCustomerName(customer.getCustomerName());
                    }

                    return foundCustomer;
                })
                .flatMap(repository::save)
                .map(mapper::entityToDto);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }
}
