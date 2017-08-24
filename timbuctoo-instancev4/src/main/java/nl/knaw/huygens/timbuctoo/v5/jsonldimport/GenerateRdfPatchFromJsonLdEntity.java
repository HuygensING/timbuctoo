package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nl.knaw.huygens.timbuctoo.v5.filestorage.exceptions.LogStorageFailedException;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfPatchSerializer;
import nl.knaw.huygens.timbuctoo.v5.rdfio.RdfSerializer;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;

import java.util.Map;

public class GenerateRdfPatchFromJsonLdEntity {
  private Entity entity;
  private String subjectUri;

  @JsonCreator
  public GenerateRdfPatchFromJsonLdEntity(@JsonProperty("entity") Entity entity, String subjectUri) {
    this.entity = entity;
    this.subjectUri = subjectUri;
  }

  public Entity getEntity() {
    return entity;
  }

  public void generateAdditions(RdfPatchSerializer saver) {
    Map<String, String[]> additions = this.entity.getAdditions();
    additions.forEach((predicate, values) -> {
      //String addition = "";

      for (String value : values) {
        //addition = "+ " + predicate + " " + value;
        try {
          saver.onQuad(subjectUri, predicate, value, RdfConstants.STRING,null,null);
        } catch (LogStorageFailedException e) {
          e.printStackTrace();
        }
      }

    });
  }

  public void generateDeletions() {
    Map<String, Object> deletions = this.entity.getDeletions();

    deletions.forEach((predicate, values) -> {
      String deletion = "";
      if (values instanceof String[]) {
        String[] valuesArray = (String[]) values;
        for (String value : valuesArray) {
          deletion = "- " + "<dataset here>" + predicate + " " + value;
          System.out.println(deletion);
        }
      } else {
        deletion = "- " + "<dataset here>" + predicate + " " + values;
        System.out.println(deletion);
      }
    });

  }

  public void generateReplacements() {
    Map<String, Object> replacements = this.entity.getReplacements();

    replacements.forEach((predicate, values) -> {
      String replacement = "";
      if (values instanceof String[]) {
        String[] valuesArray = (String[]) values;
        for (String value : valuesArray) {
          replacement = "- " + "<dataset here>" + predicate + " " + value;
          System.out.println(replacement);
          replacement = "+ " + "<dataset here>" + predicate + " " + value;
          System.out.println(replacement);
        }
      } else {
        replacement = "- " + "<dataset here>" + predicate + " " + values;
        System.out.println(replacement);
        replacement = "+ " + "<dataset here>" + predicate + " " + values;
        System.out.println(replacement);
      }
    });
  }

  public void sendQuads(RdfPatchSerializer saver) throws LogStorageFailedException {
    generateAdditions(saver);
  }
}

