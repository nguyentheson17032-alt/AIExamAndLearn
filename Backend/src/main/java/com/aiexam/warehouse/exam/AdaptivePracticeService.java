package com.aiexam.warehouse.exam;

import com.aiexam.warehouse.ai.ExamAiClient;
import com.aiexam.warehouse.ai.GeneratedQuestion;
import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.exception.ResourceNotFoundException;
import com.aiexam.warehouse.question.PublishStatus;
import com.aiexam.warehouse.question.Question;
import com.aiexam.warehouse.question.QuestionRepository;
import com.aiexam.warehouse.question.QuestionService;
import com.aiexam.warehouse.question.QuestionSource;
import com.aiexam.warehouse.user.User;
import com.aiexam.warehouse.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdaptivePracticeService {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final QuestionService questionService;
    private final ExamAiClient examAiClient;

    @Transactional
    public Exam createSession(UserPrincipal principal, AdaptivePracticeRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getUsername()));
        int targetElo = user.getEloRating();
        int window = 200;
        List<Question> nearby = new ArrayList<>(questionRepository.findPublishedNearElo(
                PublishStatus.PUBLISHED,
                request.subject(),
                Math.max(100, targetElo - window),
                targetElo + window));
        nearby.sort((left, right) -> Integer.compare(
                Math.abs(left.getEloRating() - targetElo),
                Math.abs(right.getEloRating() - targetElo)));
        int needed = Math.max(5, request.questionCount());
        if (nearby.size() < needed) {
            List<GeneratedQuestion> generated = examAiClient.generateQuestions(
                    request.subject(),
                    request.topic() == null ? request.subject() : request.topic(),
                    user.rank() == com.aiexam.warehouse.elo.Rank.BRONZE
                            ? com.aiexam.warehouse.question.Difficulty.BEGINNER
                            : com.aiexam.warehouse.question.Difficulty.INTERMEDIATE,
                    needed - nearby.size(),
                    targetElo);
            for (GeneratedQuestion item : generated) {
                nearby.add(questionService.persistGenerated(user, item, QuestionSource.AI_GENERATED));
            }
        }
        Exam exam = Exam.create(
                user,
                "Adaptive practice: " + request.subject(),
                "Practice generated around Elo " + targetElo,
                ExamType.PRACTICE,
                nearby.stream().allMatch(question -> question.getSource() == QuestionSource.USER_UPLOAD)
                        ? ExamSource.USER_CREATED
                        : ExamSource.AI_GENERATED,
                targetElo,
                request.timeLimitMinutes());
        nearby.stream().limit(needed).forEach(question -> exam.addQuestion(question, 1));
        return examRepository.save(exam);
    }

    public record AdaptivePracticeRequest(
            @NotBlank String subject,
            String topic,
            @jakarta.validation.constraints.Min(5) @jakarta.validation.constraints.Max(30) int questionCount,
            Integer timeLimitMinutes
    ) {}
}
