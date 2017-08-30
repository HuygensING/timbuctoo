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
  private Entity[] entities;
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

  public void generateAdditions(RdfPatchSerializer saver) {
    for (Entity entity : entities) {
      Map<String, String[]> additions = entity.getAdditions();
      additions.forEach((predicate, values) -> {

        for (String value : values) {
          try {
            saver.onQuad(entity.getSpecializationOf().toString(), predicate, value,
              RdfConstants.STRING, null, null);
          } catch (LogStorageFailedException e) {
            e.printStackTrace();
          }
        }

      });
    }
  }

  public void generateDeletions(RdfPatchSerializer saver) {
    for (Entity entity : entities) {
      Map<String, String[]> deletions = entity.getDeletions();

      deletions.forEach((predicate, valuesArray) -> {

        for (String value : valuesArray) {
          try {
            saver.delValue(entity.getSpecializationOf().toString(), predicate, value, RdfConstants.STRING, null);
          } catch (LogStorageFailedException e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  public void generateReplacements(RdfPatchSerializer saver) {
    this.toReplace.forEach(quad -> {
      try {
        saver.delQuad(quad.getSubject(), quad.getPredicate(), quad.getObject(), null, null, null);
      } catch (LogStorageFailedException e) {
        e.printStackTrace();
      }
    });

    for (Entity entity : entities) {
      Map<String, String[]> replacements = entity.getReplacements();
      replacements.forEach((predicate, valuesArray) -> {
        for (String value : valuesArray) {
          try {
            saver.onQuad(entity.getSpecializationOf().toString(), predicate, value,
              RdfConstants.STRING, null, null);
          } catch (LogStorageFailedException e) {
            e.printStackTrace();
          }
        }
      });
    }
  }

  public void generateRevisionInfo(RdfPatchSerializer saver) {
    for (Entity entity : entities) {
      Map<String, String[]> additions = entity.getAdditions();

      URI specialization = entity.getSpecializationOf();
      URI revision = entity.getWasRevisionOf().get("@id");

      try {
        saver.onQuad(specialization.toString(), RdfConstants.TIM_LATEST_REVISION_OF, revision.toString(),
          RdfConstants.STRING, null, null);
        saver.delQuad(revision.toString(), RdfConstants.TIM_LATEST_REVISION_OF, revision.toString(),
          RdfConstants.STRING, null, null);
      } catch (LogStorageFailedException e) {
        e.printStackTrace();
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

