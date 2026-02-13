Daily learning

# Documenting and Testing Your REST API with Kotlin

Project developed at TQI Kotlin - Backend Developer Bootcamp, with the guidance of expert [Camila Cavalcante](https://github.com/cami-la/ "Camila Cavalcante").
Learning create a complete REST API, documenting endpoints using the OpenAPI specification and implementing automated tests to ensure the API functions correctly. The challenge encouraged the use of good programming practices and an understanding of fundamental concepts in testing and documenting REST APIs.

**Customer:**

- Register:

1. Request: firstName, lastName, cpf, income, email, password, zipCode e street
2. Response: String

- Edit registration:

1. Request: id, firstName, lastName, income, zipCode, street
2. Response: firstName, lastName, income, cpf, email, income, zipCode, street

- View profile:

1. Request: id
2. Response: firstName, lastName, income, cpf, email, income, zipCode, street

- Delete registration:

1. Request: id
2. Response: no return

**Loan Application (Credit):**

- Register:

1. Request: creditValue, dayFirstOfInstallment, numberOfInstallments e customerId
2. Response: String

- List all loan requests from a client:

1. Request: customerId
2. Response: creditCode, creditValue, numberOfInstallment

- View a loan:

1. Request: customerId e creditCode
2. Response: creditCode, creditValue, numberOfInstallment, status, emailCustomer e incomeCustomer

<img width="1536" height="1024" alt="Diagram" src="https://github.com/user-attachments/assets/5969dcfc-d7d9-48e4-9be2-fcba107339fc" />

Simplified UML Diagram of an API for a Credit Rating System

<img width="1536" height="1024" alt="Architecture" src="https://github.com/user-attachments/assets/d20b08bd-e4dc-4b0a-923d-e69fab177cdd" />

3-layer architecture Spring Boot project

## CHALLENGE

- Implement the following business rules for the loan application.:

1. The maximum number of installments allowed will be 48.
2. The first payment is due no later than 3 months after the current date.

[LICENSE](/LICENSE)

See [repositoty original](https://github.com/cami-la/credit-application-system).
