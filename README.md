# DWP-Cinema-ticket-JAVA-

# Overview
This project implements a TicketService that:
- Validates ticket purchase requests
- Calculates total payment
- Calculates seat reservations
- Integrates with external payment and seat services

# Design Decisions
- Used constructor injection for testability
- Created PurchaseSummary to simplify aggregation logic
- Applied strict validation for business rules

# Assumptions
- External services always succeed
- Account IDs > 0 are valid

# Testing
- JUnit 5 for testing
- Mockito for mocking dependencies
- Tests cover:
  - Valid scenarios
  - Invalid inputs
  - Business rules
  - Payment and seat calculations

# How to Run
Run tests using:
- Java 11+
- Maven
