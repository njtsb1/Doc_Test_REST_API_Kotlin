package me.dio.credit.application.system.configuration

import me.dio.credit.application.system.exception.BusinessException
import me.dio.credit.application.system.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {

  data class ErrorResponse(val message: String, val details: Map<String, String>? = null)

  @ExceptionHandler(NotFoundException::class)
  fun handleNotFound(ex: NotFoundException): ResponseEntity<ErrorResponse> =
    ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(ex.message ?: "Resource not found"))

  @ExceptionHandler(BusinessException::class)
  fun handleBusiness(ex: BusinessException): ResponseEntity<ErrorResponse> =
    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(ex.message ?: "Business rule violation"))

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
    val errors = ex.bindingResult.fieldErrors.associate { fe: FieldError -> fe.field to (fe.defaultMessage ?: "Invalid") }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("Validation failed", errors))
  }

  @ExceptionHandler(Exception::class)
  fun handleOther(ex: Exception): ResponseEntity<ErrorResponse> =
    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("Internal server error"))
}
