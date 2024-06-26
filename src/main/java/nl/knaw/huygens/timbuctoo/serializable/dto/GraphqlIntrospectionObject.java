package nl.knaw.huygens.timbuctoo.serializable.dto;

import nl.knaw.huygens.timbuctoo.serializable.dto.ImmutableGraphqlIntrospectionObject;
import org.immutables.value.Value;

import java.util.Map;

/**
 * Represents a data object as generated by the graphql introspection system.
 */
@Value.Immutable
@Value.Style(jdkOnly = true) //Needed to allow nulls in the collection
public interface GraphqlIntrospectionObject extends Serializable {
  @AllowNulls
  Map<String, Serializable> getContents();

  static GraphqlIntrospectionObject graphqlIntrospectionObject(Map<String, Serializable> contents) {
    return ImmutableGraphqlIntrospectionObject.builder()
      .contents(contents)
      .build();
  }
}
