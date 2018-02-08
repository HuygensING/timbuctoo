package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.jsonldjava.core.DocumentLoader;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;
import com.google.common.collect.ImmutableMap;
import nl.knaw.huygens.timbuctoo.v5.dataset.PatchRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.io.IOException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsn;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnA;
import static nl.knaw.huygens.timbuctoo.util.JsonBuilder.jsnO;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_EDITOR;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_LATEST_REVISION;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_SKOLEMIZE;

public class JsonProvenanceToRdfPatch implements PatchRdfCreator {
  static final String PROV_GENERATES = "http://www.w3.org/ns/prov#generates";
  static final String PROV_SPECIALIZATION_OF = "http://www.w3.org/ns/prov#specializationOf";
  static final String TIM_MUTATION = "http://timbuctoo.huygens.knaw.nl/v5/vocabulary#mutation";
  static final String TIM_DELETIONS = "http://timbuctoo.huygens.knaw.nl/v5/vocabulary#deletions";
  static final String TIM_ADDITIONS = "http://timbuctoo.huygens.knaw.nl/v5/vocabulary#additions";
  static final String TIM_PREDICATE = "http://timbuctoo.huygens.knaw.nl/v5/vocabulary#predicate";
  static final String TIM_VALUE = "http://timbuctoo.huygens.knaw.nl/v5/vocabulary#value";
  static final String TIM_REPLACEMENTS = "http://timbuctoo.huygens.knaw.nl/v5/vocabulary#replacements";
  static final String PROV_REVISION_OF = "http://www.w3.org/ns/prov#wasRevisionOf";
  static final String PROV_ASSOCIATION = "http://www.w3.org/ns/prov#qualifiedAssociation";
  private static final String PROV_STARTED_AT = "http://www.w3.org/ns/prov#startedAtTime";
  private static final String PROV_ENDED_AT = "http://www.w3.org/ns/prov#endedAtTime";
  private static final String XSD_DATE_TIME_STAMP = "http://www.w3.org/TR/xmlschema11-2/#dateTimeStamp";
  private final JsonNode activity;
  private final Map<String, List<CursorQuad>> toReplace;
  private final String changeTime;
  private static final Map<String, String> FRAME = ImmutableMap.of(
    "@type", "http://www.w3.org/ns/prov#Activity"
  );
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  JsonProvenanceToRdfPatch(JsonNode activity, Map<String, List<CursorQuad>> toReplace, String changeTime) {
    this.activity = activity;
    this.toReplace = toReplace;
    this.changeTime = changeTime;
  }

  @JsonCreator
  public static JsonProvenanceToRdfPatch fromJson(@JsonProperty("activity") JsonNode activity,
                                                  @JsonProperty("toReplace") Map<String, List<CursorQuad>> toReplace,
                                                  @JsonProperty("changeTime") String changeTime) {
    return new JsonProvenanceToRdfPatch(activity, toReplace, changeTime);
  }

  public static JsonProvenanceToRdfPatch fromCurrentState(DocumentLoader documentLoader, String jsonLd,
                                                          QuadStore quadStore, String editorUri, String randomness,
                                                          Clock clock)
    throws IOException, ConcurrentUpdateException {

    JsonLdOptions opts = new JsonLdOptions();
    opts.setDocumentLoader(documentLoader);
    //used also for side-effect of checking the json-ld for wellformedness
    //turns the json object into an array of objects that have type prov:activity
    //The json is normalized:
    // - all properties are arrays
    // - all values are wrapped in an object
    // - all non-value objects have an @id property that contains a string (with either a blank node id or a URI)
    Map<String, String> frame = new HashMap<>();
    frame.put("@type", "http://www.w3.org/ns/prov#Activity");
    try {
      final JsonNode activities = OBJECT_MAPPER.valueToTree(
        JsonLdProcessor.expand(
          JsonLdProcessor.frame(
            JsonUtils.fromString(jsonLd),
            frame,
            opts
          )
        )
      );


      final List<String> errors = integrityCheck(activities);
      if (errors.size() > 0) {
        throw new IOException(String.join("\n", errors));
      }

      final ObjectNode activity = (ObjectNode) activities.get(0);
      if (!skolemize(activity, randomness, "")) {
        throw new IOException("Not all nodes were blank nodes");
      }

      if (!checkTypes(activity, "")) {
        throw new IOException("Not all nodes had a type");
      }

      checkIfUpdatedInTheMeanTime(quadStore, activity);

      addEditor(editorUri, activity, randomness);

      return new JsonProvenanceToRdfPatch(
        activity,
        collectReplacements(activity, quadStore),
        clock.instant().toString()
      );
    } catch (JsonLdError e) {
      throw new IOException(e);
    }
  }

  public static void checkIfUpdatedInTheMeanTime(QuadStore quadStore, ObjectNode activity)
    throws ConcurrentUpdateException {
    for (JsonNode revision : activity.get(PROV_GENERATES)) {
      if (!isContinuationOfMostRecentRevision(revision, quadStore)) {
        throw new ConcurrentUpdateException(
          "This revision does not point to the most recent revision of the entity"
        );
      }
    }
  }

  public static void addEditor(String editorUri, ObjectNode activity, String randomness) {
    if (!activity.has(PROV_ASSOCIATION)) {
      activity.set(PROV_ASSOCIATION, jsnA());
    }
    ((ArrayNode) activity.get(PROV_ASSOCIATION)).add(jsnO(
      "@id", jsn(TIM_SKOLEMIZE + randomness + "/editor"),
      "@type", jsnA(jsn("http://www.w3.org/ns/prov#Association")),
      "http://www.w3.org/ns/prov#agent", jsnA(jsnO("@id", jsn(editorUri))),
      "http://www.w3.org/ns/prov#hadRole", jsnA(jsnO("@id", jsn(TIM_EDITOR)))
    ));
  }

  public static Map<String, List<CursorQuad>> collectReplacements(JsonNode activity, QuadStore quadStore) {
    Map<String, List<CursorQuad>> toReplace = new HashMap<>();
    for (JsonNode revision : activity.get(PROV_GENERATES)) {
      final JsonNode replacements = revision.get(TIM_REPLACEMENTS);
      if (replacements != null) {
        String entity = revision.get(PROV_SPECIALIZATION_OF).get(0).get("@id").asText();
        List<CursorQuad> quads = new ArrayList<>();
        toReplace.put(revision.get("@id").asText(), quads);
        for (JsonNode replacement : replacements) {
          if (replacement.has(TIM_PREDICATE)) {
            final String predicate = replacement.get(TIM_PREDICATE).get(0).get("@value").asText();
            try (Stream<CursorQuad> source = quadStore.getQuads(entity, predicate, Direction.OUT, "")) {
              source.forEach(quads::add);
            }
          }
        }
      }
    }
    return toReplace;
  }

  private static boolean checkTypes(ObjectNode tree, String path) {
    boolean isEntity = tree.has("@id");
    final String prov_generates = "\nhttp://www.w3.org/ns/prov#generates";
    if (isEntity &&
      !path.equals(prov_generates + "\nhttp://www.w3.org/ns/prov#specializationOf") &&
      !path.equals(prov_generates + "\nhttp://timbuctoo.huygens.knaw.nl/v5/vocabulary#additions") &&
      !path.equals(prov_generates + "\nhttp://timbuctoo.huygens.knaw.nl/v5/vocabulary#deletions") &&
      !path.equals(prov_generates + "\nhttp://timbuctoo.huygens.knaw.nl/v5/vocabulary#replacements") &&
      !path.equals(prov_generates + "\nhttp://www.w3.org/ns/prov#wasRevisionOf")) {
      boolean hasType = tree.has("@type") && !tree.get("@type").asText().startsWith("_:");
      for (Iterator<Map.Entry<String, JsonNode>> it = tree.fields(); it.hasNext(); ) {
        Map.Entry<String, JsonNode> values = it.next();
        for (JsonNode value : values.getValue()) {
          if (value.isObject()) {
            hasType = hasType && checkTypes((ObjectNode) value, path + "\n" + values.getKey());
          }
        }
      }
      return hasType;
    }
    return true;
  }

  private static boolean skolemize(ObjectNode tree, String randomness, String path) {
    if (tree.has("@id")) {
      boolean isBlankSubject = tree.get("@id").asText().startsWith("_:");
      if (isBlankSubject) {
        tree.set("@id", jsn(TIM_SKOLEMIZE + randomness + "/" + tree.get("@id").asText().substring(2)));
      }
      boolean isSuccess = isBlankSubject ||
        path.equals("\nhttp://www.w3.org/ns/prov#generates\n" + PROV_SPECIALIZATION_OF) ||
        path.equals("\nhttp://www.w3.org/ns/prov#generates\n" + TIM_ADDITIONS + "\n" + TIM_VALUE) ||
        path.equals("\nhttp://www.w3.org/ns/prov#generates\n" + TIM_REPLACEMENTS + "\n" + TIM_VALUE) ||
        path.equals("\nhttp://www.w3.org/ns/prov#generates\n" + TIM_DELETIONS + "\n" + TIM_VALUE) ||
        path.equals("\nhttp://www.w3.org/ns/prov#generates\n" + PROV_REVISION_OF);

      for (Iterator<Map.Entry<String, JsonNode>> it = tree.fields(); it.hasNext(); ) {
        Map.Entry<String, JsonNode> values = it.next();
        for (JsonNode value : values.getValue()) {
          if (value.isObject()) {
            isSuccess = isSuccess && skolemize((ObjectNode) value, randomness, path + "\n" + values.getKey());
          }
        }
      }
      return isSuccess;
    } else {
      return true;
    }
  }

  @Override
  public void sendQuads(RdfPatchSerializer saver, Consumer<String> importStatus) throws LogStorageFailedException {
    for (JsonNode revision : activity.get(PROV_GENERATES)) {
      final String entityUri = revision.get(PROV_SPECIALIZATION_OF).get(0).get("@id").asText();

      final String revisionUri = revision.get("@id").asText();
      String wasRevisionOf = null;
      if (revision.get(PROV_REVISION_OF) != null) {
        wasRevisionOf = revision.get(PROV_REVISION_OF).get(0).get("@id").asText();
      }
      generateRevisionInfo(saver, revisionUri, entityUri, wasRevisionOf);
      generatePatch(saver, revision.get(TIM_ADDITIONS), entityUri, true);
      for (CursorQuad quad : toReplace.getOrDefault(revisionUri, new ArrayList<>())) {
        saver.delQuad(
          quad.getSubject(),
          quad.getPredicate(),
          quad.getObject(),
          quad.getValuetype().orElse(null),
          quad.getLanguage().orElse(null), null
        );
      }
      generatePatch(saver, revision.get(TIM_REPLACEMENTS), entityUri, true);
      generatePatch(saver, revision.get(TIM_DELETIONS), entityUri, false);

    }
    try {
      final HashMap map = OBJECT_MAPPER.treeToValue(activity, HashMap.class);
      final RDFDataset dataset = (RDFDataset) JsonLdProcessor.toRDF(map);
      for (String graphName : dataset.graphNames()) {
        for (RDFDataset.Quad quad : dataset.getQuads(graphName)) {
          saver.onQuad(
            quad.getSubject().getValue(),
            quad.getPredicate().getValue(),
            quad.getObject().getValue(),
            quad.getObject().isLiteral() ? quad.getObject().getDatatype() : null,
            quad.getObject().getLanguage(),
            quad.getGraph() == null ? null : quad.getGraph().getValue()
          );
        }
      }
    } catch (JsonProcessingException | JsonLdError e) {
      throw new LogStorageFailedException(e);
    }
  }

  public JsonNode getActivity() {
    return activity;
  }

  @JsonProperty
  public Map<String, List<CursorQuad>> getToReplace() {
    return toReplace;
  }

  private void generatePatch(RdfPatchSerializer saver, JsonNode action, String subject, boolean isAddition)
    throws LogStorageFailedException {

    if (action == null) {
      return;
    }

    for (JsonNode item : action) {
      String predicate = item.get(TIM_PREDICATE).get(0).get("@value").asText();
      for (JsonNode value : item.get(TIM_VALUE)) {
        String valueStr;
        String typeStr;
        String languageStr;
        if (predicate.equals("@type")) {
          predicate = RdfConstants.RDF_TYPE;
          valueStr = value.asText();
          typeStr = null;
          languageStr = null;
        } else if (value.has("@language")) {
          valueStr = value.asText();
          typeStr = RdfConstants.LANGSTRING;
          languageStr = value.get("@language").asText();
        } else if (value.has("@id")) {
          valueStr = value.get("@id").asText();
          typeStr = null;
          languageStr = null;
        } else if (value.has("@type")) {
          valueStr = value.get("@value").asText();
          typeStr = value.get("@type").asText();
          languageStr = null;
        } else {
          valueStr = value.get("@value").asText();
          typeStr = getType(value.get("@value"));
          languageStr = null;
        }
        saver.addDelQuad(
          isAddition,
          subject,
          predicate,
          valueStr,
          typeStr,
          languageStr,
          null
        );
      }
    }
  }

  private String getType(JsonNode object) {
    if (object.isIntegralNumber()) {
      return "http://www.w3.org/2001/XMLSchema#long";
    } else if (object.isFloatingPointNumber()) {
      return "http://www.w3.org/2001/XMLSchema#double";
    } else if (object.isBoolean()) {
      return "http://www.w3.org/2001/XMLSchema#boolean";
    } else if (object.isTextual()) {
      return "http://www.w3.org/2001/XMLSchema#string";
    } else {
      return null;//never happens
    }
  }

  private void generateRevisionInfo(RdfPatchSerializer saver, String revisionUri, String entityUri,
                                    String wasRevisionOf) throws LogStorageFailedException {
    saver.onQuad(entityUri, RdfConstants.TIM_LATEST_REVISION, revisionUri, null, null, null);
    if (wasRevisionOf != null) {
      saver.delQuad(entityUri, RdfConstants.TIM_LATEST_REVISION, wasRevisionOf, null, null, null);
      saver.onQuad(wasRevisionOf, PROV_ENDED_AT, changeTime, XSD_DATE_TIME_STAMP, null, null);
    }
    saver.onQuad(revisionUri, PROV_STARTED_AT, changeTime, XSD_DATE_TIME_STAMP, null, null);
  }

  private static List<String> integrityCheck(JsonNode activity) throws IOException {
    List<String> errors = new ArrayList<>();

    if (activity.size() != 1) {
      errors.add("There should be one prov:Activity per edit action");
    }
    activity = activity.get(0);

    if (hasEditorRole(activity.get("http://www.w3.org/ns/prov#qualifiedAssociation"))) {
      errors.add("The editor role is added by timbuctoo and should not be inserted manually");
    }

    for (JsonNode revision : activity.get(PROV_GENERATES)) {
      if (!revision.has("@id")) {
        errors.add("A revision may not be a value object");
      }
      if (!isSpecializationOfOneEntity(revision)) {
        errors.add("A revision must be the specialization of one entity");
      }
      if (!containsZeroOrOnePreviousRevisions(revision)) {
        errors.add("A specialization must refer to one previous revision, or none if the entity does not yet exist");
      }
      if (revision.has(TIM_ADDITIONS)) {
        isFlatNode(revision.get(TIM_ADDITIONS), errors);
      }
      if (revision.has(TIM_DELETIONS)) {
        isFlatNode(revision.get(TIM_DELETIONS), errors);
      }
      if (revision.has(TIM_REPLACEMENTS)) {
        isFlatNode(revision.get(TIM_REPLACEMENTS), errors);
      }
    }
    return errors;
    //FIXME: check if the updates match the schema (use graphql for that)
  }

  private static boolean hasEditorRole(JsonNode associations) {
    if (associations != null) {
      for (JsonNode association : associations) {
        JsonNode roles = association.get("http://www.w3.org/ns/prov#hadRole");
        if (roles != null) {
          for (JsonNode role : roles) {
            if (role.has("@id") && TIM_EDITOR.equals(role.get("@id").asText())) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private static boolean containsZeroOrOnePreviousRevisions(JsonNode revision) {
    return
      !revision.has(PROV_REVISION_OF) ||
      (
        revision.get(PROV_SPECIALIZATION_OF).size() == 1 &&
        revision.get(PROV_SPECIALIZATION_OF).get(0).has("@id") &&
        revision.get(PROV_SPECIALIZATION_OF).get(0).get("@id").isTextual()
      );
  }

  private static boolean isContinuationOfMostRecentRevision(JsonNode revision, QuadStore quadStore) {
    String entity = revision.get(PROV_SPECIALIZATION_OF).get(0).get("@id").asText();
    String prevRevision = null;
    if (revision.has(PROV_REVISION_OF)) {
      prevRevision = revision.get(PROV_REVISION_OF).get(0).get("@id").asText();
    }
    try (Stream<CursorQuad> quads = quadStore.getQuads(entity, TIM_LATEST_REVISION, Direction.OUT, "")) {
      String mostRecentRevision = quads.findFirst().map(CursorQuad::getObject).orElse(null);
      return Objects.equals(prevRevision, mostRecentRevision);
    }
  }

  private static boolean isSpecializationOfOneEntity(JsonNode revision) {
    return revision.has(PROV_SPECIALIZATION_OF) &&
      revision.get(PROV_SPECIALIZATION_OF).size() == 1 &&
      revision.get(PROV_SPECIALIZATION_OF).get(0).has("@id") &&
      revision.get(PROV_SPECIALIZATION_OF).get(0).get("@id").isTextual() &&
      !revision.get(PROV_SPECIALIZATION_OF).get(0).get("@id").asText().startsWith("_:");
  }

  private static void isFlatNode(JsonNode array, List<String> errors) {
    for (JsonNode object : array) {
      if (!object.has("@type") ||
        !(object.get("@type").size() == 1) ||
        !object.get("@type").get(0).asText().equals(TIM_MUTATION)) {
        errors.add("Additions/Replacements/Deletions must be of exactly the type " + TIM_MUTATION);
      }
      if (!object.has(TIM_PREDICATE) ||
        !object.get(TIM_PREDICATE).isArray() ||
        !(object.get(TIM_PREDICATE).size() == 1) ||
        !object.get(TIM_PREDICATE).get(0).has("@value") ||
        !object.get(TIM_PREDICATE).get(0).get("@value").isTextual()) {
        errors.add("Additions/Replacements/Deletions must contain a 'predicate' key containing the " +
          "predicate as string");
      } else {
        if (object.get(TIM_PREDICATE).get(0).get("@value").asText().equals(RDF_TYPE)) {
          for (JsonNode value : object.get(TIM_VALUE)) {
            if (!value.has("@id")) {
              errors.add(RDF_TYPE + " should point to a IRI ({@id: \"\"} instead of \"\"");
            }
          }
        }
      }
      if (!object.has(TIM_VALUE)) {
        errors.add("Additions/Replacements/Deletions must contain a 'value' key containing the value");
      } else {
        for (JsonNode value : object.get(TIM_VALUE)) {
          if (value.has("@id") && stream(value.fieldNames()).count() > 1) {
            errors.add("Additions/Replacements/Deletions may only point to other objects, not contain properties " +
              "on them");
          }
        }
      }
    }
  }
}

