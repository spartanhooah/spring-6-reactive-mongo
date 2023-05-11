package net.frey.mongo.bootstrap;

import static java.time.LocalDateTime.now;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import net.frey.mongo.domain.Beer;
import net.frey.mongo.domain.Customer;
import net.frey.mongo.repository.BeerRepository;
import net.frey.mongo.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {
    private final BeerRepository beerRepository;
    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {
        beerRepository.deleteAll().doOnSuccess(success -> loadBeerData(now())).subscribe();

        customerRepository
                .deleteAll()
                .doOnSuccess(success -> loadCustomerData(now()))
                .subscribe();
    }

    private void loadBeerData(LocalDateTime now) {
        beerRepository.count().subscribe(count -> {
            if (count == 0) {
                Beer beer1 = Beer.builder()
                        .beerName("Galaxy Cat")
                        .beerStyle("Pale Ale")
                        .upc("12356")
                        .price(new BigDecimal("12.99"))
                        .quantityOnHand(122)
                        .createdDate(now)
                        .lastModifiedDate(now)
                        .build();

                Beer beer2 = Beer.builder()
                        .beerName("Crank")
                        .beerStyle("Pale Ale")
                        .upc("12356222")
                        .price(new BigDecimal("11.99"))
                        .quantityOnHand(392)
                        .createdDate(now)
                        .lastModifiedDate(now)
                        .build();

                Beer beer3 = Beer.builder()
                        .beerName("Sunshine City")
                        .beerStyle("IPA")
                        .upc("12356")
                        .price(new BigDecimal("13.99"))
                        .quantityOnHand(144)
                        .createdDate(now)
                        .lastModifiedDate(now)
                        .build();

                beerRepository.save(beer1).subscribe();
                beerRepository.save(beer2).subscribe();
                beerRepository.save(beer3).subscribe();
            }
        });
    }

    private void loadCustomerData(LocalDateTime now) {
        customerRepository.count().subscribe(count -> {
            if (count == 0) {
                Customer customer1 = Customer.builder()
                        .customerName("Bobby Tables")
                        .createdDate(now)
                        .lastModifiedDate(now)
                        .build();

                Customer customer2 = Customer.builder()
                        .customerName("Joan Rivers")
                        .createdDate(now)
                        .lastModifiedDate(now)
                        .build();

                customerRepository.save(customer1).subscribe();
                customerRepository.save(customer2).subscribe();
            }
        });
    }
}
