package net.frey.mongo.service

import net.frey.mongo.domain.Customer
import net.frey.mongo.mapper.CustomerMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import spock.lang.Specification

import static reactor.core.publisher.Mono.just

@SpringBootTest
class CustomerServiceImplTest extends Specification {
    @Autowired
    CustomerService customerService

    @Autowired
    CustomerMapper customerMapper

    def customerDto

    def setup() {
        customerDto = customerMapper.entityToDto(buildTestEntity())
    }

    def "save customer"() {
        when:
        def savedMono = customerService.saveCustomer(just(customerDto))

        then:
        StepVerifier.create(savedMono)
            .expectNextMatches { it?.id != null }
            .verifyComplete()
    }

    def "find customer by name"() {
        given:
        def customerDto = getSavedCustomer()

        when:
        def foundDto = customerService.findFirstByName(customerDto.customerName)

        then:
        StepVerifier.create(foundDto)
            .expectNextMatches { it.customerName == customerDto.customerName }
            .verifyComplete()
    }

    def getSavedCustomer() {
        customerService.saveCustomer(just(buildTestDto())).block()
    }

    def buildTestDto() {
        customerMapper.entityToDto(buildTestEntity())
    }

    static def buildTestEntity() {
        Customer.builder()
            .customerName("Bobby Tables")
            .build()
    }
}
