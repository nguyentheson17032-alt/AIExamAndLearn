package com.aiexam.warehouse.exam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aiexam.warehouse.common.exception.ProblemDetailExceptionHandler;
import com.aiexam.warehouse.question.Question;
import com.aiexam.warehouse.question.QuestionSource;
import com.aiexam.warehouse.question.QuestionType;
import com.aiexam.warehouse.support.TestEntities;
import com.aiexam.warehouse.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExamController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProblemDetailExceptionHandler.class)
class ExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExamService examService;

    @MockitoBean
    private AdaptivePracticeService adaptivePracticeService;

    @MockitoBean
    private com.aiexam.warehouse.auth.JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void create_withValidRequest_shouldReturn201() throws Exception {
        User author = TestEntities.user("teacher@example.com");
        Question question = Question.create(author, "Stem", QuestionType.SHORT_ANSWER, "Math", "Algebra",
                QuestionSource.USER_UPLOAD);
        TestEntities.setId(question, UUID.randomUUID());
        Exam exam = Exam.create(author, "Midterm", "Desc", ExamType.EXAM, ExamSource.USER_CREATED, 1200, 45);
        exam.addQuestion(question, 1);
        TestEntities.setId(exam, UUID.randomUUID());
        when(examService.create(any(), any())).thenReturn(exam);

        var request = new ExamService.CreateExamRequest(
                "Midterm", "Desc", ExamType.EXAM, 1200, 45, List.of(question.getId()));

        mockMvc.perform(post("/api/v1/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Midterm"));
    }

    @Test
    void practice_withValidRequest_shouldReturn201() throws Exception {
        User author = TestEntities.user("learner@example.com");
        Exam exam = Exam.create(author, "Adaptive practice: Math", "Practice", ExamType.PRACTICE,
                ExamSource.AI_GENERATED, 1200, 20);
        TestEntities.setId(exam, UUID.randomUUID());
        when(adaptivePracticeService.createSession(any(), any())).thenReturn(exam);

        var request = new AdaptivePracticeService.AdaptivePracticeRequest("Math", "Algebra", 5, 20);

        mockMvc.perform(post("/api/v1/exams/practice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.examType").value("PRACTICE"));
    }
}
