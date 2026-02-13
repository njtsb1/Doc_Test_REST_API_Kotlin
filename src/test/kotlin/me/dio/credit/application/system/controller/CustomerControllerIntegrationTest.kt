package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  @Autowired
  private lateinit var customerRepository: CustomerRepository

  @BeforeEach
  fun setup() {
    customerRepository.deleteAll()
  }

  @Test
  fun `POST create customer returns 201 and Location header`() {
    val dto = CustomerDto(
      firstName = "John",
      lastName = "Doe",
      cpf = "12345678909",
      income = BigDecimal("2000.00"),
      email = "john.doe@example.com",
      password = "password123",
      zipCode = "00000-000",
      street = "Main St"
    )

    val result = mockMvc.perform(
      post("/api/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto))
    )
      .andExpect(status().isCreated)
      .andExpect(header().exists("Location"))
      .andReturn()

    val location = result.response.getHeader("Location")
    assert(location != null)
  }

  @Test
  fun `GET profile returns 200 and correct fields`() {
    val dto = CustomerDto(
      firstName = "Jane",
      lastName = "Roe",
      cpf = "98765432100",
      income = BigDecimal("3000.00"),
      email = "jane.roe@example.com",
      password = "secret",
      zipCode = "11111-111",
      street = "Second St"
    )

    val post = mockMvc.perform(
      post("/api/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto))
    ).andExpect(status().isCreated).andReturn()

    val location = post.response.getHeader("Location")!!
    val id = location.substringAfterLast("/").toLong()

    mockMvc.perform(get("/api/customers/{id}", id))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.firstName").value("Jane"))
      .andExpect(jsonPath("$.email").value("jane.roe@example.com"))
  }
}