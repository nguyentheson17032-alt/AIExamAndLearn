package com.aiexam.warehouse.user;

import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.api.ApiResponse;
import com.aiexam.warehouse.common.api.PageResponse;
import com.aiexam.warehouse.elo.EloHistory;
import com.aiexam.warehouse.elo.Rank;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserService.UserProfileResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.profile(principal));
    }

    @GetMapping("/me/elo-history")
    public ApiResponse<PageResponse<EloHistoryResponse>> eloHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(userService.eloHistory(principal, pageable).map(EloHistoryResponse::from)));
    }

    public record EloHistoryResponse(
            UUID id,
            int eloBefore,
            int eloAfter,
            int delta,
            String reason,
            Rank rankAfter,
            Instant createdAt
    ) {
        public static EloHistoryResponse from(EloHistory history) {
            return new EloHistoryResponse(
                    history.getId(),
                    history.getEloBefore(),
                    history.getEloAfter(),
                    history.getDelta(),
                    history.getReason().name(),
                    Rank.fromElo(history.getEloAfter()),
                    history.getCreatedAt());
        }
    }
}
