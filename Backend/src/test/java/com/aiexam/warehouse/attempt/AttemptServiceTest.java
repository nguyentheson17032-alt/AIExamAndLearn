package com.aiexam.warehouse.attempt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aiexam.warehouse.ai.ExamAiClient;
import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.elo.EloHistoryRepository;
import com.aiexam.warehouse.elo.EloProperties;
import com.aiexam.warehouse.elo.EloService;
import com.aiexam.warehouse.exam.Exam;
import com.aiexam.warehouse.exam.ExamRepository;
import com.aiexam.warehouse.exam.ExamSource;
import com.aiexam.warehouse.exam.ExamType;
import com.aiexam.warehouse.question.Question;
import com.aiexam.warehouse.question.QuestionSource;
import com.aiexam.warehouse.question.QuestionType;
import com.aiexam.warehouse.support.TestEntities;
import com.aiexam.warehouse.user.User;
import com.aiexam.warehouse.user.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock
    private AttemptRepository attemptRepository;
    @Mock
    private ExamRepository examRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EloHistoryRepository eloHistoryRepository;
    @Mock
    private ExamAiClient examAiClient;

    private AttemptService attemptService;
    private User user;
    private Question question;
    private Exam exam;

    @BeforeEach
    void setUp() {
        attemptService = new AttemptService(
                attemptRepository,
                examRepository,
                userRepository,
                eloHistoryRepository,
                new EloService(),
                new EloProperties(1200, 24, 12),
                examAiClient);
        user = TestEntities.user("learner@example.com");
        question = Question.create(user, "2+2?", QuestionType.MULTIPLE_CHOICE, "Math", "Arithmetic",
                QuestionSource.USER_UPLOAD);
        TestEntities.setId(question, UUID.randomUUID());
        question.addChoice("A", "4", true, 1);
        question.addChoice("B", "3", false, 2);
        TestEntities.setId(question.getChoices().get(0), UUID.randomUUID());
        TestEntities.setId(question.getChoices().get(1), UUID.randomUUID());
        exam = Exam.create(user, "Quiz", null, ExamType.EXERCISE, ExamSource.USER_CREATED, 1200, 10);
        TestEntities.setId(exam, UUID.randomUUID());
        exam.addQuestion(question, 1);
    }

    @Test
    void submit_whenCorrectMultipleChoice_shouldIncreaseElo() {
        Attempt attempt = Attempt.start(user, exam);
        TestEntities.setId(attempt, UUID.randomUUID());
        UUID correctId = question.getChoices().get(0).getId();
        attempt.answerFor(question).recordResponse(null, correctId.toString());

        when(attemptRepository.findWithDetailsById(attempt.getId())).thenReturn(Optional.of(attempt));
        when(examAiClient.recommendKFactor(anyInt(), anyDouble(), anyInt())).thenReturn(24);
        when(eloHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        int eloBefore = user.getEloRating();
        Attempt graded = attemptService.submit(new UserPrincipal(user), attempt.getId());

        assertThat(graded.getStatus()).isEqualTo(AttemptStatus.GRADED);
        assertThat(graded.getScore()).isEqualByComparingTo("1.00");
        assertThat(user.getEloRating()).isGreaterThan(eloBefore);
        verify(eloHistoryRepository).save(any());
    }

    @Test
    void saveAnswers_shouldPersistSelectedChoice() {
        Attempt attempt = Attempt.start(user, exam);
        TestEntities.setId(attempt, UUID.randomUUID());
        when(attemptRepository.findWithDetailsById(attempt.getId())).thenReturn(Optional.of(attempt));

        UUID choiceId = question.getChoices().get(1).getId();
        Attempt updated = attemptService.saveAnswers(
                new UserPrincipal(user),
                attempt.getId(),
                List.of(new AttemptService.AnswerRequest(question.getId(), null, List.of(choiceId))));

        assertThat(updated.answerFor(question).getSelectedChoiceIds()).isEqualTo(choiceId.toString());
    }
}
