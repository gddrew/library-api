package com.randomlake.library.repository;

import com.randomlake.library.model.ApplicationUser;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<ApplicationUser, String> {

  Optional<ApplicationUser> findByUsername(String username);
}
