package com.yourcompany.surveys.service;

import com.yourcompany.surveys.dto.participation.ParticipationResponse;
import com.yourcompany.surveys.dto.report.*;
import com.yourcompany.surveys.dto.review.ReviewResponse;
import com.yourcompany.surveys.entity.Review;
import com.yourcompany.surveys.entity.User;
import com.yourcompany.surveys.mapper.ReviewMapper;
import com.yourcompany.surveys.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExcelReportService {
    private final AnswerRepository answerRepository;
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    private void createHeaderRow(Sheet sheet, String[] columnNames) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnNames.length; i++) {
            headerRow.createCell(i).setCellValue(columnNames[i]);
        }
    }

    public ResponseEntity<byte[]> generateReport(Long reportId, Optional<Long> surveyId, Principal principal) {
        return switch (reportId.intValue()) {
            case 1 -> generateSurveyAnswersReport(surveyId.orElseThrow(() -> new IllegalArgumentException("Survey ID is required")), principal);
            case 2 -> generateUserParticipationReport(principal);
            case 3 -> generateResponseTrendsReport(surveyId.orElseThrow(() -> new IllegalArgumentException("Survey ID is required")), principal);
            case 4 -> generatePopularSurveysReport(principal);
            case 5 -> generateParticipationCountOnUserSurveys(principal);
            case 6 -> generateUserSatisfactionReport(principal);
            case 7 -> generateUserReviewsReport(surveyId.orElseThrow(() -> new IllegalArgumentException("Survey ID is required")), principal);
            default -> throw new IllegalArgumentException("Invalid report ID");
        };
    }

    public ResponseEntity<byte[]> generateSurveyAnswersReport(Long surveyId, Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            List<SurveyReportResponse> responses = answerRepository.findByAnswerBySurveyIdAndCreatorId(surveyId, user.getId());

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Informe de Respuestas de Encuesta");

            String[] columnNames = {"ID de Usuario", "Nombre de Usuario", "ID de Pregunta", "Texto de Pregunta", "ID de Respuesta", "Texto de Respuesta"};
            createHeaderRow(sheet, columnNames);

            int rowIdx = 1;
            for (SurveyReportResponse response : responses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(response.userId());
                row.createCell(1).setCellValue(response.userName());
                row.createCell(2).setCellValue(response.questionId());
                row.createCell(3).setCellValue(response.questionText());
                row.createCell(4).setCellValue(response.answerId());
                row.createCell(5).setCellValue(response.answerText());
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "survey_answers_report.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating survey answers report", e);
        }
    }

    private ResponseEntity<byte[]> generateUserParticipationReport(Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ParticipationResponse> responses = participationRepository.findAllUserParticipationsByUserId(user.getId());

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("User Participation Report");

            String[] columnNames = {"User ID", "User Name", "Survey ID", "Survey Title", "Participation Date"};
            createHeaderRow(sheet, columnNames);

            int rowIdx = 1;
            for (ParticipationResponse response : responses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(response.userId());
                row.createCell(1).setCellValue(response.username());
                row.createCell(2).setCellValue(response.surveyId());
                row.createCell(3).setCellValue(response.surveyTitle());
                row.createCell(4).setCellValue(response.participatedDate().toString());
            }

            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "user_participation_report.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating user participation report", e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
    private ResponseEntity<byte[]> generateResponseTrendsReport(Long surveyId, Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ResponseTrendReportResponse> responseTrends = answerRepository.findResponseTrendsBySurveyIdAndUserId(surveyId, user.getId());

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Response Trends Report");

            String[] columnNames = {"Question ID", "Question Text", "Answer Text", "Frequency"};
            createHeaderRow(sheet, columnNames);

            int rowIdx = 1;
            for (ResponseTrendReportResponse trend : responseTrends) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(trend.questionId());
                row.createCell(1).setCellValue(trend.questionText());
                row.createCell(2).setCellValue(trend.answerText());
                row.createCell(3).setCellValue(trend.frequency());
            }

            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "response_trends_report.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating response trends report", e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    private ResponseEntity<byte[]> generatePopularSurveysReport(Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<PopularSurveyReportResponse> popularSurveys = surveyRepository.findPopularSurveysByUserId(user.getId());

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Informe de Encuestas Populares");

            String[] columnNames = {"ID de Encuesta", "Título de Encuesta", "Cantidad de Participaciones"};
            createHeaderRow(sheet, columnNames);

            int rowIdx = 1;
            for (PopularSurveyReportResponse survey : popularSurveys) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(survey.surveyId());
                row.createCell(1).setCellValue(survey.surveyTitle());
                row.createCell(2).setCellValue(survey.participationCount());
            }

            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "popular_surveys_report.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating popular surveys report", e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    private ResponseEntity<byte[]> generateParticipationCountOnUserSurveys(Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<UserSurveyParticipationCountResponse> participations = surveyRepository.findParticipationCountByCreatorId(user.getId());

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Informe de Conteo de Participación en Encuestas de Usuario");

            String[] columnNames = {"ID de Encuesta", "Título de Encuesta", "ID de Usuario", "Username", "Cantidad de Participaciones"};
            createHeaderRow(sheet, columnNames);

            int rowIdx = 1;
            for (UserSurveyParticipationCountResponse participation : participations) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(participation.surveyId());
                row.createCell(1).setCellValue(participation.surveyTitle());
                row.createCell(2).setCellValue(participation.userId());
                row.createCell(3).setCellValue(participation.username());
                row.createCell(4).setCellValue(participation.participationCount());
            }

            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "user_survey_participation_count_report.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating user survey participation count report", e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    private ResponseEntity<byte[]> generateUserSatisfactionReport(Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<UserSatisfactionReportResponse> satisfactions = surveyRepository.findUserSatisfactionByCreatorId(user.getId());

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Informe de Satisfacción del Usuario");

            String[] columnNames = {"ID de Encuesta", "Título de Encuesta", "Satisfacción Promedio"};
            createHeaderRow(sheet, columnNames);

            int rowIdx = 1;
            for (UserSatisfactionReportResponse satisfaction : satisfactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(satisfaction.surveyId());
                row.createCell(1).setCellValue(satisfaction.surveyTitle());
                row.createCell(2).setCellValue(satisfaction.averageSatisfaction());
            }

            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "user_satisfaction_report.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating user satisfaction report", e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }

    private ResponseEntity<byte[]> generateUserReviewsReport(Long surveyId, Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Review> reviews = reviewRepository.findBySurveyIdAndUserId(surveyId, user.getId());
            List<ReviewResponse> reviewResponses = reviews.stream()
                    .map(reviewMapper::toResponse)
                    .toList();

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("User Reviews Report");

            String[] columnNames = {"Review ID", "Title", "Content", "Rating", "Survey ID", "Survey Title", "Created Date"};
            createHeaderRow(sheet, columnNames);

            int rowIdx = 1;
            for (ReviewResponse review : reviewResponses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(review.id());
                row.createCell(1).setCellValue(review.title());
                row.createCell(2).setCellValue(review.content());
                row.createCell(3).setCellValue(review.rating().rating());
                row.createCell(4).setCellValue(review.surveyId());
                row.createCell(5).setCellValue(review.surveyTitle());
                row.createCell(6).setCellValue(review.createdDate().toString());
            }

            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "user_reviews_report.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating user reviews report", e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
}
