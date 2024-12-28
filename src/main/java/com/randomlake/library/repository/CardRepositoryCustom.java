package com.randomlake.library.repository;

import com.randomlake.library.dto.ReportCardPatron;
import java.util.List;
import java.util.Optional;

public interface CardRepositoryCustom {
  List<ReportCardPatron> getReportCardPatron(Optional<Integer> cardId, Optional<Integer> patronId);
}
