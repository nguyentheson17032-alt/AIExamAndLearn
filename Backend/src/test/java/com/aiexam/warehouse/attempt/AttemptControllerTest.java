package com.aiexam.warehouse.attempt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aiexam.warehouse.common.exception.ProblemDetailExceptionHandler;
import com.aiexam.warehouse.exam.Exam;
import com.aiexam.warehouse.exam.ExamSource;
import com.aiexam.warehouse.exam.ExamType;
import com.aiexam.warehouse.question.Question;
import com.aiexam.warehouse.question.QuestionSource;
import com.aiexam.warehouse.question.QuestionType;
import com.aiexam.warehouse.support.TestEntities;
import com.aiexam.warehouse.user.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AttemptController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProblemDetailExceptionHandler.class)
class AttemptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttemptService attemptService;

    @MockitoBean
    private com.aiexam.warehouse.auth.JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void start_shouldReturn201() throws Exception {
        User user = TestEntities.user("learner@example.com");
        Question question = Question.create(user, "Stem", QuestionType.SHORT_ANSWER, "Math", "Algebra",
                QuestionSource.USER_UPLOAD);
        TestEntities.setId(question, UUID.randomUUID());
        Exam exam = Exam.create(user, "Quiz", null, ExamType.EXERCISE, ExamSource.USER_CREATED, 1200, 15);
        exam.addQuestion(question, 1);
        TestEntities.setId(exam, UUID.randomUUID());
        Attempt attempt = Attempt.start(user, exam);
        TestEntities.setId(attempt, UUID.randomUUID());
        when(attemptService.start(any(), eq(exam.getId()))).thenReturn(attempt);

        mockMvc.perform(post("/api/v1/exams/" + exam.getId() + "/attempts"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }
}
