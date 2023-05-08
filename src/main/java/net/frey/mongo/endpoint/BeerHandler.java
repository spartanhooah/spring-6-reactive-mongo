package net.frey.mongo.endpoint;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.created;
import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import lombok.RequiredArgsConstructor;
import net.frey.mongo.model.BeerDTO;
import net.frey.mongo.service.BeerService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BeerHandler {
    public static final String BEER_PATH = "/api/v3/beer";
    public static final String ID_PATH = BEER_PATH + "/{id}";

    private final BeerService service;

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
        return ok().body(service.listBeers(), BeerDTO.class);
    }

    public Mono<ServerResponse> getBeerById(ServerRequest request) {
        return ok().body(service.getById(request.pathVariable("id")), BeerDTO.class);
    }

    public Mono<ServerResponse> createBeer(ServerRequest request) {
        return service.saveBeer(request.bodyToMono(BeerDTO.class))
                .flatMap(beerDTO -> created(fromPath(ID_PATH).build(beerDTO.getId()))
                        .build());
    }

    public Mono<ServerResponse> updateBeer(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .map(dto -> service.updateBeer(request.pathVariable("id"), dto))
                .flatMap(savedDto -> noContent().build());
    }

    public Mono<ServerResponse> patchBeer(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .map(dto -> service.patchBeer(request.pathVariable("id"), dto))
                .flatMap(savedDto -> noContent().build());
    }

    public Mono<ServerResponse> deleteBeer(ServerRequest request) {
        return service.deleteById(request.pathVariable("id")).then(noContent().build());
    }
}
