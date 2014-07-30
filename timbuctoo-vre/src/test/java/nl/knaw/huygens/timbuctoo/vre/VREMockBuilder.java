package nl.knaw.huygens.timbuctoo.vre;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import nl.knaw.huygens.timbuctoo.index.Index;

import com.google.common.collect.Lists;

public class VREMockBuilder {

  private String name;
  private List<Index> indexes;

  private VREMockBuilder() {}

  public static VREMockBuilder newVRE() {
    return new VREMockBuilder();
  }

  public VREMockBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public VREMockBuilder withIndexes(Index... indexes) {
    this.indexes = Lists.newArrayList(indexes);
    return this;
  }

  public VRE create() {
    VRE vreMock = mock(VRE.class);
    when(vreMock.getName()).thenReturn(name);
    when(vreMock.getIndexes()).thenReturn(indexes);
    return vreMock;
  }

}
