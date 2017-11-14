package nl.knaw.huygens.timbuctoo.v5.dataset.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.IllegalDataSetNameException;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutablePromotedDataSet.class)
@JsonDeserialize(as = ImmutablePromotedDataSet.class)
public interface PromotedDataSet {
  //DataSetId's must be Safe. Meaning that they can be used on the fileSystem, in queries, wherever.
  //We implement that by making sure that the the owner and dataSetId contain only a-z (lowercase for case
  // insensitive environments) and 0-9
  //we also allow for an underscore. But only one, so that we can join the parts using 2 underscores

  //finally, the id must start with a character because some environments (java variables, graphql, javascript, sql)
  //don't allow an identifier to start with a number
  String VALID_ID = "^[a-z](_?[a-z0-9]+)+$";

  String getDataSetId();

  String getOwnerId();

  /**
   * Returns the baseUri that is used to resolve relative uri's in uploaded rdf files that have no explicit baseUri set.
   */
  String getBaseUri();

  /**
   * Returns a uri that you can use to generate dataSet-local uri's
   */
  String getUriPrefix();

  String getCombinedId();

  @Value.Auxiliary
  boolean isPromoted();

  @Value.Auxiliary
  boolean isPrivate();
  
  Optional<String> role = Optional.empty();

  static Tuple<String, String> splitCombinedId(String combinedId) {
    String[] parts = combinedId.split("__", 2);
    return Tuple.tuple(parts[0], parts[1]);
  }

  static String createCombinedId(String ownerId, String dataSetId) {
    return ownerId + "__" + dataSetId;
  }

  static PromotedDataSet promotedDataSet(String ownerId, String dataSetId, String baseUri, String uriPrefix,
                                         boolean promoted, boolean isPrivate) throws IllegalDataSetNameException {
    if (!ownerId.matches(VALID_ID) || !dataSetId.matches(VALID_ID)) {
      throw new IllegalDataSetNameException("Owner id and dataSet id should match " + VALID_ID);
    }

    return ImmutablePromotedDataSet.builder()
      .combinedId(createCombinedId(ownerId,dataSetId))
      .ownerId(ownerId)
      .baseUri(baseUri)
      .uriPrefix(uriPrefix)
      .dataSetId(dataSetId)
      .isPromoted(promoted)
      .isPrivate(isPrivate)
      .build();
  }
}
