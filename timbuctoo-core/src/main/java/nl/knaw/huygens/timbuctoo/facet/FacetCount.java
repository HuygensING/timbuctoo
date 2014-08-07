package nl.knaw.huygens.timbuctoo.facet;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2014 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.FacetType;

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

  public static class Option {
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
