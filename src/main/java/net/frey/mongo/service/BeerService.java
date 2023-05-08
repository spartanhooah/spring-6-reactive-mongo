package net.frey.mongo.service;

import net.frey.mongo.model.BeerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BeerService {
    Flux<BeerDTO> listBeers();

    Mono<BeerDTO> getById(String id);

    Mono<BeerDTO> findFirstByBeerName(String beerName);

    Flux<BeerDTO> findByBeerStyle(String beerStyle);

    Mono<BeerDTO> saveBeer(Mono<BeerDTO> beer);

    Mono<BeerDTO> saveBeer(BeerDTO beer);

    Mono<BeerDTO> updateBeer(String id, BeerDTO beer);

    Mono<BeerDTO> patchBeer(String id, BeerDTO beer);

    Mono<Void> deleteById(String id);
}
