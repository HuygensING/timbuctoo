package nl.knaw.huygens.repository.model;

public class RelationValue {

  public String source;
  public String target;

  @Override
  public String toString() {
    return String.format("%s-%s", source, target);
  }

}
