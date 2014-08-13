package nl.knaw.huygens.timbuctoo.model.util;

public interface Range {
  Object getUpperLimit();

  Object getLowerLimit();

  public abstract boolean isValidRange();
}
