package com.aiexam.warehouse.exam;

import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.api.ApiResponse;
import com.aiexam.warehouse.common.api.PageResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final AdaptivePracticeService adaptivePracticeService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExamService.ExamResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ExamService.CreateExamRequest request) {
        Exam exam = examService.create(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ExamService.ExamResponse.from(exam)));
    }

    @GetMapping
    public ApiResponse<PageResponse<ExamService.ExamResponse>> list(
            @RequestParam(required = false) ExamType examType,
            Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(examService.list(examType, pageable).map(ExamService.ExamResponse::summary)));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExamService.ExamResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(ExamService.ExamResponse.from(examService.getById(id)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ExamService.ExamResponse>> generate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ExamService.GenerateExamRequest request) {
        Exam exam = examService.generateExamSet(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ExamService.ExamResponse.from(exam)));
    }

    @PostMapping("/{id}/similar")
    public ResponseEntity<ApiResponse<ExamService.ExamResponse>> similar(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "4") int count) {
        Exam exam = examService.generateSimilar(principal, id, count);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ExamService.ExamResponse.from(exam)));
    }

    @PostMapping("/practice")
    public ResponseEntity<ApiResponse<ExamService.ExamResponse>> practice(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AdaptivePracticeService.AdaptivePracticeRequest request) {
        Exam exam = adaptivePracticeService.createSession(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(ExamService.ExamResponse.from(exam)));
    }
}
