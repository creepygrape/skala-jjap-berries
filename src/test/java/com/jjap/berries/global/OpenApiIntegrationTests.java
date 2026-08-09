package com.jjap.berries.global;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiIntegrationTests {

  @Autowired MockMvc mvc;

  @Test
  void exposesBearerJwtSecurityScheme() throws Exception {
    mvc.perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
        .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
        .andExpect(jsonPath("$.security").doesNotExist())
        .andExpect(jsonPath("$.paths['/api/auth/login'].post.security").doesNotExist())
        .andExpect(jsonPath("$.paths['/api/products'].get.security").doesNotExist())
        .andExpect(
            jsonPath("$.paths['/api/channels/{channelId}/members'].get.security[0].bearerAuth")
                .isArray())
        .andExpect(
            jsonPath("$.paths['/api/channels/managed'].get.security[0].bearerAuth").isArray())
        .andExpect(jsonPath("$.paths['/api/orders'].post.security[0].bearerAuth").isArray());
  }

  @Test
  void channelManagerQueriesRequireAuthentication() throws Exception {
    mvc.perform(get("/api/channels/{channelId}/members", 1001L))
        .andExpect(status().isUnauthorized());
    mvc.perform(get("/api/channels/managed")).andExpect(status().isUnauthorized());
  }

}
