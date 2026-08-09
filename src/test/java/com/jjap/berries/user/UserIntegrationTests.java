package com.jjap.berries.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @Test
  void getUpdateAndWithdrawMyProfile() throws Exception {
    mvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"email\":\"me-test@berries.com\",\"password\":\"password123!\",\"nickname\":\"me_tester\",\"role\":\"USER\"}"))
        .andExpect(status().isCreated());
    String login =
        mvc.perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"me-test@berries.com\",\"password\":\"password123!\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String access = mapper.readTree(login).get("data").get("accessToken").asText();
    String bearer = "Bearer " + access;
    mvc.perform(get("/api/users/me").header("Authorization", bearer))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nickname").value("me_tester"));
    mvc.perform(
            patch("/api/users/me")
                .header("Authorization", bearer)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"nickname\":\"updated_tester\",\"profileImageUrl\":\"https://example.com/me.png\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nickname").value("updated_tester"));
    mvc.perform(delete("/api/users/me").header("Authorization", bearer))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/users/me").header("Authorization", bearer))
        .andExpect(status().isUnauthorized());
  }
}
