package net.frey.mongo.service

import net.frey.mongo.domain.Beer
import net.frey.mongo.mapper.BeerMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import spock.lang.Specification
import reactor.core.publisher.Mono

import static reactor.core.publisher.Mono.just

@SpringBootTest
class BeerServiceImplTest extends Specification {
    @Autowired
    BeerService beerService

    @Autowired
    BeerMapper beerMapper

    def beerDto

    def setup() {
        beerDto = beerMapper.entityToDto(buildTestEntity())
    }

    def "save beer"() {
        when:
        def savedMono = beerService.saveBeer(just(beerDto))

        then:
        StepVerifier.create(savedMono)
            .expectNextMatches { it?.id != null }
            .verifyComplete()
    }

    def "find beer by name"() {
        given:
        def beerDto = getSavedBeer()

        when:
        def foundDto = beerService.findFirstByBeerName(beerDto.beerName)

        then:
        StepVerifier.create(foundDto)
            .expectNextMatches { it.beerStyle == beerDto.beerStyle }
            .verifyComplete()
    }

    def getSavedBeer() {
        beerService.saveBeer(just(buildTestDto())).block()
    }

    def buildTestDto() {
        beerMapper.entityToDto(buildTestEntity())
    }

    static def buildTestEntity() {
        Beer.builder()
            .beerName("Space Dust")
            .beerStyle("IPA")
            .price(BigDecimal.TEN)
            .quantityOnHand(12)
            .upc("123456")
            .build()
    }
}
