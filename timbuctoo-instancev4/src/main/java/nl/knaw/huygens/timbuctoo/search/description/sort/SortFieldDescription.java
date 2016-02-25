package nl.knaw.huygens.timbuctoo.search.description.sort;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

public class SortFieldDescription {
  private final String name;
  private final GraphTraversal<Object, Object> traversal;
  private final Object defaultValue;

  SortFieldDescription(String name, GraphTraversal<Object, Object> traversal, Object defaultValue) {
    this.name = name;
    this.traversal = traversal;
    this.defaultValue = defaultValue;
  }

  public static SortFieldDescriptionNameBuilder newSortFieldDescription() {
    return new Builder();
  }

  public String getName() {
    return name;
  }

  @SuppressWarnings("unchecked")
  public GraphTraversal<Object, Object> getTraversal() {
    return __.coalesce(traversal, __.map(x -> defaultValue));
  }

  public interface SortFieldDescriptionNameBuilder {
    SortFieldDescriptionDefaultValueBuilder withName(String name);
  }

  public interface SortFieldDescriptionDefaultValueBuilder {
    SortFieldDescriptionPropertyBuilder withDefaultValue(Comparable<?> value);
  }

  public interface SortFieldDescriptionPropertyBuilder {
    SortFieldDescriptionBuilder withProperty(Property.PropertyBuilder property);
  }

  public interface SortFieldDescriptionBuilder {
    SortFieldDescription build();
  }

  private static class Builder implements SortFieldDescriptionNameBuilder,
    SortFieldDescriptionBuilder, SortFieldDescriptionPropertyBuilder, SortFieldDescriptionDefaultValueBuilder {
    private String name;
    private Property property;
    private Comparable<?> value;

    public SortFieldDescriptionBuilder withProperty(Property.PropertyBuilder property) {
      this.property = property.build();
      return this;
    }

    public SortFieldDescription build() {
      return new SortFieldDescription(name, property.getTraversal(), value);
    }

    public SortFieldDescriptionDefaultValueBuilder withName(String name) {
      this.name = name;
      return this;
    }

    @Override
    public SortFieldDescriptionPropertyBuilder withDefaultValue(Comparable<?> value) {
      this.value = value;
      return this;
    }
  }

}
