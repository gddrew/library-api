package com.randomlake.library.model;

import com.randomlake.library.enums.PatronStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = true)
@Document(collection = "patrons")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Patron extends BaseModel {

  @Id private ObjectId id;

  private int patronId;

  @CreatedDate private LocalDateTime created_date;

  @Field("patron_name")
  private String patronName;

  @Field("date_of_birth")
  private LocalDate dateOfBirth;

  @Field("street_address")
  private String streetAddress;

  @Field("city_name")
  private String cityName;

  @Field("state_name")
  private String stateName;

  @Field("zip_code")
  private String zipCode;

  @Field("telephone_home")
  private String telephoneHome;

  @Field("telephone_mobile")
  private String telephoneMobile;

  @Field("email_address")
  private String emailAddress;

  @Field("contact_method")
  private String contactMethod;

  @Field("patron_status")
  private PatronStatus status;

  // List to store the IDs of checked out items
  @Field("checked_out_items")
  private List<Integer> checkedOutItems = new ArrayList<>();

  public String getId() {
    return id != null ? id.toHexString() : null;
  }

  public void setId(String id) {
    this.id = id != null ? new ObjectId(id) : null;
  }
}
