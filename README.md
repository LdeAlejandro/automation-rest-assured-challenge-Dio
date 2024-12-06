

# QA - Rest Assured Test Automation for a Booking App

## Overview

This repository showcases my skills in automated testing using **Rest Assured**. The project demonstrates a comprehensive set of tests for a booking application, covering various functionalities such as creating and updating bookings, health checks, and authentication.

## Project Structure

The main structure of this project includes:

- **Entities**: Classes representing booking data, user details, and other related information (`Booking`, `BookingDates`, `User`, `TokenUser`).
- **Rest Assured Configuration**: Configurations for HTTP requests and responses, logging filters, and validation.
- **Test Classes**: The `BookingTests` class contains all test cases, including:
  - Health checks (`PingHealthCheck_201OK`)
  - User authentication (`CreateToken_OK`)
  - CRUD operations for bookings (`CreateBooking_WithValidData_returnOk`, `getBookingById_Booking_Exist_returnOk`, etc.)


## Test Cases

### Sample Test Methods
- **PingHealthCheck_201OK**: Verifies that the server is up and responding with a status code of `201 OK`.
- **CreateToken_OK**: Tests the generation of an authentication token.
- **CreateBooking_WithValidData_returnOk**: Creates a booking and verifies the response.
- **getBookingById_Booking_Exist_returnOk**: Retrieves a booking by its ID and validates its details.
- **UpdateBooking_returnOk**: Updates an existing booking and verifies the update.
- **PartialUpdateBooking_returnOk**: Performs a partial update of a booking.
- **DeleteBookingById_returnOk**: Deletes a booking by its ID and confirms the deletion.

