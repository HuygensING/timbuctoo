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

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Test;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.pipes.PipeFunction;

public class PipeFunctionFactoryTest {
  private static final String ID = "id";

  @Test
  public void forPropertyCreatesAPipeFunctionThatRetrievesAPropertyFromAnElement() {
    // setup
    Vertex vertex = aVertex().withId(ID).build();

    PipeFunctionFactory instance = new PipeFunctionFactory();

    // action
    PipeFunction<Vertex, String> pipeFunction = instance.forDistinctProperty(Entity.DB_ID_PROP_NAME);

    // verify
    assertThat(pipeFunction.compute(vertex), is(ID));
  }
}
