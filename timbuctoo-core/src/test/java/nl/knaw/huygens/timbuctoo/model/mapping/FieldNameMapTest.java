package nl.knaw.huygens.timbuctoo.model.mapping;

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

import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FieldNameMapTest {

  public static final String FROM_1 = "from1";
  public static final String FROM_2 = "from2";
  public static final String TO_2 = "to2";
  public static final String TO_1 = "to1";
  public static final String NON_MAPPED = "nonMapped";
  public static final String SIMPLE_VALUE = "value1";
  public static final String OTHER_SIMPLE_VALUE = "value2";

  @Test
  public void remapIteratesThroughAllTheFromFieldsAndRemapsThemIfTheyExistInTheSourceMap() {
    // setup
    DomainEntity entity = mock(DomainEntity.class);
    FieldNameMap instance = new FieldNameMap(entity);
    instance.put(FROM_1, TO_1);
    instance.put(FROM_2, TO_2);

    Map<String, String> input = Maps.newHashMap();
    input.put(FROM_1, SIMPLE_VALUE);
    input.put(NON_MAPPED, OTHER_SIMPLE_VALUE);

    // action
    Map<String, String> output = instance.remap(input);

    // verify
    ArgumentCaptor<Map> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
    verify(entity).createRelSearchRep(mapArgumentCaptor.capture());

    Map<String, Object> capturedMap = mapArgumentCaptor.getValue();
    assertThat(capturedMap.keySet(), containsInAnyOrder(TO_1));
    assertThat(capturedMap.get(TO_1), is(SIMPLE_VALUE));
  }


}
