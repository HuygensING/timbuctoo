package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePromotedDataSet.class)
@JsonDeserialize(as = ImmutablePromotedDataSet.class)
public interface PromotedDataSet {

  String VALID_ID = "^([a-zA-Z0-9]+_)*[a-zA-Z0-9]+$";

  String getDataSetId();

  String getOwnerId();

  String getCombinedId();

  @Value.Auxiliary
  boolean isPromoted();
  
  Optional<String> role = Optional.empty();

  static PromotedDataSet promotedDataSet(String ownerId, String dataSetId, boolean promoted) {
    if (!ownerId.matches(VALID_ID) || !dataSetId.matches(VALID_ID)) {
      throw new IllegalArgumentException("Owner id and dataSet id should match " + VALID_ID);
    }

    return ImmutablePromotedDataSet.builder()
      .combinedId(ownerId + "__" + dataSetId)
      .ownerId(ownerId)
      .dataSetId(dataSetId)
      .isPromoted(promoted)
      .build();
  }
}
