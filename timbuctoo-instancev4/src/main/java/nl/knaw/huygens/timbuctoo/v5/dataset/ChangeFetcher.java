package nl.knaw.huygens.timbuctoo.v5.dataset;

import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.QuadGraphs;

import java.util.stream.Stream;

public interface ChangeFetcher {

  Stream<QuadGraphs> getPredicates(String subject, boolean getRetracted, boolean getUnchanged, boolean getAsserted);

  Stream<QuadGraphs> getPredicates(String subject, String predicate, Direction direction, boolean getRetracted,
                                   boolean getUnchanged, boolean getAsserted);
}
