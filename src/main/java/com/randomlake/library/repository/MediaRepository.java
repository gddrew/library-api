package com.randomlake.library.repository;

import com.randomlake.library.model.Media;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaRepository extends MongoRepository<Media, ObjectId> {

  Optional<Media> findByMediaId(int mediaId);

  List<Media> findAllByMediaIdIn(Collection<Integer> mediaIds);

  List<Media> findByMediaTitle(String mediaTitle);

  List<Media> findByAuthorName(String authorName);

  List<Media> findByPublisherName(String publisherName);

  List<Media> findByIsbnId(String isbnId);

  void deleteById(int mediaId);
}
