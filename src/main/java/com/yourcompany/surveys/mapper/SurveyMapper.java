package com.yourcompany.surveys.mapper;

import com.yourcompany.surveys.dto.SurveyRequestDTO;
import com.yourcompany.surveys.dto.SurveyResponse;
import com.yourcompany.surveys.entity.Question;
import com.yourcompany.surveys.entity.Survey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SurveyMapper {

    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;

    public SurveyResponse toResponse(Survey survey) {
        return new SurveyResponse(
                survey.getId(),
                survey.getTitle(),
                survey.getDescription(),
                userMapper.toUserResponse(survey.getCreator()),
                survey.getQuestions().stream()
                        .map(questionMapper::toResponse)
                        .collect(Collectors.toList())
        );
    }

    public Survey toEntity(SurveyRequestDTO surveyRequest) {
        Survey survey = Survey.builder()
                .id(surveyRequest.id())
                .title(surveyRequest.title())
                .description(surveyRequest.description())
                .build();

        survey.setQuestions(surveyRequest.questions().stream()
                .map(questionRequest -> {
                    Question question = questionMapper.toEntity(questionRequest);
                    question.setSurvey(survey);
                    return question;
                })
                .collect(Collectors.toList()));

        return survey;
    }
}