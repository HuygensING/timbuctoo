package nl.knaw.huygens.solr;

/*
 * #%L
 * Timbuctoo search
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;

public class FacetParameter {

  String name = "";
  List<String> values = Lists.newArrayList();

  public String getName() {
    return name;
  }

  public FacetParameter setName(String name) {
    this.name = name;
    return this;
  }

  public List<String> getValues() {
    return values;
  }

  public FacetParameter setValues(List<String> values) {
    this.values = values;
    return this;
  }

  public List<String> getEscapedValues() {
    Builder<String> builder = ImmutableList.builder();
    for (String value : getValues()) {
      builder.add(SolrUtils.escapeFacetId(value));
    }
    return builder.build();
  }
}
