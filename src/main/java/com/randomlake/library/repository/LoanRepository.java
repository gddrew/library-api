package com.randomlake.library.repository;

import com.randomlake.library.enums.ItemStatus;
import com.randomlake.library.enums.LoanStatus;
import com.randomlake.library.model.Loan;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanRepository extends MongoRepository<Loan, String>, LoanRepositoryCustom {

  // Find loans by due date across all loan items
  @Query("{ 'items.dueDate' : ?0 }")
  List<Loan> findByItemsDueDate(LocalDate dueDate);

  // Find loans by loan ID
  Optional<Loan> findByLoanId(int loanId);

  // Find loans by patron ID
  List<Loan> findByPatronId(int patronId);

  // Find first active loan by patron
  Optional<Loan> findFirstByPatronIdAndStatus(int patronId, LoanStatus status);

  // Find loans by mediaId, checking within the LoanItem array
  @Query("{ 'items.mediaId' : ?0 }")
  List<Loan> findByMediaId(int mediaId);

  // Find active loan for a patron with specific media item
  @Query("{ 'patronId': ?0, 'status': ?1, 'items.mediaId': ?2, 'items.status': ?3 }")
  Optional<Loan> findActiveByPatronIdAndMediaId(
      int patronId, LoanStatus status, int mediaId, ItemStatus itemStatus);

  // Find active loans with items due on a specific date and item status CHECKED_OUT
  @Query("{ 'status': ?1, 'items': { $elemMatch: { 'dueDate' : ?0, 'status': ?2 } } }")
  List<Loan> findActiveLoansByDueDateAndItemStatus(
      LocalDate dueDate, LoanStatus loanStatus, ItemStatus itemStatus);

  // Find loans that have items overdue by a certain date
  @Query("{ 'items': { $elemMatch: { 'dueDate' : { $lt : ?0 }, 'status': 'CHECKED_0UT' } } }")
  List<Loan> findLoansWithOverdueItems(LocalDate overdueDate);

  // Count active overdue items for a patron
  @Query(
      value =
          "{ 'patronId': ?0, 'items': { $elemMatch: { 'dueDate': { $lt: ?1 }, 'status': 'CHECKED_OUT' } } }",
      count = true)
  long countActiveOverdueItemsByPatron(int patronId, LocalDate currentDate);

  // Delete loan by loanId
  boolean existsByLoanId(int loanId);

  boolean existsByPatronIdAndStatus(int patronId, LoanStatus status);

  void deleteByLoanId(int loanId);
}
