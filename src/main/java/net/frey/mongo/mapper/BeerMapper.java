package net.frey.mongo.mapper;

import net.frey.mongo.domain.Beer;
import net.frey.mongo.model.BeerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {
    BeerDTO entityToDto(Beer beer);

    Beer dtoToEntity(BeerDTO beer);
}
