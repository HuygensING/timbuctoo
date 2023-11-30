package nl.knaw.huygens.timbuctoo.serializable.dto;

import nl.knaw.huygens.timbuctoo.serializable.dto.ImmutableQueryContainer;
import org.immutables.value.Value;

import java.util.Map;

/**
 * Represents a top-level key that is used to retrieve data
 */
@Value.Immutable
public interface QueryContainer {
  Map<String, Serializable> getContents();

  static QueryContainer queryContainer(Map<String, Serializable> contents) {
    return ImmutableQueryContainer.builder().contents(contents).build();
  }
}
