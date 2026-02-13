import me.dio.credit.application.system.dto.request.CreditDto
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.enummeration.Status
import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.impl.CreditService
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Optional
import java.util.UUID

class CreditServiceTest {

  private val creditRepository: CreditRepository = mock()
  private val customerRepository: CustomerRepository = mock()
  private val fixedClock = Clock.fixed(Instant.parse("2026-02-13T00:00:00Z"), ZoneId.of("UTC"))
  private val service = CreditService(creditRepository, customerRepository, fixedClock)

  @Test
  fun `should throw when first installment after 3 months`() {
    val customerId = 1L
    whenever(customerRepository.findById(customerId)).thenReturn(Optional.of(Customer(id = customerId)))

    val dto = CreditDto(
      creditValue = BigDecimal("1000"),
      dayFirstInstallment = LocalDate.now(fixedClock).plusMonths(4),
      numberOfInstallments = 12,
      customerId = customerId
    )

    assertThrows(BusinessException::class.java) { service.create(dto) }
  }
}