package com.randomlake.library.util;

import org.springframework.stereotype.Component;

@Component
public class CheckDigitCalculator {

  /**
   * Calculates the check digit for a given number by using a Modulo 11 algorithm.
   *
   * @param number The number to calculate the check digit for.
   * @return The check digit.
   */
  public int calculateCheckDigit(String number) {
    int sum = 0;
    int weight = 2;

    // Start from the rightmost digit and move left
    for (int i = number.length() - 1; i >= 0; i--) {
      int digit = Character.getNumericValue(number.charAt(i));
      sum += digit * weight;
      weight++;
      if (weight > 7) { // Weights are from 2 to 7, then they repeat
        weight = 2;
      }
    }

    int remainder = sum % 11;

    return (remainder == 0 || remainder == 1) ? 0 : 11 - remainder;
  }
}
