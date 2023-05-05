package net.frey.mongo.mapper;

import net.frey.mongo.domain.Customer;
import net.frey.mongo.model.CustomerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {
    CustomerDTO entityToDto(Customer customer);
    Customer dtoToEntity(CustomerDTO customer);
}
