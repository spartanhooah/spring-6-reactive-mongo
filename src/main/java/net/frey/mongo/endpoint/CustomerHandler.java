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

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import net.frey.mongo.model.CustomerDTO;
import net.frey.mongo.service.CustomerService;
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
public class CustomerHandler {
    public static final String CUSTOMER_PATH = "/api/v3/customer";
    public static final String ID_PATH = CUSTOMER_PATH + "/{id}";

    private final CustomerService service;
    private final Validator validator;

    @Bean
    public RouterFunction<ServerResponse> customerRoutes() {
        return route().GET(CUSTOMER_PATH, this::listCustomers)
                .GET(ID_PATH, this::getCustomerById)
                .POST(CUSTOMER_PATH, accept(APPLICATION_JSON), this::createCustomer)
                .PUT(ID_PATH, accept(APPLICATION_JSON), this::updateCustomer)
                .PATCH(ID_PATH, accept(APPLICATION_JSON), this::patchCustomer)
                .DELETE(ID_PATH, this::deleteCustomer)
                .build();
    }

    private Mono<ServerResponse> listCustomers(ServerRequest request) {

        Optional<String> nameParam = request.queryParam("name");

        Flux<CustomerDTO> customerFlux =
                nameParam.map(name -> service.findFirstByName(name).flux()).orElseGet(service::listCustomers);

        return ok().body(customerFlux, CustomerDTO.class);
    }

    private Mono<ServerResponse> getCustomerById(ServerRequest request) {
        return ok().body(
                        service.getById(request.pathVariable("id"))
                                .switchIfEmpty(error(new ResponseStatusException(NOT_FOUND))),
                        CustomerDTO.class);
    }

    private Mono<ServerResponse> updateCustomer(ServerRequest request) {
        String id = request.pathVariable("id");

        return request.bodyToMono(CustomerDTO.class)
                .doOnNext(this::validate)
                .flatMap(findDto -> service.getById(id))
                .map(dto -> service.updateCustomer(id, dto))
                .switchIfEmpty(error(new ResponseStatusException(NOT_FOUND)))
                .flatMap(savedDto -> noContent().build());
    }

    private Mono<ServerResponse> createCustomer(ServerRequest request) {
        return service.saveCustomer(request.bodyToMono(CustomerDTO.class))
                .doOnNext(this::validate)
                .flatMap(customerDTO ->
                        created(fromPath(ID_PATH).build(customerDTO.getId())).build());
    }

    private Mono<ServerResponse> patchCustomer(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .doOnNext(this::validate)
                .flatMap(dto -> service.patchCustomer(request.pathVariable("id"), dto))
                .switchIfEmpty(error(new ResponseStatusException(NOT_FOUND)))
                .flatMap(savedDto -> noContent().build());
    }

    private Mono<ServerResponse> deleteCustomer(ServerRequest request) {
        return service.getById(request.pathVariable("id"))
                .switchIfEmpty(error(new ResponseStatusException(NOT_FOUND)))
                .flatMap(dto -> service.deleteById(dto.getId()).then(noContent().build()));
    }

    private void validate(CustomerDTO dto) {
        Errors errors = new BeanPropertyBindingResult(dto, "customerDto");

        validator.validate(dto, errors);

        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}
