package nl.knaw.huygens.timbuctoo.v5.dataset;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePromotedDataSet.class)
@JsonDeserialize(as = ImmutablePromotedDataSet.class)
public interface PromotedDataSet {

  String getName();

  @Value.Auxiliary
  boolean isPromoted();
  
  Optional<String> role = null;

  static PromotedDataSet create(String name, boolean promoted) {
    return ImmutablePromotedDataSet.builder()
      .name(name)
      .isPromoted(promoted)
      .build();
  }
}
