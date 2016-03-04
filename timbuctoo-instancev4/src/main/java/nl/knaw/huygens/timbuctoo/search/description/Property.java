package nl.knaw.huygens.timbuctoo.search.description;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

public class Property {

  private final String name;
  private final PropertyParser parser;

  private Property(String name, PropertyParser parser) {
    this.name = name;
    this.parser = parser;
  }

  public static PropertyNameBuilder localProperty() {
    return new Property.Builder();
  }

  public GraphTraversal<Object, Object> getTraversal() {
    if (parser == null) {
      return __.has(name).values(name);
    }

    return
      __.has(name).values(name).map(x -> parser.parseForSort("" + x.get()));
  }

  public interface PropertyNameBuilder {
    PropertyWithoutParserBuilder withName(String name);
  }

  public interface PropertyParserBuilder {
    PropertyBuilder withParser(PropertyParser parser);
  }

  public interface PropertyBuilder {
    Property build();
  }

  public interface PropertyWithoutParserBuilder extends PropertyParserBuilder, PropertyBuilder {

  }

  private static class Builder implements PropertyNameBuilder, PropertyWithoutParserBuilder {

    private PropertyParser parser;
    private String name;

    @Override
    public Property build() {
      return new Property(name, parser);
    }

    @Override
    public PropertyWithoutParserBuilder withName(String name) {
      this.name = name;
      return this;
    }

    @Override
    public PropertyBuilder withParser(PropertyParser parser) {
      this.parser = parser;
      return this;
    }

  }
}
