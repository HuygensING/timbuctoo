package nl.knaw.huygens.timbuctoo.tools.conversion;

/*
 * #%L
 * Timbuctoo tools
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class RelationPropertyVerfierTest extends PropertyVerifierTest {
  private static final String NEW_ID = "newId";
  private static final String OLD_ID = "oldId";
  private Map<String, String> oldIdNewIdMap;

  @Before
  @Override
  public void setup() {
    oldIdNewIdMap = Maps.newHashMap();
    oldIdNewIdMap.put(OLD_ID, NEW_ID);

    instance = new RelationPropertyVerifier(oldIdNewIdMap);
  }

  @Test
  public void checkRetrievesTheMappedNewIdFormTheOldIdNewIdMapForTheFieldSourceId() {
    // action
    instance.check("sourceId", OLD_ID, NEW_ID);

    // verify
    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), hasSize(0));
  }

  @Test
  public void checkRetrievesTheMappedNewIdFormTheOldIdNewIdMapForTheFieldTargetId() {
    // action
    instance.check("targetId", OLD_ID, NEW_ID);

    // verify
    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), hasSize(0));
  }

  @Test
  public void checkRetrievesTheMappedNewIdFormTheOldIdNewIdMapForTheFieldTypeId() {
    // action
    instance.check("typeId", OLD_ID, NEW_ID);

    // verify
    assertThat(instance.hasInconsistentProperties(), is(false));
    assertThat(instance.getMismatches(), hasSize(0));
  }
}
