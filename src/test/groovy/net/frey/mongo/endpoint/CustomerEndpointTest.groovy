package net.frey.mongo.endpoint

import net.frey.mongo.model.CustomerDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import spock.lang.Specification
import spock.lang.Stepwise

import static org.hamcrest.Matchers.equalTo
import static org.springframework.web.util.UriComponentsBuilder.fromPath
import static reactor.core.publisher.Mono.just
import static net.frey.mongo.endpoint.CustomerHandler.CUSTOMER_PATH

@Stepwise
@SpringBootTest
@AutoConfigureWebTestClient
class CustomerEndpointTest extends Specification {
    @Autowired
    WebTestClient client

    def "list customers"() {
        expect:
        client.get().uri(CUSTOMER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", "application/json")
            .expectBody().jsonPath('$.size()').isEqualTo(2)
    }

    def "get customer by name"() {
        given:
        final def NAME = "Bobby Tables"
        def testDto = buildDto()
        testDto.customerName = NAME

        client.post().uri(CUSTOMER_PATH)
            .body(just(testDto), CustomerDTO)
            .header("Content-Type", "application/json")
            .exchange()

        expect:
        client.get().uri(fromPath(CUSTOMER_PATH).queryParam("name", NAME).build().toUri())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", "application/json")
            .expectBody().jsonPath('$.size()').value(equalTo(1))
    }

    def "get by id"() {
        given:
        def customerDto = getSavedTestCustomer()

        expect:
        client.get().uri("$CUSTOMER_PATH/$customerDto.id")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", "application/json")
            .expectBody(CustomerDTO)
    }

    def "create a new customer"() {
        expect:
        client.post().uri(CUSTOMER_PATH)
            .body(just(buildDto()), CustomerDTO)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().exists("location")
    }

    def "update a customer"() {
        given:
        def dto = getSavedTestCustomer()

        expect:
        client.put().uri("$CUSTOMER_PATH/$dto.id")
            .body(just(buildDto()), CustomerDTO)
            .exchange()
            .expectStatus().isNoContent()
    }

    def "patch a customer"() {
        given:
        def dto = getSavedTestCustomer()

        expect:
        client.patch().uri("$CUSTOMER_PATH/$dto.id")
            .body(just(buildDto()), CustomerDTO)
            .exchange()
            .expectStatus().isNoContent()
    }

    def "create a new customer but there's an error"() {
        given:
        def customer = buildDto()
        customer.customerName = ""

        expect:
        client.post().uri(CUSTOMER_PATH)
            .body(just(customer), CustomerDTO)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isBadRequest()
    }

    def "update a customer but there's an error"() {
        given:
        def customer = buildDto()
        customer.customerName = ""

        expect:
        client.put().uri("$CUSTOMER_PATH/1")
            .body(just(customer), CustomerDTO)
            .exchange()
            .expectStatus().isBadRequest()
    }

    def "get by id but there's an error"() {
        expect:
        client.get().uri("$CUSTOMER_PATH/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    def "update a customer that doesn't exist"() {
        expect:
        client.put().uri("$CUSTOMER_PATH/999")
            .body(just(buildDto()), CustomerDTO)
            .exchange()
            .expectStatus().isNotFound()
    }

    def buildDto() {
        CustomerDTO.builder()
            .customerName("Joan Rivers")
            .build()
    }

    def getSavedTestCustomer() {
        client.get().uri(CUSTOMER_PATH).exchange().returnResult(CustomerDTO).getResponseBody().blockFirst()
    }
}
