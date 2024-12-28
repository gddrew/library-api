package com.randomlake.library.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "counters")
public class Counter {

  @Getter @Setter @Id private String id;
  private int sequence_value;

  public int getSequenceValue() {
    return sequence_value;
  }

  public void setSequenceValue(int sequence_value) {
    this.sequence_value = sequence_value;
  }
}
