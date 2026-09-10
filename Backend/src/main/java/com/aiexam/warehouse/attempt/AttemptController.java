package com.aiexam.warehouse.attempt;

import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.api.ApiResponse;
import com.aiexam.warehouse.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    @PostMapping("/api/v1/exams/{examId}/attempts")
    public ResponseEntity<ApiResponse<AttemptService.AttemptResponse>> start(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID examId) {
        Attempt attempt = attemptService.start(principal, examId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(AttemptService.AttemptResponse.from(attempt)));
    }

    @GetMapping("/api/v1/attempts")
    public ApiResponse<PageResponse<AttemptService.AttemptResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(
                attemptService.listMine(principal, pageable).map(AttemptService.AttemptResponse::from)));
    }

    @GetMapping("/api/v1/attempts/{id}")
    public ApiResponse<AttemptService.AttemptResponse> get(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ApiResponse.ok(AttemptService.AttemptResponse.from(attemptService.getById(id, principal)));
    }

    @PostMapping("/api/v1/attempts/{id}/answers")
    public ApiResponse<AttemptService.AttemptResponse> saveAnswers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody SaveAnswersRequest request) {
        Attempt attempt = attemptService.saveAnswers(principal, id, request.answers());
        return ApiResponse.ok(AttemptService.AttemptResponse.from(attempt));
    }

    @PostMapping("/api/v1/attempts/{id}/submit")
    public ApiResponse<AttemptService.AttemptResponse> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ApiResponse.ok(AttemptService.AttemptResponse.from(attemptService.submit(principal, id)));
    }

    public record SaveAnswersRequest(@NotEmpty List<AttemptService.AnswerRequest> answers) {}
}
