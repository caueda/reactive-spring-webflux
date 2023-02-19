package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe")).log();
    }

    public Mono<String> nameMono() {
        return Mono.just("alex");
    }

    public Flux<String> namesFluxFilterMap(Integer stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(name -> name.length() > stringLength)
                .map(name -> name.length() + " - " + name);
    }

    public Flux<String> namesFluxFilterFlatmap(Integer stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(name -> name.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(this::splitString)
                .log();
    }

    public Flux<String> namesFluxFilterFlatmapAsync(Integer stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(name -> name.length() > stringLength)
                .map(String::toUpperCase)
                .flatMap(this::splitStringDelay)
                .log();
    }

    public Flux<String> namesFluxFilterConcatmapAsync(Integer stringLength) {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .filter(name -> name.length() > stringLength)
                .map(String::toUpperCase)
                .concatMap(this::splitStringDelay)
                .log();
    }

    public Flux<String> splitString(String value) {
        var arrayOfStrings = value.split("");
        return Flux.fromArray(arrayOfStrings);
    }

    public Flux<String> splitStringDelay(String value) {
        var arrayOfString = value.split("");
        var delay = new Random().nextInt(1000);
        return Flux.fromArray(arrayOfString).delayElements(Duration.ofMillis(delay));
    }
}
