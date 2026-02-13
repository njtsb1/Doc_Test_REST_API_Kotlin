package me.dio.credit.application.system.repository

import me.dio.credit.application.system.entity.Credit
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface CreditRepository : JpaRepository<Credit, Long> {
  fun findAllByCustomerId(customerId: Long, pageable: Pageable): Page<Credit>
  fun findByCreditCode(creditCode: UUID): Optional<Credit>
}