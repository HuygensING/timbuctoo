package nl.knaw.huygens.timbuctoo.search.description.facet;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

public class Facet {

  private static final String LIST = "LIST";

  private final String name;
  private final List<Option> options;

  public Facet(String name, List<Option> options) {
    this.name = name;
    this.options = options;
  }

  public List<Option> getOptions() {
    return this.options;
  }


  public String getName() {
    return name;
  }

  public String getType() {
    return LIST;
  }

  public static class Option {
    private final String name;
    private final long count;

    public Option(String name, long count) {
      this.name = name;
      this.count = count;
    }


    public String getName() {
      return name;
    }

    public long getCount() {
      return count;
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object obj) {
      return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
      return HashCodeBuilder.reflectionHashCode(this);
    }
  }
}
