package com.randomlake.library.config;

import java.time.Clock;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LibraryConfig {

  @Getter
  @Value("${library.id.code}")
  private String libraryIdCode;

  @Getter
  @Value("${media.barcode.prefix}")
  private String mediaBarcodePrefix;

  @Getter
  @Value("${card.barcode.prefix}")
  private String cardBarcodePrefix;

  /**
   * Provides a system default Clock bean.
   *
   * @return Clock instance representing the system default time zone.
   */
  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}
