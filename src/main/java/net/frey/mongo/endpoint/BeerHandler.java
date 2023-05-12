package net.frey.mongo.endpoint;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static reactor.core.publisher.Mono.error;

import lombok.RequiredArgsConstructor;
import net.frey.mongo.model.BeerDTO;
import net.frey.mongo.service.BeerService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BeerHandler {
    public static final String BEER_PATH = "/api/v3/beer";
    public static final String ID_PATH = BEER_PATH + "/{id}";

    private final BeerService service;
    private final Validator validator;

    @Bean
    public RouterFunction<ServerResponse> beerRoutes() {
        return route().GET(BEER_PATH, this::listBeers)
                .GET(ID_PATH, this::getBeerById)
                .POST(BEER_PATH, accept(APPLICATION_JSON), this::createBeer)
                .PUT(ID_PATH, accept(APPLICATION_JSON), this::updateBeer)
                .PATCH(ID_PATH, accept(APPLICATION_JSON), this::patchBeer)
                .DELETE(ID_PATH, this::deleteBeer)
                .build();
    }

    public Mono<ServerResponse> listBeers(ServerRequest request) {
        Flux<BeerDTO> beerFlux =
                request.queryParam("style").map(service::findByBeerStyle).orElseGet(service::listBeers);

        return ok().body(beerFlux, BeerDTO.class);
    }

    public Mono<ServerResponse> getBeerById(ServerRequest request) {
        return ok().body(
                        service.getById(request.pathVariable("id"))
                                .switchIfEmpty(error(new ResponseStatusException(NOT_FOUND))),
                        BeerDTO.class);
    }

    public Mono<ServerResponse> createBeer(ServerRequest request) {
        return service.saveBeer(request.bodyToMono(BeerDTO.class))
                .doOnNext(this::validate)
                .flatMap(beerDTO ->
                        created(fromPath(ID_PATH).build(beerDTO.getId())).build());
    }

    public Mono<ServerResponse> updateBeer(ServerRequest request) {
        String id = request.pathVariable("id");

        return request.bodyToMono(BeerDTO.class)
                .doOnNext(this::validate)
                .map(dto -> service.updateBeer(id, dto))
                .switchIfEmpty(error(new ResponseStatusException(NOT_FOUND)))
                .flatMap(savedDto -> noContent().build());
    }

    public Mono<ServerResponse> patchBeer(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .doOnNext(this::validate)
                .flatMap(dto -> service.patchBeer(request.pathVariable("id"), dto))
                .switchIfEmpty(error(new ResponseStatusException(NOT_FOUND)))
                .flatMap(savedDto -> noContent().build());
    }

    public Mono<ServerResponse> deleteBeer(ServerRequest request) {
        return service.getById(request.pathVariable("id"))
                .switchIfEmpty(error(new ResponseStatusException(NOT_FOUND)))
                .flatMap(dto -> service.deleteById(dto.getId()).then(noContent().build()));
    }

    private void validate(BeerDTO dto) {
        Errors errors = new BeanPropertyBindingResult(dto, "beerDto");

        validator.validate(dto, errors);

        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}
