package com.randomlake.library.repository;

import com.randomlake.library.enums.PatronStatus;
import com.randomlake.library.model.Patron;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PatronRepository extends MongoRepository<Patron, String> {

  Optional<Patron> findByPatronId(int patronId);

  List<Patron> findByPatronName(String patronName);

  List<Patron> findByDateOfBirth(LocalDate dateOfBirth);

  List<Patron> findByPatronNameAndDateOfBirth(String patronName, LocalDate dateOfBirth);

  @Query("{$or: [{'telephone_home': ?0}, {'telephone_mobile': ?0}]}")
  List<Patron> findByTelephone(String telephone);

  List<Patron> findByEmailAddress(String emailAddress);

  List<Patron> findByStatus(PatronStatus status);

  void deleteByPatronId(int patronId);
}
