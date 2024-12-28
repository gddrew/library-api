package com.randomlake.library.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CheckDigitCalculatorTest {

  private CheckDigitCalculator checkDigitCalculator;

  @BeforeEach
  public void setup() {
    checkDigitCalculator = new CheckDigitCalculator();
  }

  @Test
  public void testCalculateCheckDigit_ValidNumber() {
    String number = "1234567";
    int expectedCheckDigit = 4;

    int result = checkDigitCalculator.calculateCheckDigit(number);
    assertEquals(expectedCheckDigit, result);
  }
}
