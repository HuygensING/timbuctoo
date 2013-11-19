package nl.knaw.huygens.timbuctoo.model;

import java.util.List;

/**
 * An interface to define if an object could contain variations.
 */
public interface Variable {

  List<Reference> getVariations();

  void setVariations(List<Reference> variations);

}
