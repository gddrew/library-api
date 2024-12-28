package com.randomlake.library.repository;

import com.randomlake.library.dto.ReportLoanPatronMedia;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
public class LoanRepositoryCustomImpl implements LoanRepositoryCustom {

  private final MongoTemplate mongoTemplate;

  public LoanRepositoryCustomImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public List<ReportLoanPatronMedia> getReportLoanPatronMedia(Optional<Integer> patronIdOpt) {

    List<AggregationOperation> pipeline = new ArrayList<>();

    // Match stage for patronId (if present)
    Criteria criteria = new Criteria();
    patronIdOpt.ifPresent(patronId -> criteria.and("patronId").is(patronId));
    pipeline.add(Aggregation.match(criteria));

    // Lookup for patrons
    pipeline.add(Aggregation.lookup("patrons", "patronId", "patronId", "patronInfo"));

    // Unwind patronInfo
    pipeline.add(Aggregation.unwind("patronInfo"));

    // Lookup for media items
    pipeline.add(Aggregation.lookup("media", "items.mediaId", "mediaId", "mediaInfo"));

    // Project fields using raw Document for nested array mapping
    Document itemsMapping =
        new Document(
            "$map",
            new Document()
                .append("input", "$items")
                .append("as", "item")
                .append(
                    "in",
                    new Document("mediaId", "$$item.mediaId")
                        .append("checkoutDate", "$$item.checkoutDate")
                        .append("dueDate", "$$item.dueDate")
                        .append("returnDate", "$$item.returnDate")
                        .append("status", "$$item.status")
                        .append(
                            "mediaDetails",
                            new Document(
                                "$let",
                                new Document()
                                    .append(
                                        "vars",
                                        new Document(
                                            "media",
                                            new Document(
                                                "$arrayElemAt",
                                                List.of(
                                                    new Document(
                                                        "$filter",
                                                        new Document()
                                                            .append("input", "$mediaInfo")
                                                            .append("as", "media")
                                                            .append(
                                                                "cond",
                                                                new Document(
                                                                    "$eq",
                                                                    List.of(
                                                                        "$$media.mediaId",
                                                                        "$$item.mediaId")))),
                                                    0))))
                                    .append(
                                        "in",
                                        new Document()
                                            .append("mediaTitle", "$$media.media_title")
                                            .append("authorName", "$$media.author_name")
                                            .append("isbnId", "$$media.isbn_id")
                                            .append(
                                                "classificationCategory",
                                                "$$media.classification_category")
                                            .append(
                                                "classificationSubcategory",
                                                "$$media.classification_subcategory")
                                            .append("mediaType", "$$media.media_type")
                                            .append("mediaFormat", "$$media.media_format"))))));
    Document projectFields =
        new Document("loanId", 1)
            .append("patronId", 1)
            .append("patronName", "$patronInfo.patron_name")
            .append("loanStatus", "$status")
            .append("items", itemsMapping);

    // Build the $project stage
    Document projectStage = new Document("$project", projectFields);

    // Create a custom AggregationOperation
    AggregationOperation projectOperation = context -> projectStage;

    // Add the custom project operation to the pipeline
    pipeline.add(projectOperation);

    // Execute the aggregation and map results
    Aggregation aggregation = Aggregation.newAggregation(pipeline);
    return mongoTemplate
        .aggregate(aggregation, "loans", ReportLoanPatronMedia.class)
        .getMappedResults();
  }
}
