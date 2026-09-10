package com.aiexam.warehouse.exam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.aiexam.warehouse.ai.ExamAiClient;
import com.aiexam.warehouse.ai.GeneratedQuestion;
import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.question.Difficulty;
import com.aiexam.warehouse.question.PublishStatus;
import com.aiexam.warehouse.question.Question;
import com.aiexam.warehouse.question.QuestionRepository;
import com.aiexam.warehouse.question.QuestionService;
import com.aiexam.warehouse.question.QuestionSource;
import com.aiexam.warehouse.question.QuestionType;
import com.aiexam.warehouse.support.TestEntities;
import com.aiexam.warehouse.user.User;
import com.aiexam.warehouse.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdaptivePracticeServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private ExamRepository examRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private QuestionService questionService;
    @Mock
    private ExamAiClient examAiClient;

    @InjectMocks
    private AdaptivePracticeService adaptivePracticeService;

    @Test
    void createSession_whenBankIsShort_shouldGenerateAdditionalQuestions() {
        User user = TestEntities.user("learner@example.com");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Question existing = Question.create(user, "Existing", QuestionType.SHORT_ANSWER, "Math", "Algebra",
                QuestionSource.USER_UPLOAD);
        TestEntities.setId(existing, UUID.randomUUID());
        when(questionRepository.findPublishedNearElo(eq(PublishStatus.PUBLISHED), eq("Math"), anyInt(), anyInt()))
                .thenReturn(List.of(existing));
        GeneratedQuestion generated = new GeneratedQuestion(
                "Generated stem",
                QuestionType.MULTIPLE_CHOICE,
                "Math",
                "Algebra",
                Difficulty.INTERMEDIATE,
                "A",
                "Because",
                List.of(new GeneratedQuestion.GeneratedChoice("A", "Yes", true)));
        when(examAiClient.generateQuestions(eq("Math"), eq("Algebra"), any(), eq(4), eq(user.getEloRating())))
                .thenReturn(List.of(generated, generated, generated, generated));
        when(questionService.persistGenerated(any(), any(), any())).thenAnswer(invocation -> {
            Question question = Question.create(user, "AI", QuestionType.MULTIPLE_CHOICE, "Math", "Algebra",
                    QuestionSource.AI_GENERATED);
            TestEntities.setId(question, UUID.randomUUID());
            return question;
        });
        when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Exam exam = adaptivePracticeService.createSession(
                new UserPrincipal(user),
                new AdaptivePracticeService.AdaptivePracticeRequest("Math", "Algebra", 5, 20));

        assertThat(exam.getExamType()).isEqualTo(ExamType.PRACTICE);
        assertThat(exam.getTargetElo()).isEqualTo(user.getEloRating());
        assertThat(exam.getQuestions()).hasSize(5);
    }
}
