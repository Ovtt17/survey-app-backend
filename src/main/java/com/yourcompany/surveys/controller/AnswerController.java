package com.yourcompany.surveys.controller;

import com.yourcompany.surveys.dto.answer.AnswerRequestDTO;
import com.yourcompany.surveys.dto.answer.AnswerResponse;
import com.yourcompany.surveys.service.AnswerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/surveys/answers")
@RequiredArgsConstructor
@Tag(name = "Answers")
public class AnswerController {

    private final AnswerService answerService;

    @GetMapping
    public ResponseEntity<List<AnswerResponse>> getAllAnswers() {
        return ResponseEntity.ok(answerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<AnswerResponse>> getAnswerById(@PathVariable Long id) {
        Optional<AnswerResponse> answer = answerService.findById(id);
        return new ResponseEntity<>(answer, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Void> createAnswer(
            @RequestBody List<AnswerRequestDTO> answers,
            Principal principal
    ) {
        answerService.save(answers, principal);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnswerResponse> updateAnswer(
            @PathVariable Long id,
            @RequestBody AnswerRequestDTO answer,
            Principal principal
    ) {
        AnswerResponse updatedAnswer = answerService.update(id, answer, principal);
        return new ResponseEntity<>(updatedAnswer, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        answerService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{surveyId}/{userId}/{participationId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersBySurveyAndUser(
            @PathVariable @Valid Long surveyId,
            @PathVariable @Valid Long userId,
            @PathVariable @Valid Long participationId
    ) {
        return ResponseEntity.ok(answerService.findBySurveyIdAndUserIdAndParticipationId(surveyId, userId, participationId));
    }
}