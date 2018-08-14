package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.util.UserUriCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.PatchRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.RDF_TYPE;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.STRING;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.TIM_VOCAB;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.dataSetObjectUri;
import static nl.knaw.huygens.timbuctoo.v5.util.RdfConstants.timPredicate;

public class GraphQlToRdfPatch implements PatchRdfCreator {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String TIM_LATEST_REVISION = RdfConstants.timPredicate("latestRevision");
  private static final String PROV_SPECIALIZATION_OF = "http://www.w3.org/ns/prov#specializationOf";
  private static final String PROV_GENERATED = "http://www.w3.org/ns/prov#generated";
  private static final String PROV_ACTIVITY = "http://www.w3.org/ns/prov#Activity";
  private static final String PROV_ASSOCIATED_WITH = "http://www.w3.org/ns/prov#associatedWith";
  private static final String PROV_AGENT = "http://www.w3.org/ns/prov#Agent";
  private static final String PROV_QUALIFIED_ASSOCIATION = "http://www.w3.org/ns/prov#qualifiedAssociation";
  private static final String PROV_ASSOCIATION = "http://www.w3.org/ns/prov#Association";
  private static final String PROV_AGENT_PRED = "http://www.w3.org/ns/prov#agent";
  private static final String PROV_HAD_PLAN = "http://www.w3.org/ns/prov#hadPlan";
  private static final String PROV_PLAN = "http://www.w3.org/ns/prov#Plan";
  private final User user;
  private final String subjectUri;
  private final UserUriCreator userUriCreator;
  private final ChangeLog changeLog;

  GraphQlToRdfPatch(User user, String subjectUri, UserUriCreator userUriCreator, Map entity)
    throws JsonProcessingException {
    this.user = user;
    this.subjectUri = subjectUri;
    this.userUriCreator = userUriCreator;
    TreeNode jsonNode = OBJECT_MAPPER.valueToTree(entity);
    changeLog = OBJECT_MAPPER.treeToValue(jsonNode, ChangeLog.class);
  }

  @Override
  public void sendQuads(RdfPatchSerializer saver, Consumer<String> importStatusConsumer, DataSet dataSet)
    throws LogStorageFailedException {
    updateData(saver, dataSet);
    addRevision(saver, dataSet);
    addProvenance(saver, dataSet);
  }

  private void addProvenance(RdfPatchSerializer saver, DataSet dataSet) throws LogStorageFailedException {
    int rev = dataSet.getVersionStore().getVersion();

    String newRevision = subjectUri + "/" + (rev + 1);
    String activity = dataSetObjectUri(dataSet, "activity");
    saver.addDelQuad(true, activity, PROV_GENERATED, newRevision, null, null, null);
    saver.addDelQuad(true, activity, RDF_TYPE, PROV_ACTIVITY, null, null, null);

    String agent = userUriCreator.create(user);
    saver.addDelQuad(true, activity, PROV_ASSOCIATED_WITH, agent, null, null, null);
    saver.addDelQuad(true, agent, RDF_TYPE, PROV_AGENT, null, null, null);

    String association = dataSetObjectUri(dataSet, "association");
    saver.addDelQuad(true, activity, PROV_QUALIFIED_ASSOCIATION, association, null, null, null);
    saver.addDelQuad(true, association, RDF_TYPE, PROV_ASSOCIATION, null, null, null);
    saver.addDelQuad(true, association, PROV_AGENT_PRED, agent, null, null, null);

    String plan = dataSetObjectUri(dataSet, "plan");
    saver.addDelQuad(true, association, PROV_HAD_PLAN, plan, null, null, null);
    saver.addDelQuad(true, plan, RDF_TYPE, PROV_PLAN, null, null, null);

    addAction(saver, dataSet, plan, changeLog.getAdditions(), "additions", "Additions", "addition", "hasAddition",
      "Addition");

    addAction(saver, dataSet, plan, changeLog.getDeletions(), "deletions", "Deletions", "deletion", "hasDeletion",
      "Deletion");

    addAction(saver, dataSet, plan, changeLog.getReplacements(), "replacements", "Replacements", "replacement",
      "hasReplacement", "Replacement");
  }

  private void addAction(RdfPatchSerializer saver, DataSet dataSet, String plan,
                         Map<String, ? extends JsonNode> actions, String actionPredPlural, String actionTypePlural,
                         String actionPred, String hasAddition, String actionType) throws LogStorageFailedException {
    if (!actions.isEmpty()) {
      String additions = dataSetObjectUri(dataSet, actionPredPlural);
      saver.addDelQuad(true, plan, timPredicate(actionPredPlural), additions, null, null, null);
      saver.addDelQuad(true, additions, RDF_TYPE, TIM_VOCAB + actionTypePlural, null, null, null);

      for (Map.Entry<String, ? extends JsonNode> action : actions.entrySet()) {
        String additionUri = dataSetObjectUri(dataSet, actionPred);
        saver.addDelQuad(true, additions, timPredicate(hasAddition), additionUri, null, null, null);
        saver.addDelQuad(true, additionUri, RDF_TYPE, TIM_VOCAB + actionType, null, null, null);
        String predicate = dataSet.getTypeNameStore().makeUri(action.getKey());
        saver.addDelQuad(true, additionUri, timPredicate("hasKey"), predicate, null, null, null);

        processPlanValues(saver, dataSet, additionUri, action.getValue());

      }
    }
  }

  private void processPlanValues(RdfPatchSerializer saver, DataSet dataSet,
                                 String actionUri, JsonNode value) throws LogStorageFailedException {
    String prefValueUri = null;
    if (value.isArray()) {
      for (JsonNode propertyInput : value) {
        String uri = dataSetObjectUri(dataSet, "value");
        saver.addDelQuad(true, actionUri, timPredicate("hasValue"), uri, null, null, null);
        saver.addDelQuad(true, uri, timPredicate("type"), propertyInput.get("type").asText(), STRING, null, null);
        saver.addDelQuad(true, uri, timPredicate("rawValue"), propertyInput.get("value").asText(), STRING, null, null);
        if (prefValueUri != null) {
          saver.addDelQuad(true, prefValueUri, timPredicate("nextValue"), uri, null, null, null);
        }
        prefValueUri = uri;
      }
    } else {
      String valueUri = dataSetObjectUri(dataSet, "value");
      saver.addDelQuad(true, actionUri, timPredicate("hasValue"), valueUri, null, null, null);
      saver.addDelQuad(true, valueUri, timPredicate("type"), value.get("type").asText(), STRING, null, null);
      saver.addDelQuad(true, valueUri, timPredicate("rawValue"), value.get("value").asText(), STRING, null, null);
    }
  }

  private void addRevision(RdfPatchSerializer saver, DataSet dataSet) throws LogStorageFailedException {
    removePrevious(saver, dataSet, TIM_LATEST_REVISION);
    int rev = dataSet.getVersionStore().getVersion();

    String newRevision = subjectUri + "/" + (rev + 1);
    String prevRevision = subjectUri + "/" + rev;
    saver.addDelQuad(true, subjectUri, TIM_LATEST_REVISION, newRevision, null, null, null);
    saver.addDelQuad(true, newRevision, PROV_SPECIALIZATION_OF, subjectUri, null, null, null);
    saver.addDelQuad(false, subjectUri, TIM_LATEST_REVISION, prevRevision, null, null, null);
    saver.addDelQuad(true, newRevision, timPredicate("version"), "2", RdfConstants.INTEGER, null, null);
  }

  private void updateData(RdfPatchSerializer saver, DataSet dataSet) throws LogStorageFailedException {
    for (Map.Entry<String, ArrayNode> addition : changeLog.getAdditions().entrySet()) {
      JsonNode value = addition.getValue();
      String predicate = dataSet.getTypeNameStore().makeUri(addition.getKey());
      // Process the user input
      processValues(saver, value, predicate, true);
    }

    for (Map.Entry<String, ArrayNode> deletion : changeLog.getDeletions().entrySet()) {
      JsonNode value = deletion.getValue();
      String predicate = dataSet.getTypeNameStore().makeUri(deletion.getKey());
      // Process the user input
      processValues(saver, value, predicate, false);
    }

    for (Map.Entry<String, JsonNode> replacement : changeLog.getReplacements().entrySet()) {
      JsonNode value = replacement.getValue();
      String predicate = dataSet.getTypeNameStore().makeUri(replacement.getKey());

      // Process the user input
      processValues(saver, value, predicate, true);

      removePrevious(saver, dataSet, predicate);
    }
  }

  private void removePrevious(RdfPatchSerializer saver, DataSet dataSet, String predicate)
    throws LogStorageFailedException {
    try (Stream<CursorQuad> quads = dataSet.getQuadStore().getQuads(subjectUri, predicate, Direction.OUT, "")) {
      for (Iterator<CursorQuad> iterator = quads.iterator(); iterator.hasNext(); ) {
        CursorQuad quad = iterator.next();
        saver.addDelQuad(
          false,
          quad.getSubject(),
          quad.getPredicate(),
          quad.getObject(),
          quad.getValuetype().orElse(null),
          quad.getLanguage().orElse(null),
          null
        );
      }
    }
  }

  private void processValues(RdfPatchSerializer saver, JsonNode value, String predicate, boolean isAddition)
    throws LogStorageFailedException {
    if (value.isArray()) {
      for (JsonNode propertyInput : value) {
        saver.addDelQuad(
          isAddition,
          subjectUri,
          predicate,
          propertyInput.get("value").asText(),
          propertyInput.get("type").asText(),
          null,
          null
        );
      }
    } else {
      saver
        .addDelQuad(true, subjectUri, predicate, value.get("value").asText(), value.get("type").asText(), null, null);
    }
  }

  private static class ChangeLog {
    private LinkedHashMap<String, ArrayNode> additions;
    private LinkedHashMap<String, ArrayNode> deletions;
    private LinkedHashMap<String, JsonNode> replacements;

    @JsonCreator
    public ChangeLog(
      @JsonProperty("additions") LinkedHashMap<String, ArrayNode> additions,
      @JsonProperty("deletions") LinkedHashMap<String, ArrayNode> deletions,
      @JsonProperty("replacements") LinkedHashMap<String, JsonNode> replacements) {
      this.additions = additions == null ? Maps.newLinkedHashMap() : additions;
      this.deletions = deletions == null ? Maps.newLinkedHashMap() : deletions;
      this.replacements = replacements == null ? Maps.newLinkedHashMap() : replacements;
    }


    public LinkedHashMap<String, ArrayNode> getAdditions() {
      return additions;
    }

    public LinkedHashMap<String, ArrayNode> getDeletions() {
      return deletions;
    }

    public LinkedHashMap<String, JsonNode> getReplacements() {
      return replacements;
    }
  }
}
