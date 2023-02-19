package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

class FluxAndMonoGeneratorServiceTest {
    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void namesFlux() {
        //Given

        //When
        var namesFlux = fluxAndMonoGeneratorService.namesFlux();
        //Then
        StepVerifier.create(namesFlux)
                .expectNext("alex", "ben", "chloe")
                .verifyComplete();
    }

    @Test
    void namesFluxFilterMap() {
        //Given
        Integer stringLength = 3;
        //When
        var namesFlux = fluxAndMonoGeneratorService.namesFluxFilterMap(stringLength);
        //Then
        StepVerifier.create(namesFlux)
                .expectNext("4 - alex", "5 - chloe")
                .verifyComplete();
    }

    @Test
    void namesFluxFilterFlatmap() {
        //Given
        Integer stringLength = 3;
        //When
        var namesFlux = fluxAndMonoGeneratorService.namesFluxFilterFlatmapAsync(stringLength);
        //Then
        StepVerifier.create(namesFlux)
                //.expectNext("A", "L", "E", "X", "C", "H", "L",  "O", "E")
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void namesFluxFilterConcatmap() {
        //Given
        Integer stringLength = 3;
        //When
        var namesFlux = fluxAndMonoGeneratorService.namesFluxFilterConcatmapAsync(stringLength);
        //Then
        StepVerifier.create(namesFlux)
                .expectNext("A", "L", "E", "X", "C", "H", "L",  "O", "E")
                //.expectNextCount(9)
                .verifyComplete();
    }

    @Test
    void zipOperator() {
        var aFlux = Flux.just("A","B", "C")
                .delayElements(Duration.ofMillis(100));
        var bFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(90));

        var nFlux = Flux.zip(aFlux, bFlux, (String a, String b) ->  a + b).log();

        StepVerifier.create(nFlux)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    void zipOperator_2() {
        var aFlux = Flux.just("A","B", "C")
                .delayElements(Duration.ofMillis(100));
        var bFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(90));
        var numberFlux = Flux.just("1", "2", "3")
                .map(n -> "0" + n);

        var nFlux = Flux.zip(aFlux, bFlux, numberFlux)
                .map(t3 -> t3.getT1() + t3.getT2() + t3.getT3()).log();

        StepVerifier.create(nFlux)
                .expectNext("AD01", "BE02", "CF03")
                .verifyComplete();
    }
}