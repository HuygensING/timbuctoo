package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePromotedDataSet.class)
@JsonDeserialize(as = ImmutablePromotedDataSet.class)
public interface PromotedDataSet {

  String getDataSetId();

  String getOwnerId();

  String getCombinedId();

  @Value.Auxiliary
  boolean isPromoted();
  
  Optional<String> role = Optional.empty();

  static PromotedDataSet promotedDataSet(String ownerId, String dataSetId, boolean promoted) {
    return ImmutablePromotedDataSet.builder()
      .combinedId(ownerId + "_" + dataSetId)
      .ownerId(ownerId)
      .dataSetId(dataSetId)
      .isPromoted(promoted)
      .build();
  }
}
