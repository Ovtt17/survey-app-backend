package com.yourcompany.surveys.controller;

import com.yourcompany.surveys.dto.participant.ParticipantResponse;
import com.yourcompany.surveys.dto.survey.SurveyRequestDTO;
import com.yourcompany.surveys.dto.survey.SurveyResponse;
import com.yourcompany.surveys.service.SurveyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/surveys")
@RequiredArgsConstructor
@Tag(name = "Surveys")
public class SurveyController {
    private final SurveyService surveyService;

    @GetMapping
    public ResponseEntity<List<SurveyResponse>> getAllSurveys() {
        return ResponseEntity.ok(surveyService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<SurveyResponse>> getSurveyById(@PathVariable Long id) {
        Optional<SurveyResponse> survey = surveyService.findById(id);
        return new ResponseEntity<>(survey, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<SurveyResponse>> getSurveysByUser(Principal principal) {
        return ResponseEntity.ok(surveyService.getByUser(principal));
    }

    @PostMapping
    public ResponseEntity<SurveyResponse> createSurvey(@RequestBody SurveyRequestDTO surveyRequest, Principal principal) {
        SurveyResponse createdSurvey = surveyService.save(surveyRequest, principal);
        return new ResponseEntity<>(createdSurvey, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SurveyResponse> updateSurvey(@PathVariable Long id, @RequestBody SurveyRequestDTO surveyRequest) {
        SurveyResponse updatedSurvey = surveyService.update(id, surveyRequest);
        return new ResponseEntity<>(updatedSurvey, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSurvey(@PathVariable Long id) {
        surveyService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantResponse>> getSurveyParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(surveyService.getSurveyParticipants(id));
    }
}