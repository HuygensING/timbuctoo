package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.timbuctoo.v5.dataset.PatchRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;

import java.util.Optional;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.HAS_VIEW_CONFIG;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;

class GraphQlPatchRdfCreator implements PatchRdfCreator {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final String collectionUri;
  private final Object viewConfig;
  private final String baseUri;
  private final Optional<CursorQuad> oldValueOpt;

  public GraphQlPatchRdfCreator(QuadStore quadStore, String collectionUri, Object viewConfig, String baseUri) {
    this.collectionUri = collectionUri;
    this.viewConfig = viewConfig;
    this.baseUri = baseUri;
    try (Stream<CursorQuad> quads = quadStore.getQuads(collectionUri, HAS_VIEW_CONFIG, Direction.OUT, "")) {
      this.oldValueOpt = quads.findFirst();
    }
  }

  @Override
  public void sendQuads(RdfPatchSerializer saver) throws LogStorageFailedException {

    try {
      if (oldValueOpt.isPresent()) {
        CursorQuad oldValue = oldValueOpt.get();
        String valueType = oldValue.getValuetype().isPresent() ? oldValue.getValuetype().get() : null;
        saver.delValue(oldValue.getSubject(), oldValue.getPredicate(), oldValue.getObject(), valueType, baseUri);
      }
      saver.onValue(collectionUri, HAS_VIEW_CONFIG, OBJECT_MAPPER.writeValueAsString(viewConfig), STRING, baseUri);
    } catch (JsonProcessingException e) {
      throw new LogStorageFailedException(e);
    }
  }
}
