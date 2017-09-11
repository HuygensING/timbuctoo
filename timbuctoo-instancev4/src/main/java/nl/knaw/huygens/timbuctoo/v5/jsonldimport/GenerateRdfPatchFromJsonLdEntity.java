package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.PatchRdfCreator;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GenerateRdfPatchFromJsonLdEntity implements PatchRdfCreator {
  private final Entity[] entities;
  private List<CursorQuad> toReplace = new ArrayList<>();


  @JsonCreator
  public GenerateRdfPatchFromJsonLdEntity(@JsonProperty("entities") Entity[] entities) {
    this.entities = entities;
  }

  public GenerateRdfPatchFromJsonLdEntity(Entity[] entities, QuadStore quadStore) {
    this.entities = entities;

    for (Entity entity : entities) {
      entity.getReplacements().forEach((predicate, value) -> {
        try (Stream<CursorQuad> quads = quadStore
          .getQuads(entity.getSpecializationOf().toString(), predicate, Direction.OUT, "")) {
          quads.forEach(q -> {
            this.toReplace.add(q);
          });
        }
      });
    }
  }

  public Entity[] getEntities() {
    return entities;
  }

  public void generateAdditions(RdfPatchSerializer saver) throws LogStorageFailedException {
    for (Entity entity : entities) {
      Map<String, String[]> additions = entity.getAdditions();

      for (Map.Entry<String, String[]> entry : additions.entrySet()) {
        for (String value : entry.getValue()) {
          saver.onQuad(entity.getSpecializationOf().toString(), RdfConstants.TIM_PROP_ID + "/" + entry.getKey(),
            "\"" + value + "\"",
            RdfConstants.STRING, null, null);
        }
      }

    }
  }

  public void generateDeletions(RdfPatchSerializer saver) throws LogStorageFailedException {
    for (Entity entity : entities) {
      Map<String, String[]> deletions = entity.getDeletions();

      for (Map.Entry<String, String[]> entry : deletions.entrySet()) {
        for (String value : entry.getValue()) {
          saver.delValue(entity.getSpecializationOf().toString(), RdfConstants.TIM_PROP_ID + "/" + entry.getKey(),
            "\"" + value + "\"", RdfConstants.STRING, null);
        }
      }
    }
  }

  public void generateReplacements(RdfPatchSerializer saver) throws LogStorageFailedException {
    for (CursorQuad quad : toReplace) {
      saver.delQuad(quad.getSubject(), quad.getPredicate(), quad.getObject(), null, null, null);
    }

    for (Entity entity : entities) {
      Map<String, String[]> replacements = entity.getReplacements();

      for (Map.Entry<String, String[]> entry : replacements.entrySet()) {
        for (String value : entry.getValue()) {
          saver.onQuad(entity.getSpecializationOf().toString(), RdfConstants.TIM_PROP_ID + "/" + entry.getKey(),
            "\"" + value + "\"",
            RdfConstants.STRING, null, null);
        }
      }
    }

  }

  public void generateRevisionInfo(RdfPatchSerializer saver) throws LogStorageFailedException {
    for (Entity entity : entities) {
      URI specialization = entity.getSpecializationOf();
      URI revision = entity.getWasRevisionOf().get("@id");

      saver
        .onQuad(specialization.toString(), RdfConstants.TIM_SPECIALIZATION_OF, "<" + specialization.toString() + ">",
          RdfConstants.STRING, null, null);

      if (revision != null) {
        saver.onQuad(specialization.toString(), RdfConstants.TIM_LATEST_REVISION_OF, "<" + revision.toString() + ">",
          RdfConstants.STRING, null, null);
        saver.delQuad(revision.toString(), RdfConstants.TIM_LATEST_REVISION_OF, revision.toString(),
          RdfConstants.STRING, null, null);
      } else {
        saver
          .onQuad(specialization.toString(), RdfConstants.TIM_LATEST_REVISION_OF, "<" + specialization.toString() + ">",
            RdfConstants.STRING, null, null);
      }
    }
  }

  public void sendQuads(RdfPatchSerializer saver) throws LogStorageFailedException {
    generateAdditions(saver);
    generateDeletions(saver);
    generateReplacements(saver);
    generateRevisionInfo(saver);
  }

}

