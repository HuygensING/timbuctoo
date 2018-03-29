package nl.knaw.huygens.timbuctoo.v5.graphql.mutations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;

import java.util.ArrayList;
import java.util.List;

public class PredicateMutation {

  @JsonProperty
  private final List<CursorQuad> replacements = new ArrayList<>();

  @JsonProperty
  private final List<CursorQuad> additions = new ArrayList<>();

  @JsonProperty
  private final List<CursorQuad> retractions = new ArrayList<>();

  public List<CursorQuad> getReplacements() {
    return replacements;
  }

  public List<CursorQuad> getAdditions() {
    return additions;
  }

  public List<CursorQuad> getRetractions() {
    return retractions;
  }

  public PredicateMutation withAddition(String subject, String predicate, String object, String valueType) {
    additions.add(CursorQuad.create(subject, predicate, Direction.OUT, object, valueType, null, ""));
    return this;
  }

  public PredicateMutation withRetraction(String subject, String predicate, String object, String valueType) {
    retractions.add(CursorQuad.create(subject, predicate, Direction.OUT, object, valueType, null, ""));
    return this;
  }

  public PredicateMutation withReplacement(String subject, String predicate, String object, String valueType) {
    replacements.add(CursorQuad.create(subject, predicate, Direction.OUT, object, valueType, null, ""));
    return this;
  }

}
