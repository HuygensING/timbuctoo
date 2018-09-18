package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PredicateBuilder {
  private String name;
  private boolean isList;
  private Direction direction;
  private boolean hasBeenSingular;
  private boolean inUse = true;
  private boolean hasBeenList;
  private boolean explicit;

  private PredicateBuilder() {

  }

  public static PredicateBuilder predicate() {
    return new PredicateBuilder();
  }

  public PredicateBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public PredicateBuilder isList() {
    this.isList = true;
    return this;
  }

  public PredicateBuilder hasDirection(Direction direction) {
    this.direction = direction;
    return this;
  }


  public Predicate build() {
    Predicate predicate = mock(Predicate.class);
    when(predicate.getName()).thenReturn(name);
    when(predicate.isList()).thenReturn(isList);
    when(predicate.isHasBeenSingular()).thenReturn(hasBeenSingular);
    when(predicate.inUse()).thenReturn(inUse);
    when(predicate.hasBeenList()).thenReturn(hasBeenList);
    when(predicate.getDirection()).thenReturn(direction);
    when(predicate.isExplicit()).thenReturn(explicit);

    return predicate;
  }

  public PredicateBuilder hasBeenSingular() {
    this.hasBeenSingular = true;
    return this;
  }

  public PredicateBuilder hasBeenList() {
    this.isList = false;
    this.hasBeenList = true;
    return this;
  }

  public PredicateBuilder notInUse() {
    this.inUse = false;
    return this;
  }

  public PredicateBuilder explicit() {
    this.explicit = true;
    return this;
  }
}
