package com.aiexam.warehouse.question;

import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.api.ApiResponse;
import com.aiexam.warehouse.common.api.PageResponse;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuestionService.QuestionResponse>> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody QuestionService.UploadQuestionRequest request) {
        Question saved = questionService.upload(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(QuestionService.QuestionResponse.from(saved)));
    }

    @GetMapping
    public ApiResponse<PageResponse<QuestionService.QuestionResponse>> list(
            @RequestParam(required = false) String subject,
            Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(
                questionService.list(subject, pageable).map(QuestionService.QuestionResponse::from)));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionService.QuestionResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(QuestionService.QuestionResponse.from(questionService.getById(id)));
    }

    @PostMapping("/{id}/classify")
    public ApiResponse<QuestionService.QuestionResponse> classify(@PathVariable UUID id) {
        return ApiResponse.ok(QuestionService.QuestionResponse.from(questionService.classify(id)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<QuestionService.QuestionResponse>>> generate(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody QuestionService.GenerateQuestionsRequest request) {
        List<QuestionService.QuestionResponse> created = questionService.generate(principal, request).stream()
                .map(QuestionService.QuestionResponse::from)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }
}
