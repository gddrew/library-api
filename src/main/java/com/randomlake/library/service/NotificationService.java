package com.randomlake.library.service;

import com.randomlake.library.constants.MessageTemplates;
import com.randomlake.library.enums.ItemStatus;
import com.randomlake.library.enums.LoanStatus;
import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.model.Loan;
import com.randomlake.library.model.Media;
import com.randomlake.library.repository.LoanRepository;
import com.randomlake.library.repository.MediaRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

  private final LoanRepository loanRepository;
  private final MediaRepository mediaRepository;
  private final NotificationSender notificationSender;
  private final Clock clock;
  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

  public NotificationService(
      LoanRepository loanRepository,
      MediaRepository mediaRepository,
      NotificationSender notificationSender,
      Clock clock) {
    this.loanRepository = loanRepository;
    this.mediaRepository = mediaRepository;
    this.notificationSender = notificationSender;
    this.clock = clock;
  }

  @Scheduled(cron = "${item.due.notification.cron}")
  public void checkDueItemsAndNotifyPatron() {
    LocalDate today = LocalDate.now(clock);

    try {
      // Map to accumulate messages per patron across all due dates
      Map<Integer, StringBuilder> messagesPerPatron = new HashMap<>();

      // Map of due dates to their corresponding days offset
      Map<LocalDate, Integer> dueDates =
          Map.of(
              today.plusDays(3),
              3,
              today,
              0,
              today.minusDays(5),
              -5,
              today.minusDays(10),
              -10,
              today.minusDays(15),
              -15,
              today.minusDays(30),
              -30);

      // Process each due dates
      for (Map.Entry<LocalDate, Integer> entry : dueDates.entrySet()) {
        LocalDate dueDate = entry.getKey();
        int daysOffset = entry.getValue();
        notifyForDueDate(dueDate, daysOffset, messagesPerPatron);
      }

      // Send consolidated notifications per patron
      for (Map.Entry<Integer, StringBuilder> entry : messagesPerPatron.entrySet()) {
        int patronId = entry.getKey();
        String message = entry.getValue().toString();

        // Send the consolidated notification
        notificationSender.sendNotification(patronId, message);
        log.info("Items due notification sent to patron ID: {}", patronId);
      }
    } catch (Exception e) {
      log.error("An error occurred while sending due items notification", e);
    }
  }

  protected void notifyForDueDate(
      LocalDate dueDate, int daysOffset, Map<Integer, StringBuilder> messagesPerPatron) {
    // Fetch loans with items due on the specified date
    List<Loan> dueLoans =
        loanRepository.findActiveLoansByDueDateAndItemStatus(
            dueDate, LoanStatus.ACTIVE, ItemStatus.CHECKED_OUT);
    sendNotificationsForLoans(dueLoans, daysOffset, dueDate, messagesPerPatron);
  }

  public void sendNotificationsForLoans(
      List<Loan> loans,
      int daysOffset,
      LocalDate dueDateFilter,
      Map<Integer, StringBuilder> messagesPerPatron) {

    // Map media IDs to media titles
    Map<Integer, Media> mediaMap = getMediaMapFromLoans(loans);

    // Select the appropriate message template based on days offset
    String messageTemplate = getMessageForDaysOffset(daysOffset);

    // Loop through loans and accumulate messages
    for (Loan loan : loans) {
      int patronId = loan.getPatronId();
      for (Loan.LoanItem item : loan.getItems()) {
        if (item.getDueDate().equals(dueDateFilter) && item.getStatus() == ItemStatus.CHECKED_OUT) {

          int mediaId = item.getMediaId();
          Media media = mediaMap.get(mediaId);
          String title = media != null ? media.getMediaTitle() : "Unknown Title";
          LocalDate dueDate = item.getDueDate();

          // Construct the notification message
          String message = formatMessage(messageTemplate, daysOffset, mediaId, title, dueDate);

          // Accumulate message per patron across all due dates
          messagesPerPatron.computeIfAbsent(patronId, k -> new StringBuilder()).append(message);
        }
      }
    }
  }

  // Helper methods
  private String getMessageForDaysOffset(int daysOffset) {
    if (daysOffset == 3) {
      return MessageTemplates.DUE_IN_DAYS_TEMPLATE;
    } else if (daysOffset == 0) {
      return MessageTemplates.DUE_TODAY_TEMPLATE;
    } else if (daysOffset == -5 || daysOffset == -10) {
      return MessageTemplates.PAST_DUE_TEMPLATE;
    } else if (daysOffset == -15) {
      return MessageTemplates.PAST_DUE_WITH_WARNING_TEMPLATE;
    } else if (daysOffset == -30) {
      return MessageTemplates.ACCOUNT_SUSPENDED_TEMPLATE;
    } else {
      throw new IllegalArgumentException("Unsupported daysOffset: " + daysOffset);
    }
  }

  private String formatMessage(
      String template, int daysOffset, int mediaId, String title, LocalDate dueDate) {
    if (template.contains("%d")) {
      // Adjust daysOffset for past due messages
      int days = Math.abs(daysOffset);
      return String.format(template, days, mediaId, title, dueDate);
    } else {
      return String.format(template, mediaId, title, dueDate);
    }
  }

  private Map<Integer, Media> getMediaMapFromLoans(List<Loan> loans) {
    Set<Integer> mediaIds =
        loans.stream()
            .flatMap(loan -> loan.getItems().stream())
            .map(Loan.LoanItem::getMediaId)
            .collect(Collectors.toSet());

    List<Media> mediaList = mediaRepository.findAllByMediaIdIn(mediaIds);

    return mediaList.stream().collect(Collectors.toMap(Media::getMediaId, Function.identity()));
  }

  public void notifyPatronStatusChange(int patronId, PatronStatus status) {
    String message = "Your account status has been changed to " + status.name() + ".\n";
    notificationSender.sendNotification(patronId, message);
    log.info("Notification of status change sent to patron ID: {}", patronId);
  }
}
