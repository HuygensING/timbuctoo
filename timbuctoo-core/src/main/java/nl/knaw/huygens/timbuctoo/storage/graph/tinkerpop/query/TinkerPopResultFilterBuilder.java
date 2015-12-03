package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
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

import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;

import com.tinkerpop.blueprints.Element;

public class TinkerPopResultFilterBuilder {

  private final PropertyBusinessRules businessRules;
  private final PipeFunctionFactory pipeFunctionFactory;

  public TinkerPopResultFilterBuilder() {
    this(new PropertyBusinessRules(), new PipeFunctionFactory());
  }

  public TinkerPopResultFilterBuilder(PropertyBusinessRules businessRules, PipeFunctionFactory pipeFunctionFactory) {
    this.businessRules = businessRules;
    this.pipeFunctionFactory = pipeFunctionFactory;
  }

  public <T extends Element> TinkerPopResultFilter<T> buildFor(TimbuctooQuery query) {
    TinkerPopResultFilter<T> resultFilter = new TinkerPopResultFilter<T>(pipeFunctionFactory, businessRules);

    query.addFilterOptionsToResultFilter(resultFilter);

    return resultFilter;
  }
}
