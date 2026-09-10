package com.aiexam.warehouse.user;

import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.exception.ResourceNotFoundException;
import com.aiexam.warehouse.elo.EloHistory;
import com.aiexam.warehouse.elo.EloHistoryRepository;
import com.aiexam.warehouse.elo.Rank;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final EloHistoryRepository eloHistoryRepository;

    public User requireById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.user(id.toString()));
    }

    public UserProfileResponse profile(UserPrincipal principal) {
        User user = requireById(principal.getId());
        return UserProfileResponse.from(user);
    }

    public Page<EloHistory> eloHistory(UserPrincipal principal, Pageable pageable) {
        return eloHistoryRepository.findByUser_IdOrderByCreatedAtDesc(principal.getId(), pageable);
    }

    public record UserProfileResponse(
            UUID id,
            String email,
            String displayName,
            String role,
            int eloRating,
            Rank rank
    ) {
        public static UserProfileResponse from(User user) {
            return new UserProfileResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getDisplayName(),
                    user.getRole().name(),
                    user.getEloRating(),
                    user.rank());
        }
    }
}
