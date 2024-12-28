package com.randomlake.library.repository;

import com.randomlake.library.dto.ReportCardPatron;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
public class CardRepositoryCustomImpl implements CardRepositoryCustom {

  private static final Logger log = LoggerFactory.getLogger(CardRepositoryCustomImpl.class);

  private final MongoTemplate mongoTemplate;

  public CardRepositoryCustomImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public List<ReportCardPatron> getReportCardPatron(
      Optional<Integer> cardIdOpt, Optional<Integer> patronIdOpt) {

    // Initialize a list to hold aggregation operations
    List<AggregationOperation> operations = new ArrayList<>();

    // Optional $match stage for filtering by Card ID and/or Patron ID
    if (cardIdOpt.isPresent() || patronIdOpt.isPresent()) {
      Criteria criteria = new Criteria();

      List<Criteria> criteriaList = new ArrayList<>();

      cardIdOpt.ifPresent(cardId -> criteriaList.add(Criteria.where("cardId").is(cardId)));
      patronIdOpt.ifPresent(patronId -> criteriaList.add(Criteria.where("patron_id").is(patronId)));

      // Combine criteria with AND logic
      if (!criteriaList.isEmpty()) {
        criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        MatchOperation matchOperation = Aggregation.match(criteria);
        operations.add(matchOperation);
      }
    }

    // Basic $lookup operation
    LookupOperation lookupOperation =
        LookupOperation.newLookup()
            .from("patrons") // Collection to join
            .localField("patron_id") // Field from 'cards' collection
            .foreignField("patronId") // Field from 'patrons' collection
            .as("p"); // Alias for joined data

    operations.add(lookupOperation);

    // $unwind operation to deconstruct the 'p' array
    UnwindOperation unwindOperation = Aggregation.unwind("p");
    operations.add(unwindOperation);

    // $project operation to shape the final output
    ProjectionOperation projectionOperation =
        Aggregation.project()
            .andExclude("_id")
            .and("cardId")
            .as("cardId")
            .and("card_status")
            .as("cardStatus")
            .and("p.patronId")
            .as("patronId")
            .and("p.patron_status")
            .as("patronStatus")
            .and("p.patron_name")
            .as("patronName")
            .and("p.street_address")
            .as("streetAddress")
            .and("p.city_name")
            .as("cityName")
            .and("p.state_name")
            .as("stateName")
            .and("p.zip_code")
            .as("zipCode")
            .and("p.telephone_home")
            .as("telephoneHome")
            .and("p.telephone_mobile")
            .as("telephoneMobile")
            .and("p.email_address")
            .as("emailAddress");

    operations.add(projectionOperation);

    // Build the aggregation pipeline
    Aggregation aggregation = Aggregation.newAggregation(operations);

    // Execute the aggregation
    AggregationResults<ReportCardPatron> results =
        mongoTemplate.aggregate(
            aggregation,
            "cards", // Collection name
            ReportCardPatron.class);

    List<ReportCardPatron> mappedResults = results.getMappedResults();

    return results.getMappedResults();
  }
}
