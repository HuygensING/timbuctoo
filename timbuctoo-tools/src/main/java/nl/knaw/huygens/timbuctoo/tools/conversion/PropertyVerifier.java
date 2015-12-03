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

import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PropertyVerifier {
  private List<Mismatch> misMatches;

  public PropertyVerifier() {
    misMatches = Lists.newArrayList();
  }

  public void check(String fieldName, Object oldValue, Object newValue) {
    if (!areEqual(fieldName, oldValue, newValue)) {
      addMismatch(fieldName, oldValue, newValue);
    }
  }

  protected boolean areEqual(String fieldName, Object oldValue, Object newValue) {
    /* Do not remove the Object.equals. EqualsBuilder.reflectionEquals does not
     * always recognize two equal Strings.
     */
    return Objects.equals(oldValue, newValue) || EqualsBuilder.reflectionEquals(oldValue, newValue);
  }

  private void addMismatch(String fieldName, Object oldValue, Object newValue) {
    misMatches.add(new Mismatch(fieldName, oldValue, newValue));
  }

  public boolean hasInconsistentProperties() {
    return !misMatches.isEmpty();
  }

  public Collection<Mismatch> getMismatches() {
    ArrayList<Mismatch> returnValue = Lists.newArrayList(misMatches);

    misMatches.clear();

    return returnValue;
  }

}
