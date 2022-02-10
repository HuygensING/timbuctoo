package nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto;

import nl.knaw.huygens.timbuctoo.v5.util.Graph;

import java.util.Optional;

public interface SubjectGraphReference extends SubjectReference {
  Optional<Graph> getGraph();
}
