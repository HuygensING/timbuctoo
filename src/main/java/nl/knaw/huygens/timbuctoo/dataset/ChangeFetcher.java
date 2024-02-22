package nl.knaw.huygens.timbuctoo.dataset;

import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.QuadGraphs;

import java.util.stream.Stream;

public interface ChangeFetcher {
  Stream<QuadGraphs> getPredicates(String subject, boolean getRetracted, boolean getUnchanged, boolean getAsserted);

  Stream<QuadGraphs> getPredicates(String subject, String predicate, Direction direction, boolean getRetracted,
                                   boolean getUnchanged, boolean getAsserted);
}
