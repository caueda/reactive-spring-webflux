package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinkTest {
    @Test
    void sink() {
        //given
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();
        //when
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        //then
        Flux<Integer> integerFlux = replaySink.asFlux();
        integerFlux.subscribe(i -> {
            System.out.println("Subscriber 1: " + i);
        });

        replaySink.tryEmitNext(3);
    }

    @Test
    void sink_multicast() {
        //given
        Sinks.Many<Integer> replaySink = Sinks.many().multicast().onBackpressureBuffer();
        //when
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        //then
        Flux<Integer> integerFlux = replaySink.asFlux();
        integerFlux.subscribe(i -> {
            System.out.println("Subscriber 1: " + i);
        });
        integerFlux.subscribe(i -> {
            System.out.println("Subscriber 2: " + i);
        });
        replaySink.tryEmitNext(3);
    }

    @Test
    void sink_unicast() {
        //given
        Sinks.Many<Integer> replaySink = Sinks.many().unicast().onBackpressureBuffer();
        //when
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);
        //then
        Flux<Integer> integerFlux = replaySink.asFlux();
        integerFlux.subscribe(i -> {
            System.out.println("Subscriber 1: " + i);
        });
        integerFlux.subscribe(i -> {
            System.out.println("Subscriber 2: " + i);
        });
        replaySink.tryEmitNext(3);
    }
}
