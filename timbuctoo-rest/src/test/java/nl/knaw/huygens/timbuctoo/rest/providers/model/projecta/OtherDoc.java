package nl.knaw.huygens.timbuctoo.rest.providers.model.projecta;

import nl.knaw.huygens.timbuctoo.rest.providers.model.TestInheritsFromTestBaseDoc;

/**
 * Another extension of the basic test doc.
 */
public class OtherDoc extends TestInheritsFromTestBaseDoc {

  public String otherThing;

  public OtherDoc() {}

  public OtherDoc(String id) {
    setId(id);
  }

}
