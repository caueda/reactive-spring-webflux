package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {
    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Review.class)
                .flatMap(review -> reviewReactiveRepository.save(review))
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
        var movieInfoId = serverRequest.queryParam("movieInfoId");

        Flux<Review> reviewFlux = null;

        if(movieInfoId.isPresent()) {
            reviewFlux = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));
        } else {
            reviewFlux = reviewReactiveRepository.findAll();
        }
        return ServerResponse.ok().body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {
        var reviewId = serverRequest.pathVariable("id");
        var existingReviewFlux = reviewReactiveRepository.findById(reviewId);
        return existingReviewFlux
                .flatMap(existingReview -> serverRequest.bodyToMono(Review.class)
                    .map(reqReview -> {
                        existingReview.setComment(reqReview.getComment());
                        existingReview.setRating(reqReview.getRating());
                        return existingReview;
                    })
                    .flatMap(reviewReactiveRepository::save)
                    .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))
                );
    }

    public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {
        var reviewId = serverRequest.pathVariable("id");
        var existingReviewMono = reviewReactiveRepository.findById(reviewId);
        return existingReviewMono
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))
                .then(ServerResponse.noContent().build())
                ;
    }
}
