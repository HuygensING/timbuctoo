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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.TimbuctooQuery;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.PipeFunctionFactory;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopResultFilter;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopResultFilterBuilder;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Vertex;

public class TinkerPopResultFilterBuilderTest {
  private PropertyBusinessRules businessRules;
  private PipeFunctionFactory pipeLineFunctionFactory;
  private TinkerPopResultFilterBuilder instance;
  private TimbuctooQuery queryMock;

  @Before
  public void setUp() {
    queryMock = mock(TimbuctooQuery.class);
    businessRules = new PropertyBusinessRules();
    pipeLineFunctionFactory = mock(PipeFunctionFactory.class);

    instance = new TinkerPopResultFilterBuilder(businessRules, pipeLineFunctionFactory);
  }

  @Test
  public void buildCreatesAnIsDistinctFilterForEveryFieldName() throws Exception {
    // action
    TinkerPopResultFilter<Vertex> resultFilter = instance.buildFor(queryMock);

    // verify
    assertThat(resultFilter, is(notNullValue()));

    verify(queryMock).addFilterOptionsToResultFilter(resultFilter);

  }

}
