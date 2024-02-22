package nl.knaw.huygens.timbuctoo.graphql.collectionfilter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import nl.knaw.huygens.timbuctoo.util.Tuple;

import java.io.IOException;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface CollectionFilter {
  FilterResult query(String dataSetId, String fieldName, String elasticSearchQuery, String token, int preferredPageSize)
    throws IOException;

  Tuple<Boolean, String> isHealthy();
}
