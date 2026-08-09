package com.jjap.berries.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @Test
  void loginRefreshLogoutFlow() throws Exception {
    mvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\"jwt-test@berries.com\",\"password\":\"password123!\",\"nickname\":\"jwt_tester\",\"role\":\"USER\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.role").value("USER"));
    String login =
        mvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"jwt-test@berries.com\",\"password\":\"password123!\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode data = mapper.readTree(login).get("data");
    String access = data.get("accessToken").asText();
    String refresh = data.get("refreshToken").asText();
    mvc.perform(get("/api/reservations"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    mvc.perform(get("/api/reservations").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    mvc.perform(get("/api/reservations").header("Authorization", "Bearer " + access))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new RefreshBody(refresh))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + access))
        .andExpect(status().isOk());
    mvc.perform(get("/api/reservations").header("Authorization", "Bearer " + access))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    mvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new RefreshBody(refresh))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void malformedJsonReturnsBadRequest() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid-json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  record RefreshBody(String refreshToken) {}
}
