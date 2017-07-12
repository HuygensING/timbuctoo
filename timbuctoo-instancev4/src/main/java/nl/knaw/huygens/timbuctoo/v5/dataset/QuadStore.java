package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;

import java.util.stream.Stream;

public interface QuadStore {
  Stream<CursorQuad> getQuads(String subject, String predicate, String cursor);
}
