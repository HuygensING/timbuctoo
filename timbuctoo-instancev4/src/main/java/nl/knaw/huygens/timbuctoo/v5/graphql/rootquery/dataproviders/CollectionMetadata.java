package nl.knaw.huygens.timbuctoo.v5.graphql.rootquery.dataproviders;

import org.immutables.value.Value;

@Value.Immutable
public interface CollectionMetadata {
  String getCollectionId();

  String getCollectionListId();

  String getUri();

  default String getTitle() {
    return "FIXME: list title";//FIXME
  }

  default String getArcheType() {
    return "FIXME: archetype";//FIXME
  }

  PropertyList getProperties();

  default PropertyList getProperties(int count, String cursor) {
    return getProperties();
  }

  long getTotal();
}
