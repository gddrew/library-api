package com.randomlake.library.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class EmailRequest {

  @Setter private String from;

  @Setter private String to;

  @Setter private String subject;

  @Setter private String body;
}
