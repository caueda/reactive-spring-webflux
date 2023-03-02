package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class MoviesInfoRestClient {

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;
    private WebClient webClient;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        var url = moviesInfoUrl.concat("/{id}");
        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new MoviesInfoClientException("There is no MovieInfo Available for the passed id: " + movieId,
                                clientResponse.statusCode().value()));
                    }

                    return clientResponse.bodyToMono(String.class)
                            .flatMap(s -> Mono.error(new MoviesInfoClientException(s, clientResponse.statusCode().value())));
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException("Server Exception in MoviesInfoService " + responseMessage)));
                })
                .bodyToMono(MovieInfo.class)
//                .retry(3)
                .retryWhen(
                        Retry.fixedDelay(3, Duration.ofSeconds(1))
                                .filter(ex -> ex instanceof MoviesInfoServerException)
                                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                    Exceptions.propagate(retrySignal.failure())
                                )
                )
                .log();
    }
}
