package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {
    @MockBean
    ReviewReactiveRepository reviewReactiveRepository;

    public static final String REVIEWS_URI = "/v1/reviews";

    @Autowired
    WebTestClient webTestClient;

    @Test
    void addReview() {
        var review = new Review(null, 2L, "Excellent Movie2", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("abc", 2L, "Excellent Movie2", 9.0)));

        webTestClient.post()
                .uri(REVIEWS_URI)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedReview = reviewEntityExchangeResult.getResponseBody();
                    assertNotNull(savedReview);
                    assertNotNull(savedReview.getReviewId());
                });
    }

    @Test
    void addReview_Validation() {
        var review = new Review(null, null, "Excellent Movie2", -9.0);

        webTestClient.post()
                .uri(REVIEWS_URI)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movieInfoId : must not be null,rating.negative : please pass a non-negative value");
    }

    @Test
    void updateReview_whenNotFound() {
        var id = "0";
        when(reviewReactiveRepository.findById(anyString())).thenReturn(Mono.empty());

        webTestClient.put()
                .uri(REVIEWS_URI + "/{id}", id)
                .bodyValue(new Review("abc", 2L, "Excellent Movie2", 9.0))
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
