package me.dio.credit.application.system.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Swagger3Config {

  @Bean
  fun publicApi(): GroupedOpenApi {
    return GroupedOpenApi.builder()
      .group("credit-application-api")
      .packagesToScan("me.dio.credit.application.system.controller")
      .pathsToMatch("/api/**")
      .build()
  }

  @Bean
  fun openApi(): OpenAPI {
    val contact = Contact().name("Credit API").email("support@example.com")
    val info = Info().title("Credit Application System API").version("v1").description("API to manage customers and credits").contact(contact)

    val bearerScheme = SecurityScheme()
      .type(SecurityScheme.Type.HTTP)
      .scheme("bearer")
      .bearerFormat("JWT")

    val components = Components().addSecuritySchemes("bearerAuth", bearerScheme)
    val securityRequirement = SecurityRequirement().addList("bearerAuth")

    return OpenAPI().components(components).addSecurityItem(securityRequirement).info(info)
  }
}
