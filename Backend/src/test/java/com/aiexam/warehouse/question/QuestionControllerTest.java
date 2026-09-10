package com.aiexam.warehouse.question;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aiexam.warehouse.common.exception.ProblemDetailExceptionHandler;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QuestionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ProblemDetailExceptionHandler.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private com.aiexam.warehouse.auth.JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void upload_withValidRequest_shouldReturn201() throws Exception {
        User author = TestEntities.user("author@example.com");
        Question question = Question.create(author, "What is 2+2?", QuestionType.MULTIPLE_CHOICE, "Math", "Arithmetic",
                QuestionSource.USER_UPLOAD);
        question.addChoice("A", "4", true, 1);
        question.addChoice("B", "5", false, 2);
        TestEntities.setId(question, UUID.randomUUID());
        when(questionService.upload(any(), any())).thenReturn(question);

        var request = new QuestionService.UploadQuestionRequest(
                "What is 2+2?",
                QuestionType.MULTIPLE_CHOICE,
                "Math",
                "Arithmetic",
                null,
                "4",
                List.of(
                        new QuestionService.ChoiceRequest("A", "4", true),
                        new QuestionService.ChoiceRequest("B", "5", false)));

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.subject").value("Math"));
    }

    @Test
    void list_shouldReturnPage() throws Exception {
        when(questionService.list(any(), any())).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/v1/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
