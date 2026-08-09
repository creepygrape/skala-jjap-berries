package com.jjap.berries.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_AUTH = "bearerAuth";

  @Bean
  public OpenAPI berriesOpenApi() {
    SecurityScheme bearerScheme =
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("로그인 API에서 발급받은 Access Token을 입력하세요.");

    return new OpenAPI()
        .info(new Info().title("Berries API").description("팬 커뮤니티 플랫폼 API").version("v1"))
        .components(new Components().addSecuritySchemes(BEARER_AUTH, bearerScheme));
  }

  @Bean
  public OpenApiCustomizer publicApiCustomizer() {
    return openApi ->
        openApi
            .getPaths()
            .forEach(
                (path, pathItem) ->
                    pathItem
                        .readOperationsMap()
                        .forEach(
                            (method, operation) -> {
                              if (isPublicApi(path, method)) {
                                operation.setSecurity(null);
                              } else {
                                operation.setSecurity(
                                    List.of(new SecurityRequirement().addList(BEARER_AUTH)));
                              }
                            }));
  }

  private boolean isPublicApi(String path, HttpMethod method) {
    if (method == HttpMethod.POST
        && (path.equals("/api/auth/signup")
            || path.equals("/api/auth/login")
            || path.equals("/api/auth/refresh"))) {
      return true;
    }
    return method == HttpMethod.GET
        && (path.equals("/api/users/{userId}")
            || path.equals("/api/channels")
            || path.equals("/api/channels/{channelId}")
            || path.startsWith("/api/concerts")
            || path.startsWith("/api/seats")
            || path.startsWith("/api/products")
            || path.startsWith("/api/posts")
            || path.startsWith("/api/comments"));
  }
}
