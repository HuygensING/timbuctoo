package nl.knaw.huygens.timbuctoo.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.util.Graph;

import java.util.Optional;

public interface SubjectGraphReference extends SubjectReference {
  Optional<Graph> getGraph();
}
