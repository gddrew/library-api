package com.randomlake.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.randomlake.library.enums.ItemStatus;
import com.randomlake.library.enums.LoanStatus;
import com.randomlake.library.model.Loan;
import com.randomlake.library.model.Media;
import com.randomlake.library.repository.LoanRepository;
import com.randomlake.library.repository.MediaRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class NotificationServiceTest {

  @Mock private LoanRepository loanRepository;

  @Mock private MediaRepository mediaRepository;

  @Mock private NotificationSender notificationSender;

  private NotificationService notificationService;

  private Clock clock;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    LocalDate fixedDate = LocalDate.of(2023, 10, 1);
    clock =
        Clock.fixed(
            fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
    notificationService =
        new NotificationService(loanRepository, mediaRepository, notificationSender, clock);
  }

  @Test
  public void testCheckDueItemsAndNotifyPatron_dueSoon() {
    // Arrange
    LocalDate fixedDate = LocalDate.of(2023, 10, 1);
    LocalDate dueDate = fixedDate.plusDays(3); // 2023-10-04

    Loan loan = new Loan();
    loan.setPatronId(1);
    loan.setStatus(LoanStatus.ACTIVE);

    Loan.LoanItem loanItem = new Loan.LoanItem();
    loanItem.setMediaId(100);
    loanItem.setDueDate(dueDate);
    loanItem.setStatus(ItemStatus.CHECKED_OUT);

    loan.setItems(List.of(loanItem));

    Media media = new Media();
    media.setMediaId(100);
    media.setMediaTitle("Hit the Sky");

    // Mock the repository methods
    when(loanRepository.findActiveLoansByDueDateAndItemStatus(
            any(LocalDate.class), eq(LoanStatus.ACTIVE), eq(ItemStatus.CHECKED_OUT)))
        .thenReturn(Collections.emptyList());
    when(loanRepository.findActiveLoansByDueDateAndItemStatus(
            dueDate, LoanStatus.ACTIVE, ItemStatus.CHECKED_OUT))
        .thenReturn(List.of(loan));
    when(mediaRepository.findAllByMediaIdIn(Set.of(100))).thenReturn(List.of(media));

    // Act
    notificationService.checkDueItemsAndNotifyPatron();

    // Assert
    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(notificationSender, times(1)).sendNotification(eq(1), messageCaptor.capture());

    String expectedMessage =
        String.format(
            "The following items are due in %d days:\nMedia ID: %s Title: %s Due Date: %s.\n",
            3, 100, "Hit the Sky", dueDate);
    assertEquals(expectedMessage, messageCaptor.getValue());
  }

  @Test
  public void testCheckDueItemsAndNotifyPatron_noLoansDue() {
    // Arrange
    when(loanRepository.findActiveLoansByDueDateAndItemStatus(
            any(LocalDate.class), eq(LoanStatus.ACTIVE), eq(ItemStatus.CHECKED_OUT)))
        .thenReturn(Collections.emptyList());

    // Act
    notificationService.checkDueItemsAndNotifyPatron();

    // Assert
    verify(notificationSender, times(0)).sendNotification(anyInt(), anyString());
  }

  @Test
  public void testCheckDueItemsAndNotifyPatron_pastDueNotification() {
    // Arrange
    LocalDate fixedDate = LocalDate.of(2023, 10, 1);
    LocalDate pastDueDate = fixedDate.minusDays(5); // 2023-09-26

    Loan loan = new Loan();
    loan.setPatronId(2);
    loan.setStatus(LoanStatus.ACTIVE);

    Loan.LoanItem loanItem = new Loan.LoanItem();
    loanItem.setMediaId(101);
    loanItem.setDueDate(pastDueDate);
    loanItem.setStatus(ItemStatus.CHECKED_OUT);

    loan.setItems(List.of(loanItem));

    Media media = new Media();
    media.setMediaId(101);
    media.setMediaTitle("1984");

    // Mock the repository methods
    when(loanRepository.findActiveLoansByDueDateAndItemStatus(
            any(LocalDate.class), eq(LoanStatus.ACTIVE), eq(ItemStatus.CHECKED_OUT)))
        .thenReturn(Collections.emptyList());
    when(loanRepository.findActiveLoansByDueDateAndItemStatus(
            pastDueDate, LoanStatus.ACTIVE, ItemStatus.CHECKED_OUT))
        .thenReturn(List.of(loan));
    when(mediaRepository.findAllByMediaIdIn(Set.of(101))).thenReturn(List.of(media));

    // Act
    notificationService.checkDueItemsAndNotifyPatron();

    // Assert
    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(notificationSender, times(1)).sendNotification(eq(2), messageCaptor.capture());

    String expectedMessage =
        String.format(
            "The following items are %d days past due:\nMedia ID: %s Title: %s Due Date: %s.\n",
            5, 101, "1984", pastDueDate);
    assertEquals(expectedMessage, messageCaptor.getValue());
  }

  @Test
  public void testCheckDueItemsAndNotifyPatron_pastDueAccountSuspendedNotification() {
    // Arrange
    LocalDate fixedDate = LocalDate.of(2023, 10, 1);
    LocalDate pastDueDate = fixedDate.minusDays(30); // 2023-09-01

    Loan loan = new Loan();
    loan.setPatronId(3);
    loan.setStatus(LoanStatus.ACTIVE);

    Loan.LoanItem loanItem = new Loan.LoanItem();
    loanItem.setMediaId(102);
    loanItem.setDueDate(pastDueDate);
    loanItem.setStatus(ItemStatus.CHECKED_OUT);

    loan.setItems(List.of(loanItem));

    Media media = new Media();
    media.setMediaId(102);
    media.setMediaTitle("Bright Flows the River");

    // Mock the repository methods
    when(loanRepository.findActiveLoansByDueDateAndItemStatus(
            any(LocalDate.class), eq(LoanStatus.ACTIVE), eq(ItemStatus.CHECKED_OUT)))
        .thenReturn(Collections.emptyList());
    when(loanRepository.findActiveLoansByDueDateAndItemStatus(
            pastDueDate, LoanStatus.ACTIVE, ItemStatus.CHECKED_OUT))
        .thenReturn(List.of(loan));
    when(mediaRepository.findAllByMediaIdIn(Set.of(102))).thenReturn(List.of(media));

    // Act
    notificationService.checkDueItemsAndNotifyPatron();

    // Assert
    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(notificationSender, times(1)).sendNotification(eq(3), messageCaptor.capture());

    String expectedMessage =
        String.format(
            "Important Alert.\nThe following items are %d days past due:\nMedia ID: %s Title: %s Due Date: %s.\n"
                + "Your account is suspended, and your borrowing privileges are revoked. Please contact the library.\n",
            30, 102, "Bright Flows the River", pastDueDate);
    assertEquals(expectedMessage, messageCaptor.getValue());
  }
}
