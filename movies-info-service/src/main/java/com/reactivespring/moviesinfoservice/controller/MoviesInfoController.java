package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.service.MovieInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/v1")
public class MoviesInfoController {

    private MovieInfoService movieInfoService;

    Sinks.Many<MovieInfo> moviesInfoSink = Sinks.many().replay().latest();

    public MoviesInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @PostMapping("/movieinfos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {
        return movieInfoService.save(movieInfo)
                .doOnNext(savedMovieInfo -> moviesInfoSink.tryEmitNext(savedMovieInfo));
    }

    @GetMapping(value = "/movieinfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> getMovies() {
        return moviesInfoSink.asFlux();
    }

    @PutMapping ("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo(@RequestBody MovieInfo movieInfo, @PathVariable String id) {
        return movieInfoService.update(movieInfo, id)
                .map(ResponseEntity.ok()::body)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @GetMapping("/movieinfos")
    public Flux<MovieInfo> getAllMovieInfos(@RequestParam(name = "year", required = false) Integer year,
                                            @RequestParam(name="name", required = false) String name) {
        if(year != null) {
            log.info("Calling getAllMovieInfos with year param as {}", year);
            return movieInfoService.getMovieInfosByYear(year).log();
        }
        if(name != null) {
            log.info("Calling getAllMovieInfos with name param as {}", name);
            return movieInfoService.getMovieInfosByName(name).log();
        }
        return movieInfoService.findAll();
    }

    @GetMapping("/movieinfos/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable("id") String id) {
        return movieInfoService.findById(id)
                .map(movieInfo -> ResponseEntity.ok().body(movieInfo))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
                .log();
    }

    @DeleteMapping("/movieinfos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfo(@PathVariable("id") String id) {
        return movieInfoService.delete(id).log();
    }
}
