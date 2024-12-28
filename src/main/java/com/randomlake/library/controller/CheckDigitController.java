package com.randomlake.library.controller;

import com.randomlake.library.util.CheckDigitCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check-digit")
public class CheckDigitController {

  private final CheckDigitCalculator checkDigitCalculator;

  // Inject the CheckDigitCalculator using constructor injection
  @Autowired
  public CheckDigitController(CheckDigitCalculator checkDigitCalculator) {
    this.checkDigitCalculator = checkDigitCalculator;
  }

  // Define an endpoint to calculate the check digit
  @GetMapping("/calculate")
  public ResponseEntity<String> calculateCheckDigit(@RequestParam String number) {
    try {
      // Ensure the input is a valid number
      if (!number.matches("\\d+")) {
        throw new IllegalArgumentException("Input must be a valid numeric string");
      }

      // Calculate check digit
      // Append the check digit to the supplied number
      int checkDigit = checkDigitCalculator.calculateCheckDigit(number);
      String result = number + checkDigit;

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}
