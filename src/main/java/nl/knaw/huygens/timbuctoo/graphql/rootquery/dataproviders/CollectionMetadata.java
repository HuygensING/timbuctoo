package nl.knaw.huygens.timbuctoo.graphql.rootquery.dataproviders;

import nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto.SubjectReference;
import org.immutables.value.Value;

@Value.Immutable
public interface CollectionMetadata extends SubjectReference {
  String getShortenedUri();

  String getCollectionId();

  String getCollectionListId();

  String getItemType();

  PropertyList getProperties();

  default PropertyList getProperties(int count, String cursor) {
    return getProperties();
  }

  long getTotal();
}
