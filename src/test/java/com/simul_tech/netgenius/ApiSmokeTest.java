package com.simul_tech.netgenius;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Сквозной smoke-тест через настоящую цепочку фильтров Spring Security (MockMvc, H2):
 * регистрация/логин/JWT, разграничение доступа по ролям (RBAC) и публичные эндпоинты.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void ping_isPublic() throws Exception {
        mockMvc.perform(get("/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    void universities_arePublic() throws Exception {
        mockMvc.perform(get("/universities"))
                .andExpect(status().isOk());
    }

    @Test
    void register_login_me_flow() throws Exception {
        String email = "user1@example.com";
        register(email, "secret123");

        String token = login(email, "secret123");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.emailConfirmed").value(false));
    }

    @Test
    void adminEndpoint_requiresAuth_thenForbidsUser_thenAllowsAdmin() throws Exception {
        // Без токена -> 401
        mockMvc.perform(get("/admin/licenses"))
                .andExpect(status().isUnauthorized());

        // Токен обычного пользователя -> 403
        register("user2@example.com", "secret123");
        String userToken = login("user2@example.com", "secret123");
        mockMvc.perform(get("/admin/licenses").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Токен администратора (сид DataInitializer) -> 200
        String adminToken = login("admin@netgenius.ru", "Admin12345");
        mockMvc.perform(get("/admin/licenses").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void me_withoutToken_isUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void teacherEndpoint_forbidsPlainUser() throws Exception {
        register("user3@example.com", "secret123");
        String userToken = login("user3@example.com", "secret123");
        mockMvc.perform(get("/teacher/groups").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // --- helpers ---

    private void register(String email, String password) throws Exception {
        String body = """
                {"firstName":"Тест","lastName":"Тестов","email":"%s","password":"%s"}
                """.formatted(email, password);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("accessToken").asText();
    }
}
