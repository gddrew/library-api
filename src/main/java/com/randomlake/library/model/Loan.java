package com.randomlake.library.model;

import com.randomlake.library.enums.ItemStatus;
import com.randomlake.library.enums.LoanStatus;
import com.randomlake.library.enums.TransactionType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "loans")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Loan {

  @Id private ObjectId id;

  @Indexed private int loanId;

  @Indexed private int patronId;

  @Indexed private LoanStatus status;

  private List<LoanItem> items = new ArrayList<>();
  private List<TransactionLog> transactionLog = new ArrayList<>();

  @CreatedDate private LocalDateTime createdDate;
  @LastModifiedDate private LocalDateTime lastUpdateDate;

  @Data
  public static class LoanItem {

    private int mediaId;
    private LocalDate checkoutDate;
    @Indexed private LocalDate dueDate;
    private LocalDate returnDate;
    @Indexed private ItemStatus status;
  }

  @Data
  public static class TransactionLog {

    private TransactionType transactionType;
    private LocalDateTime transactionDate;
    private List<Integer> mediaIds;
  }
}
