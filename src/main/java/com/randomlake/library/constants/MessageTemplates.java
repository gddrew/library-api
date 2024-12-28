package com.randomlake.library.constants;

public class MessageTemplates {
  public static final String DUE_IN_DAYS_TEMPLATE =
      """
                    The following items are due in %d days:
                    Media ID: %s Title: %s Due Date: %s.
                    """;

  public static final String DUE_TODAY_TEMPLATE =
      """
                    The following items are due today:
                    Media ID: %s Title: %s Due Date: %s.
                    """;

  public static final String PAST_DUE_TEMPLATE =
      """
                    The following items are %d days past due:
                    Media ID: %s Title: %s Due Date: %s.
                    """;

  public static final String PAST_DUE_WITH_WARNING_TEMPLATE =
      """
                    The following items are %d days past due:
                    Media ID: %s Title: %s Due Date: %s.
                    Please return the item to avoid penalties, including revocation of your borrowing privileges.
                    """;

  public static final String ACCOUNT_SUSPENDED_TEMPLATE =
      """
                    Important Alert.
                    The following items are %d days past due:
                    Media ID: %s Title: %s Due Date: %s.
                    Your account is suspended, and your borrowing privileges are revoked. Please contact the library.
                    """;
}
