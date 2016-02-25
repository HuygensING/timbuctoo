package nl.knaw.huygens.timbuctoo.search.description.sort;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

public class SortFieldDescription {
  private final String name;
  private final GraphTraversal<?, ?> traversal;

  SortFieldDescription(String name, GraphTraversal<?, ?> traversal) {
    this.name = name;
    this.traversal = traversal;
  }

  public static SortFieldDescriptionNameBuilder newSortFieldDescription() {
    return new Builder();
  }

  public String getName() {
    return name;
  }

  public GraphTraversal<?, ?> getTraversal() {
    return traversal;
  }

  public interface SortFieldDescriptionNameBuilder {
    SortFieldDescriptionPropertyBuilder withName(String name);
  }

  public interface SortFieldDescriptionPropertyBuilder {
    SortFieldDescriptionBuilder withProperty(Property.PropertyBuilder property);
  }

  public interface SortFieldDescriptionBuilder {
    SortFieldDescription build();
  }

  private static class Builder implements SortFieldDescriptionNameBuilder,
    SortFieldDescriptionBuilder, SortFieldDescriptionPropertyBuilder {
    private String name;
    private Property property;

    public SortFieldDescriptionBuilder withProperty(Property.PropertyBuilder property) {
      this.property = property.build();
      return this;
    }

    public SortFieldDescription build() {
      return new SortFieldDescription(name, property.getTraversal());
    }

    public Builder withName(String name) {
      this.name = name;
      return this;
    }
  }

}
