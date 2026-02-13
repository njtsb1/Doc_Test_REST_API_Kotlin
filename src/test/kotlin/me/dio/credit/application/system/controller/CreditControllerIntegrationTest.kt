package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CreditControllerIntegrationTest {

  @Autowired
  private lateinit var mockMvc: MockMvc

  @Autowired
  private lateinit var objectMapper: ObjectMapper

  @Autowired
  private lateinit var customerRepository: CustomerRepository

  @Autowired
  private lateinit var creditRepository: CreditRepository

  @BeforeEach
  fun setup() {
    creditRepository.deleteAll()
    customerRepository.deleteAll()
  }

  @Test
  fun `create credit flow returns 201 and creditCode in body`() {
    val customerDto = CustomerDto(
      firstName = "Alice",
      lastName = "Smith",
      cpf = "11122233344",
      income = BigDecimal("5000.00"),
      email = "alice.smith@example.com",
      password = "pwd",
      zipCode = "22222-222",
      street = "Third St"
    )

    val created = mockMvc.perform(
      post("/api/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(customerDto))
    ).andExpect(status().isCreated).andReturn()

    val location = created.response.getHeader("Location")!!
    val customerId = location.substringAfterLast("/").toLong()

    val creditDto = CreditDto(
      creditValue = BigDecimal("1000.00"),
      dayFirstInstallment = LocalDate.now().plusDays(10),
      numberOfInstallments = 12,
      customerId = customerId
    )

    mockMvc.perform(
      post("/api/credits")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(creditDto))
    )
      .andExpect(status().isCreated)
      .andExpect(jsonPath("$.creditCode").exists())
  }

  @Test
  fun `list credits by customer returns 200 and list`() {
    // create customer and credit as above
    val customerDto = CustomerDto(
      firstName = "Bob",
      lastName = "Brown",
      cpf = "55566677788",
      income = BigDecimal("4000.00"),
      email = "bob.brown@example.com",
      password = "pwd",
      zipCode = "33333-333",
      street = "Fourth St"
    )

    val created = mockMvc.perform(
      post("/api/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(customerDto))
    ).andExpect(status().isCreated).andReturn()

    val location = created.response.getHeader("Location")!!
    val customerId = location.substringAfterLast("/").toLong()

    val creditDto = CreditDto(
      creditValue = BigDecimal("2000.00"),
      dayFirstInstallment = LocalDate.now().plusDays(5),
      numberOfInstallments = 6,
      customerId = customerId
    )

    mockMvc.perform(
      post("/api/credits")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(creditDto))
    ).andExpect(status().isCreated)

    mockMvc.perform(get("/api/credits").param("customerId", customerId.toString()))
      .andExpect(status().isOk)
      .andExpect(jsonPath("$[0].creditValue").exists())
  }
}