package com.randomlake.library.repository;

import com.randomlake.library.enums.FineType;
import com.randomlake.library.model.Fine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FineRepository extends MongoRepository<Fine, String> {

  boolean existsByPatronId(int patronId, int mediaId);

  boolean existsByPatronIdAndMediaIdAndFineType(int patronId, int mediaId, FineType fineType);

  @Query("{ 'patronId' : ?0 }")
  List<Fine> findByPatronId(int patronId);

  // Find fines that have not been paid or waived
  @Query("{ 'patronId' : ?0, 'isPaid' : false, 'isWaived' : false }")
  List<Fine> findActiveFinesByPatronId(int patronId);

  Optional<Fine> findByFineId(int fineId);
}
