package me.dio.credit.application.system.service

import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.dto.request.CustomerUpdateDto
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.impl.CreditService
import me.dio.credit.application.system.service.impl.CustomerService
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Optional
import java.util.UUID

class AdditionalServiceTests {

  // Common fixed clock for deterministic date tests
  private val fixedClock = Clock.fixed(Instant.parse("2026-02-13T00:00:00Z"), ZoneId.of("UTC"))

  @Test
  fun `should throw when creating customer with duplicate cpf`() {
    val customerRepository: CustomerRepository = mock()
    val passwordEncoder = org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
    val service = CustomerService(customerRepository, passwordEncoder)

    val dto = CustomerDto(
      firstName = "Dup",
      lastName = "Cpf",
      cpf = "11122233344",
      income = BigDecimal("1000.00"),
      email = "unique@example.com",
      password = "pwd",
      zipCode = "00000-000",
      street = "Street"
    )

    whenever(customerRepository.existsByCpf(dto.cpf)).thenReturn(true)
    whenever(customerRepository.existsByEmail(dto.email)).thenReturn(false)

    assertThrows(BusinessException::class.java) { service.create(dto) }
  }

  @Test
  fun `should throw when creating customer with duplicate email`() {
    val customerRepository: CustomerRepository = mock()
    val passwordEncoder = org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
    val service = CustomerService(customerRepository, passwordEncoder)

    val dto = CustomerDto(
      firstName = "Dup",
      lastName = "Email",
      cpf = "99988877766",
      income = BigDecimal("1500.00"),
      email = "dup@example.com",
      password = "pwd",
      zipCode = "00000-000",
      street = "Street"
    )

    whenever(customerRepository.existsByCpf(dto.cpf)).thenReturn(false)
    whenever(customerRepository.existsByEmail(dto.email)).thenReturn(true)

    assertThrows(BusinessException::class.java) { service.create(dto) }
  }

  @Test
  fun `should throw when number of installments exceeds 48`() {
    val creditRepository: CreditRepository = mock()
    val customerRepository: CustomerRepository = mock()
    val service = CreditService(creditRepository, customerRepository, fixedClock)

    val customerId = 1L
    whenever(customerRepository.findById(customerId)).thenReturn(Optional.of(Customer(id = customerId)))

    val dto = CreditDto(
      creditValue = BigDecimal("5000.00"),
      dayFirstInstallment = LocalDate.now(fixedClock).plusDays(10),
      numberOfInstallments = 49,
      customerId = customerId
    )

    assertThrows(BusinessException::class.java) { service.create(dto) }
  }

  @Test
  fun `should allow first installment exactly at three months limit`() {
    val creditRepository: CreditRepository = mock()
    val customerRepository: CustomerRepository = mock()
    val service = CreditService(creditRepository, customerRepository, fixedClock)

    val customerId = 2L
    val customer = Customer(id = customerId)
    whenever(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))

    val dto = CreditDto(
      creditValue = BigDecimal("1200.00"),
      dayFirstInstallment = LocalDate.now(fixedClock).plusMonths(3),
      numberOfInstallments = 12,
      customerId = customerId
    )

    whenever(creditRepository.save(any())).thenAnswer { invocation ->
      val arg = invocation.getArgument(0) as Credit
      arg.id = 1L
      arg.creditCode = UUID.randomUUID()
      arg
    }

    assertDoesNotThrow { service.create(dto) }
  }
}