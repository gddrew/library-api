package com.randomlake.library.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

@Setter
@Getter
public abstract class BaseModel {

  @LastModifiedDate private LocalDateTime lastUpdateDate;
}
