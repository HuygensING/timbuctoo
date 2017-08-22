package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import java.util.Map;

public class GenerateRdfPatchFromJsonLdEntity {
  private Entity entity;

  public GenerateRdfPatchFromJsonLdEntity(Entity entity) {
    this.entity = entity;
  }


  private void generateAdditions() {
    /*
    Map<String, Object> additions = this.entity.getAdditions();

    additions.forEach((predicate,values)->{
      String addition ="";
      if (values instanceof String[]){
        String[] valuesArray = (String[]) values;
        for(String value : valuesArray){
          addition = "+ " + predicate + " " + value;
          System.out.println(addition);
        }
      } else {
          addition = "+ " + predicate + " " + values;
          System.out.println(addition);
      }
    });
    */
  }

  private void generateDeletions() {
    /*
    Map<String, Object> deletions = this.entity.getDeletions();

    deletions.forEach((predicate, values)->{
      String deletion = "";
      if (values instanceof String[]){
        String[] valuesArray = (String[]) values;
        for(String value : valuesArray){
          deletion = "+ " + predicate + " " + value;
          System.out.println(deletion);
        }
      } else {
        deletion = "+ " + predicate + " " + values;
        System.out.println(deletion);
      }
    });
    */
  }

  private void generateReplacements() {

  }
}
