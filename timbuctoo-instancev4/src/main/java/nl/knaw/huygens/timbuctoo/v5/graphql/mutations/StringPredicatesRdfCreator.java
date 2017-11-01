package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.util.Tuple;
import nl.knaw.huygens.timbuctoo.v5.dataset.PatchRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

class StringPredicatesRdfCreator implements PatchRdfCreator {
  @JsonProperty
  private final Map<Tuple<String, String>, Optional<String>> newValues;

  @JsonProperty
  private final List<CursorQuad> oldValues;

  @JsonProperty
  private final String baseUri;

  public StringPredicatesRdfCreator(QuadStore quadStore, Map<Tuple<String, String>, Optional<String>> newValues,
                                    String baseUri) {
    this.baseUri = baseUri;
    this.oldValues = new ArrayList<>();
    this.newValues = newValues;

    for (Tuple<String, String> subjectAndPredicate : this.newValues.keySet()) {
      final String subject = subjectAndPredicate.getLeft();
      final String predicate = subjectAndPredicate.getRight();
      try (Stream<CursorQuad> quads = quadStore.getQuads(subject, predicate, Direction.OUT, "")) {
        for (CursorQuad quad : (Iterable<CursorQuad>) quads::iterator) {
          this.oldValues.add(quad);
        }
      }
    }
  }

  @JsonCreator
  public StringPredicatesRdfCreator(@JsonProperty("newValues")
                                             Map<Tuple<String, String>, Optional<String>> newValues,
                                    @JsonProperty("oldValues") List<CursorQuad> oldValues,
                                    @JsonProperty("baseUri") String baseUri) {
    this.newValues = newValues;
    this.oldValues = oldValues;
    this.baseUri = baseUri;
  }

  @Override
  public void sendQuads(RdfPatchSerializer saver) throws LogStorageFailedException {
    for (CursorQuad oldValue : oldValues) {
      saver.delQuad(
        oldValue.getSubject(),
        oldValue.getPredicate(),
        oldValue.getObject(),
        oldValue.getValuetype().orElse(null),
        oldValue.getLanguage().orElse(null),
        baseUri
      );
    }
    for (Map.Entry<Tuple<String, String>, Optional<String>> newValue : newValues.entrySet()) {
      if (newValue.getValue().isPresent()) {
        saver.onQuad(
          newValue.getKey().getLeft(),
          newValue.getKey().getRight(),
          newValue.getValue().get(),
          RdfConstants.STRING,
          null,
          baseUri
        );
      }
    }
  }
}
