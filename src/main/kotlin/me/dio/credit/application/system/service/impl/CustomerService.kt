package me.dio.credit.application.system.service.impl

import me.dio.credit.application.system.dto.request.CustomerDto
import me.dio.credit.application.system.dto.request.CustomerUpdateDto
import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.exception.BusinessException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

@Service
class CustomerService(
  private val customerRepository: CustomerRepository,
  private val passwordEncoder: PasswordEncoder
) {

  @Transactional
  fun create(dto: CustomerDto): Customer {
    if (customerRepository.existsByCpf(dto.cpf)) throw BusinessException("CPF already registered")
    if (customerRepository.existsByEmail(dto.email)) throw BusinessException("Email already registered")

    val customer = dto.toEntity()
    customer.password = passwordEncoder.encode(dto.password)
    return customerRepository.save(customer)
  }

  @Transactional
  fun update(id: Long, dto: CustomerUpdateDto): Customer {
    val customer = customerRepository.findById(id).orElseThrow { BusinessException("Customer not found") }
    dto.applyTo(customer)
    return customerRepository.save(customer)
  }

  fun findById(id: Long): Customer =
    customerRepository.findById(id).orElseThrow { BusinessException("Customer not found") }

  @Transactional
  fun delete(id: Long) {
    val customer = findById(id)
    customerRepository.delete(customer)
  }
}

@Service
class CreditService(
  private val creditRepository: CreditRepository,
  private val customerRepository: CustomerRepository,
  private val clock: Clock
) {

  @Transactional
  fun create(dto: CreditDto): Credit {
    // Load real customer
    val customer = customerRepository.findById(dto.customerId)
      .orElseThrow { BusinessException("Customer not found") }

    // Business rules
    if (dto.numberOfInstallments < 1 || dto.numberOfInstallments > 48) {
      throw BusinessException("Number of installments must be between 1 and 48")
    }

    val today = LocalDate.now(clock)
    val maxAllowed = today.plusMonths(3)
    if (dto.dayFirstInstallment.isAfter(maxAllowed)) {
      throw BusinessException("First installment must be no later than ${maxAllowed}")
    }

    val credit = dto.toEntity()
    credit.customer = customer
    if (credit.creditCode == null) credit.creditCode = UUID.randomUUID()
    return creditRepository.save(credit)
  }

  fun findAllByCustomer(customerId: Long, pageable: Pageable): Page<Credit> {
    if (!customerRepository.existsById(customerId)) throw BusinessException("Customer not found")
    return creditRepository.findAllByCustomerId(customerId, pageable)
  }

  fun findByCreditCode(customerId: Long, creditCode: UUID): Credit {
    val credit = creditRepository.findByCreditCode(creditCode)
      .orElseThrow { BusinessException("Credit not found") }
    if (credit.customer?.id != customerId) throw BusinessException("Credit does not belong to customer")
    return credit
  }
}