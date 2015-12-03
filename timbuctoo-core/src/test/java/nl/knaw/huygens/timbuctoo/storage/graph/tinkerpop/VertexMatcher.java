package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

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

import nl.knaw.huygens.hamcrest.CompositeMatcher;
import nl.knaw.huygens.hamcrest.PropertyEqualityMatcher;
import nl.knaw.huygens.timbuctoo.model.Entity;

import com.tinkerpop.blueprints.Vertex;

public class VertexMatcher extends CompositeMatcher<Vertex> {
  private VertexMatcher() {}

  public static VertexMatcher likeVertex() {
    return new VertexMatcher();
  }

  public VertexMatcher withId(String id) {
    addMatcher(new PropertyEqualityMatcher<Vertex, String>(Entity.DB_ID_PROP_NAME, id) {

      @Override
      protected String getItemValue(Vertex item) {
        return item.getProperty(Entity.DB_ID_PROP_NAME);
      }
    });

    return this;
  }
}
