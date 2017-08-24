package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.bdbdatafetchers.dto.CursorQuad;
import nl.knaw.huygens.timbuctoo.v5.dataset.Direction;
import nl.knaw.huygens.timbuctoo.v5.dataset.QuadStore;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class GenerateRdfPatchFromJsonLdEntity {
  private Entity entity;
  private List<CursorQuad> toReplace = new ArrayList<>();


  @JsonCreator
  public GenerateRdfPatchFromJsonLdEntity(@JsonProperty("entity") Entity entity) {
    this.entity = entity;
  }

  public GenerateRdfPatchFromJsonLdEntity(Entity entity, QuadStore quadStore) {
    this.entity = entity;

    entity.getReplacements().forEach((predicate, value) -> {
      try (Stream<CursorQuad> quads = quadStore
        .getQuads(entity.getSpecializationOf().toString(), predicate, Direction.OUT, "")) {
        quads.forEach(q -> {
          this.toReplace.add(q);
        });
      }
    });

  }


  public Entity getEntity() {
    return entity;
  }

  public void generateAdditions(RdfPatchSerializer saver) {
    Map<String, String[]> additions = this.entity.getAdditions();
    additions.forEach((predicate, values) -> {

      for (String value : values) {
        try {
          saver.onQuad(entity.getSpecializationOf().toString(), predicate, value, RdfConstants.STRING, null, null);
        } catch (LogStorageFailedException e) {
          e.printStackTrace();
        }
      }

    });
  }

  public void generateDeletions(RdfPatchSerializer saver) {
    Map<String, String[]> deletions = this.entity.getDeletions();

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

  public void generateReplacements(RdfPatchSerializer saver) {
    Map<String, String[]> replacements = this.entity.getReplacements();

    this.toReplace.forEach(quad -> {
      try {
        saver.delQuad(quad.getSubject(), quad.getPredicate(), quad.getObject(), null, null, null);
      } catch (LogStorageFailedException e) {
        e.printStackTrace();
      }
    });

    replacements.forEach((predicate, valuesArray) -> {
      for (String value : valuesArray) {
        try {
          saver.onQuad(entity.getSpecializationOf().toString(), predicate, value, RdfConstants.STRING, null, null);
        } catch (LogStorageFailedException e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void sendQuads(RdfPatchSerializer saver) throws LogStorageFailedException {
    generateAdditions(saver);
  }
}

