package com.yourcompany.surveys.controller;

import com.yourcompany.surveys.service.ExcelReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports")
public class ReportController {
    private final ExcelReportService excelService;

    @GetMapping("/survey-answers")
    public ResponseEntity<byte[]> getSurveyAnswersReport(@RequestParam Long surveyId)  {
        return excelService.generateSurveyAnswersReport(surveyId);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ex.getReason());
    }
}