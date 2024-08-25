package com.yourcompany.surveys.mapper;

import com.yourcompany.surveys.dto.ReviewRequestDTO;
import com.yourcompany.surveys.dto.ReviewResponse;
import com.yourcompany.surveys.entity.Rating;
import com.yourcompany.surveys.entity.Review;
import com.yourcompany.surveys.entity.Survey;
import com.yourcompany.surveys.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    private final RatingMapper ratingMapper;

    public ReviewMapper(RatingMapper ratingMapper) {
        this.ratingMapper = ratingMapper;
    }

    public Review toEntity (ReviewRequestDTO reviewRequest, User user) {
        return Review.builder()
                .id(reviewRequest.id())
                .title(reviewRequest.title())
                .content(reviewRequest.content())
                .survey(
                        Survey.builder()
                                .id(reviewRequest.surveyId())
                                .build()
                )
                .rating(
                        Rating.builder()
                                .id(reviewRequest.rating().id())
                                .rating(reviewRequest.rating().rating())
                                .build()
                )
                .user(user)
                .build();
    }

    public ReviewResponse toResponse (Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getTitle(),
                review.getContent(),
                review.getSurvey().getId(),
                ratingMapper.toResponse(review.getRating()),
                review.getUser().getUsername(),
                review.getCreatedDate()
        );
    }
}