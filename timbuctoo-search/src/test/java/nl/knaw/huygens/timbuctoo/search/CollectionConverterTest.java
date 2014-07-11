package nl.knaw.huygens.timbuctoo.search;

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class CollectionConverterTest {
  @Test
  public void testToFilterableSet() {
    // setup
    List<Integer> randomValues = Lists.newArrayList(1, 2, 4, 5);

    CollectionConverter collectionConverter = new CollectionConverter();

    // action
    FilterableSet<Integer> actualFilterableSet = collectionConverter.toFilterableSet(randomValues);

    // verify
    assertThat(actualFilterableSet, containsInAnyOrder(1, 2, 4, 5));
  }
}
