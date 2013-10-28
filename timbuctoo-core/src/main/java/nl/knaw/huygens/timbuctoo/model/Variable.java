package nl.knaw.huygens.timbuctoo.model;

import java.util.List;

/**
 * An interface to define if an object could contain variations.
 * @author martijnm
 *
 */
public interface Variable {

  public abstract List<Reference> getVariations();

  public abstract void setVariations(List<Reference> variations);

  public abstract void addVariation(Class<? extends Entity> refType, String refId);

  public abstract String getCurrentVariation();

  public abstract void setCurrentVariation(String currentVariation);

}