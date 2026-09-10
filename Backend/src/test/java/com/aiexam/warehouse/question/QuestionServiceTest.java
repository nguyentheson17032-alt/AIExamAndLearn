package com.aiexam.warehouse.question;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aiexam.warehouse.ai.ExamAiClient;
import com.aiexam.warehouse.ai.QuestionClassification;
import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.exception.BusinessRuleViolationException;
import com.aiexam.warehouse.support.TestEntities;
import com.aiexam.warehouse.user.User;
import com.aiexam.warehouse.user.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExamAiClient examAiClient;

    @InjectMocks
    private QuestionService questionService;

    @Test
    void upload_whenMultipleChoiceMissingCorrectChoice_shouldThrowException() {
        User user = TestEntities.user("author@example.com");
        UserPrincipal principal = new UserPrincipal(user);
        var request = new QuestionService.UploadQuestionRequest(
                "Stem",
                QuestionType.MULTIPLE_CHOICE,
                "Math",
                "Algebra",
                null,
                "A",
                List.of(new QuestionService.ChoiceRequest("A", "1", false)));

        assertThatThrownBy(() -> questionService.upload(principal, request))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("at least 2 choices");
    }

    @Test
    void upload_whenValid_shouldClassifyAndSave() {
        User user = TestEntities.user("author@example.com");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(examAiClient.classifyQuestion(any(), any(), any(), any()))
                .thenReturn(new QuestionClassification(Difficulty.BEGINNER, "REMEMBER", "math", 900, "ok"));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Question saved = questionService.upload(new UserPrincipal(user), new QuestionService.UploadQuestionRequest(
                "What is 1+1?",
                QuestionType.MULTIPLE_CHOICE,
                "Math",
                "Arithmetic",
                "Basic",
                "2",
                List.of(
                        new QuestionService.ChoiceRequest("A", "2", true),
                        new QuestionService.ChoiceRequest("B", "3", false))));

        assertThat(saved.getDifficulty()).isEqualTo(Difficulty.BEGINNER);
        verify(questionRepository).save(any(Question.class));
    }
}
