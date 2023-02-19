package com.reactivespring.moviesinfoservice.repository;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
class MovieInfoRepositoryIT {

    @Autowired
    MovieInfoRepository movieInfoRepository;


    @BeforeEach
    void setUp() {
        var movieInfos = List.of(
                new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );

        movieInfoRepository.saveAll(movieInfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {
        var movieInfoFlux = movieInfoRepository.findAll().log();
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void findMovieInfoByYear() {
        Integer yearToSearch = 2005;
        var movieInfoFlux = movieInfoRepository.findMovieInfosByYear(yearToSearch).log();
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findById() {
        var movieInfoFlux = movieInfoRepository.findById("abc").log();
        StepVerifier.create(movieInfoFlux)
                .assertNext(movieInfo -> {
                    assertEquals("Dark Knight Rises", movieInfo.getName());
                })
                .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        //given
        var movieInfo = new MovieInfo(null, "Wolverine Origins", 2020,
                List.of("Hugh Jackman"), LocalDate.parse("2020-01-01"));
        //when
        var movieInfoMono = movieInfoRepository.save(movieInfo).log();
        //then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfoSaved -> {
                    assertNotNull(movieInfoSaved.getMovieInfoId());
                }).verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        //given
        var movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setName(movieInfo.getName() + "_1");
        //when
        var movieInfoMono = movieInfoRepository.save(movieInfo).log();
        //then
        StepVerifier.create(movieInfoMono)
                .assertNext(movieInfoSaved -> {
                    assertEquals(movieInfoSaved.getName(), "Dark Knight Rises_1");
                }).verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        //given
        movieInfoRepository.deleteById("abc").block();
        //when
        var movieInfoFlux = movieInfoRepository.findAll().log();
        //then
        StepVerifier.create(movieInfoFlux)
                .expectNextCount(2)
                .verifyComplete();
    }
}