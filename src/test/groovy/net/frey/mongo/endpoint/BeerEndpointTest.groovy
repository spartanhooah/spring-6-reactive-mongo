package net.frey.mongo.endpoint

import net.frey.mongo.model.BeerDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import spock.lang.Stepwise

import static net.frey.mongo.endpoint.BeerHandler.BEER_PATH
import static org.hamcrest.Matchers.equalTo
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login
import static org.springframework.web.util.UriComponentsBuilder.fromPath
import static reactor.core.publisher.Mono.just

@Stepwise
@SpringBootTest
@AutoConfigureWebTestClient
class BeerEndpointTest extends Specification {
    @Autowired
    WebTestClient client

    def "list beers"() {
        expect:
        client
            .mutateWith(mockOAuth2Login())
            .get().uri(BEER_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", "application/json")
            .expectBody().jsonPath('$.size()').isEqualTo(3)
    }

    def "get beers by style"() {
        given:
        final def STYLE = "TEST"
        def testDto = buildDto()
        testDto.beerStyle = STYLE

        client
            .mutateWith(mockOAuth2Login())
            .post().uri(BEER_PATH)
            .body(just(testDto), BeerDTO)
            .header("Content-Type", "application/json")
            .exchange()

        expect:
        client
            .mutateWith(mockOAuth2Login())
            .get().uri(fromPath(BEER_PATH).queryParam("style", STYLE).build().toUri())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", "application/json")
            .expectBody().jsonPath('$.size()').value(equalTo(1))
    }

    def "get by id"() {
        given:
        def beerDto = getSavedTestBeer()

        expect:
        client
            .mutateWith(mockOAuth2Login())
            .get().uri("$BEER_PATH/$beerDto.id")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", "application/json")
            .expectBody(BeerDTO)
    }

    def "create a new beer"() {
        expect:
        client
            .mutateWith(mockOAuth2Login())
            .post().uri(BEER_PATH)
            .body(just(buildDto()), BeerDTO)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().exists("location")
    }

    def "update a beer"() {
        given:
        def dto = getSavedTestBeer()

        expect:
        client
            .mutateWith(mockOAuth2Login())
            .put().uri("$BEER_PATH/$dto.id")
            .body(just(buildDto()), BeerDTO)
            .exchange()
            .expectStatus().isNoContent()
    }

    def "patch a beer"() {
        given:
        def dto = getSavedTestBeer()

        expect:
        client
            .mutateWith(mockOAuth2Login())
            .patch().uri("$BEER_PATH/$dto.id")
            .body(just(buildDto()), BeerDTO)
            .exchange()
            .expectStatus().isNoContent()
    }

    def "create a new beer but there's an error"() {
        given:
        def beer = buildDto()
        beer.beerName = ""

        expect:
        client
            .mutateWith(mockOAuth2Login())
            .post().uri(BEER_PATH)
            .body(just(beer), BeerDTO)
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isBadRequest()
    }

    def "update a beer but there's an error"() {
        given:
        def beer = buildDto()
        beer.beerStyle = ""

        expect:
        client
            .mutateWith(mockOAuth2Login())
            .put().uri("$BEER_PATH/1")
            .body(just(beer), BeerDTO)
            .exchange()
            .expectStatus().isBadRequest()
    }

    def "get by id but there's an error"() {
        expect:
        client
            .mutateWith(mockOAuth2Login())
            .get().uri("$BEER_PATH/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    def "update a beer that doesn't exist"() {
        expect:
        client
            .mutateWith(mockOAuth2Login())
            .put().uri("$BEER_PATH/999")
            .body(just(buildDto()), BeerDTO)
            .exchange()
            .expectStatus().isNotFound()
    }

    def getSavedTestBeer() {
        client
            .mutateWith(mockOAuth2Login())
            .get().uri(BEER_PATH)
            .exchange()
            .returnResult(BeerDTO)
            .getResponseBody()
            .blockFirst()
    }

    def buildDto() {
        BeerDTO.builder()
            .beerName("Saporous")
            .beerStyle("Sour")
            .upc("123456")
            .quantityOnHand(1)
            .price(BigDecimal.TEN)
            .build()
    }
}
