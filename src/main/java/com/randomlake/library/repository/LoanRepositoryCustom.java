package com.randomlake.library.repository;

import com.randomlake.library.dto.ReportLoanPatronMedia;
import java.util.List;
import java.util.Optional;

public interface LoanRepositoryCustom {
  List<ReportLoanPatronMedia> getReportLoanPatronMedia(Optional<Integer> loanId);
}
