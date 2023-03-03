package com.reactivespring.moviesinfoservice.controller;

import com.reactivespring.moviesinfoservice.domain.MovieInfo;
import com.reactivespring.moviesinfoservice.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

        @Autowired
        MovieInfoRepository movieInfoRepository;

        @Autowired
        WebTestClient webTestClient;

        static String MOVIES_INFO_URL = "/v1/movieinfos";

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
                this.movieInfoRepository.deleteAll().block();
        }

        @Test
        void addMovieInfo() {
                //given
                var movieInfo = new MovieInfo(null, "Batman Begins1",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
                //when
                webTestClient.post()
                        .uri(MOVIES_INFO_URL)
                        .bodyValue(movieInfo)
                        .exchange()
                        .expectStatus()
                        .isCreated()
                        .expectBody(MovieInfo.class)
                        .consumeWith(movieInfoEntityExchangeResult -> {
                                var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                                assert savedMovieInfo != null;
                                assert savedMovieInfo.getMovieInfoId() != null;
                        });
                //then
        }

        @Test
        void findMovieById() {
                var id = "abc";
                webTestClient.get()
                        .uri(MOVIES_INFO_URL + "/{id}", id)
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
//                        .expectBody(MovieInfo.class)
//                        .consumeWith(movieInfoEntityExchangeResult -> {
//                           var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
//                           assert movieInfo != null;
//                           assertEquals("abc", movieInfo.getMovieInfoId());
//                        })
                        .expectBody()
                        .jsonPath("$.name").isEqualTo("Dark Knight Rises");
                ;
        }

        @Test
        void deleteMovieInfo() {
                var id = "abc";
                webTestClient.delete()
                        .uri(MOVIES_INFO_URL + "/{id}", id)
                        .exchange()
                        .expectStatus()
                        .isNoContent()
                        .expectBody(Void.class);
        }

        @Test
        void updateMovie() {
                //given
                var id = "abc";
                var movieInfo = new MovieInfo("abc", "Dark Knight Rises II",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
                //when
                webTestClient.put()
                        .uri(MOVIES_INFO_URL + "/{id}", id)
                        .bodyValue(movieInfo)
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBody(MovieInfo.class)
                        .consumeWith(movieInfoEntityExchangeResult -> {
                                var updatedMovie = movieInfoEntityExchangeResult.getResponseBody();
                                assertEquals(updatedMovie.getName(), "Dark Knight Rises II");
                        });
                //then
        }

        @Test
        void updateMovie_notFound() {
                var id="nonExists";
                var movieInfo = new MovieInfo(null, "Dark Knight Rises II",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));
                webTestClient.put()
                        .uri(MOVIES_INFO_URL + "/{id}", id)
                        .bodyValue(movieInfo)
                        .exchange()
                        .expectStatus()
                        .isNotFound();
        }
        @Test
        void getAllMovieInfos() {
                webTestClient
                        .get()
                        .uri(MOVIES_INFO_URL + "/stream")
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBodyList(MovieInfo.class)
                        .hasSize(3);

        }

        @Test
        void getAllMovieInfos_stream() {
                var movieInfo = new MovieInfo(null, "Batman Begins1",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
                //when
                webTestClient.post()
                        .uri(MOVIES_INFO_URL)
                        .bodyValue(movieInfo)
                        .exchange()
                        .expectStatus()
                        .isCreated()
                        .expectBody(MovieInfo.class)
                        .consumeWith(movieInfoEntityExchangeResult -> {
                                var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                                assert savedMovieInfo != null;
                                assert savedMovieInfo.getMovieInfoId() != null;
                        });

                var moviesStreamFlux = webTestClient
                        .get()
                        .uri(MOVIES_INFO_URL)
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .returnResult(MovieInfo.class)
                        .getResponseBody();

                StepVerifier.create(moviesStreamFlux)
                        .assertNext(movieInfo1 -> {
                                assert movieInfo1.getMovieInfoId() != null;
                        })
                        .thenCancel().verify();

        }

        @Test
        void getMoviesByYear() {
                var uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
                        .queryParam("year", "2005")
                        .buildAndExpand().toUri();
                webTestClient
                        .get()
                        .uri(uri)
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBodyList(MovieInfo.class)
                        .hasSize(1);
        }

        @Test
        void getMoviesByName() {
                var uri = UriComponentsBuilder.fromUriString(MOVIES_INFO_URL)
                        .queryParam("name", "dark")
                        .buildAndExpand().toUri();
                webTestClient
                        .get()
                        .uri(uri)
                        .exchange()
                        .expectStatus()
                        .is2xxSuccessful()
                        .expectBodyList(MovieInfo.class)
                        .hasSize(2);
        }
}