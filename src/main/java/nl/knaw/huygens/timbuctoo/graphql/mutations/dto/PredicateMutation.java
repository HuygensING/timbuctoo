package nl.knaw.huygens.timbuctoo.graphql.mutations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.util.RdfConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.datastores.quadstore.dto.CursorQuad.create;

public class PredicateMutation {

  @JsonProperty
  private final List<CursorQuad> additions = new ArrayList<>();

  @JsonProperty
  private final List<CursorQuad> retractions = new ArrayList<>();

  @JsonProperty
  private final List<CursorQuad> fullRetractions = new ArrayList<>();

  @JsonProperty
  private Map<UUID, SubjectFinder> subjectFinders = new HashMap<>();

  public List<CursorQuad> getFullRetractions() {
    return fullRetractions;
  }

  public Map<UUID, SubjectFinder> getSubjectFinders() {
    return subjectFinders;
  }

  public List<CursorQuad> getAdditions() {
    return additions;
  }

  public List<CursorQuad> getRetractions() {
    return retractions;
  }

  public PredicateMutation entity(String subjectUri, MutationOperation... operations) {
    entity(this, subjectUri, operations);
    return this;
  }

  public PredicateMutation entity(String subjectUri, List<MutationOperation> operations) {
    entity(this, subjectUri, operations);
    return this;
  }

  private void entity(PredicateMutation start, String subjectUri, MutationOperation... operations) {
    for (MutationOperation operation : operations) {
      if (operation != null) {
        operation.apply(subjectUri, start);
      }
    }
  }

  private void entity(PredicateMutation start, String subjectUri, List<MutationOperation> operations) {
    for (MutationOperation operation : operations) {
      if (operation != null) {
        operation.apply(subjectUri, start);
      }
    }
  }

  public static MutationOperation replace(String predicateUri, Value... values) {
    return (subject, mutation) -> {
      mutation.fullRetractions.add(create(subject, predicateUri, Direction.OUT, "", null, null, null, ""));
      for (Value value : values) {
        if (value.value != null) {
          mutation.additions.add(create(
            subject,
            predicateUri,
            Direction.OUT,
            value.value,
            value.type,
            value.language,
            value.graph,
            ""
          ));
        }
      }
    };
  }

  public static MutationOperation getOrCreate(String predicateUri, String defaultUri,
                                              List<MutationOperation> operations) {
    return (subject, mutation) -> {
      UUID id = UUID.randomUUID();
      mutation.subjectFinders.put(id, new FollowPredicateFinder(subject, predicateUri, defaultUri));
      mutation.additions.add(CursorQuad.create(subject, predicateUri, Direction.OUT, id.toString(),
          null, null, null, ""));
      for (MutationOperation operation : operations) {
        if (operation != null) {
          operation.apply(id.toString(), mutation);
        }
      }
    };
  }

  public static Value subject(String uri) {
    return new Value(uri, null, null, null);
  }

  public static Value value(String value) {
    return new Value(value, RdfConstants.STRING, null, null);
  }

  public static Value value(String value, String type) {
    return new Value(value, type, null, null);
  }

  public static Value languageString(String value, String language) {
    return new Value(value, RdfConstants.LANGSTRING, language, null);
  }

  public interface MutationOperation {
    void apply(String subject, PredicateMutation mutation);
  }

  public static class Value {
    final String value;
    final String type;
    final String language;
    final String graph;

    public Value(String value, String type, String language, String graph) {
      this.value = value;
      this.type = type;
      this.language = language;
      this.graph = graph;
    }
  }

  public interface SubjectFinder {
    String getSubject(QuadStore quadStore);
  }

  public static class FollowPredicateFinder implements SubjectFinder {
    private final String startSubject;
    private final String predicate;
    private final String preferredUri;

    public FollowPredicateFinder(String startSubject, String predicate, String preferredUri) {
      this.startSubject = startSubject;
      this.predicate = predicate;
      this.preferredUri = preferredUri;
    }

    @Override
    public String getSubject(QuadStore quadStore) {
      String first = null;
      try (Stream<CursorQuad> quads = quadStore.getQuads(startSubject, predicate, Direction.OUT, "")) {
        for (CursorQuad cursorQuad : (Iterable<CursorQuad>) quads::iterator) {
          if (!cursorQuad.getValuetype().isPresent()) {
            if (first == null) {
              first = cursorQuad.getObject();
            }
            if (cursorQuad.getObject().equals(preferredUri)) {
              return preferredUri;
            }
          }
        }
      }
      if (first != null) {
        return first;
      } else {
        return preferredUri;
      }
    }
  }

}
