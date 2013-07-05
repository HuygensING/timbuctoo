package nl.knaw.huygens.solr;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.google.common.collect.Lists;

public class FacetCount {
  private String name = "";
  private String title = "";
  private FacetType type;
  private final List<Option> options = Lists.newArrayList();

  public FacetCount setName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public FacetCount setTitle(String title) {
    this.title = title;
    return this;
  }

  public String getTitle() {
    return title;
  }

  public FacetCount setType(FacetType type) {
    this.type = type;
    return this;
  }

  public FacetType getType() {
    return type;
  }

  public FacetCount addOption(Option option) {
    options.add(option);
    return this;
  }

  public List<Option> getOptions() {
    return options;
  }

  static class Option {
    private String name = "";
    private long count = 0;

    public Option setName(String name) {
      this.name = name;
      return this;
    }

    public String getName() {
      return name;
    }

    public Option setCount(long l) {
      this.count = l;
      return this;
    }

    public long getCount() {
      return count;
    }
  }

  /* ------------------------------------------------------------------------------------------------------------------------------------ */

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE, false);
  }

}
