package net.frey.mongo.service;

import lombok.RequiredArgsConstructor;
import net.frey.mongo.mapper.BeerMapper;
import net.frey.mongo.model.BeerDTO;
import net.frey.mongo.repository.BeerRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BeerServiceImpl implements BeerService {
    private final BeerMapper mapper;
    private final BeerRepository repository;

    @Override
    public Flux<BeerDTO> listBeers() {
        return repository.findAll().map(mapper::entityToDto);
    }

    @Override
    public Mono<BeerDTO> findFirstByBeerName(String beerName) {
        return repository.findFirstByBeerName(beerName).map(mapper::entityToDto);
    }

    @Override
    public Flux<BeerDTO> findByBeerStyle(String beerStyle) {
        return repository.findByBeerStyle(beerStyle).map(mapper::entityToDto);
    }

    @Override
    public Mono<BeerDTO> getById(String id) {
        return repository.findById(id).map(mapper::entityToDto);
    }

    @Override
    public Mono<BeerDTO> saveBeer(Mono<BeerDTO> beerDTO) {
        return beerDTO.map(mapper::dtoToEntity).flatMap(repository::save).map(mapper::entityToDto);
    }

    @Override
    public Mono<BeerDTO> saveBeer(BeerDTO beer) {
        return repository.save(mapper.dtoToEntity(beer)).map(mapper::entityToDto);
    }

    @Override
    public Mono<BeerDTO> updateBeer(String id, BeerDTO beer) {
        return repository
                .findById(id)
                .map(foundBeer -> {
                    foundBeer.setBeerName(beer.getBeerName());
                    foundBeer.setBeerStyle(beer.getBeerStyle());
                    foundBeer.setPrice(beer.getPrice());
                    foundBeer.setUpc(beer.getUpc());
                    foundBeer.setQuantityOnHand(beer.getQuantityOnHand());

                    return foundBeer;
                })
                .flatMap(repository::save)
                .map(mapper::entityToDto);
    }

    @Override
    public Mono<BeerDTO> patchBeer(String id, BeerDTO beer) {
        return repository
                .findById(id)
                .map(foundBeer -> {
                    if (beer.getBeerName() != null) {
                        foundBeer.setBeerName(beer.getBeerName());
                    }

                    if (beer.getBeerStyle() != null) {
                        foundBeer.setBeerStyle(beer.getBeerStyle());
                    }

                    if (beer.getUpc() != null) {
                        foundBeer.setUpc(beer.getUpc());
                    }

                    if (beer.getPrice() != null) {
                        foundBeer.setPrice(beer.getPrice());
                    }

                    return foundBeer;
                })
                .flatMap(repository::save)
                .map(mapper::entityToDto);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return repository.deleteById(id);
    }
}
