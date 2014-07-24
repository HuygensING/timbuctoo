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

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeMatcher;

public class VREManagerMatcher extends TypeSafeMatcher<VREManager> {

  private final VREManager expectedVREManager;

  public VREManagerMatcher(VREManager expectedVREManager) {
    this.expectedVREManager = expectedVREManager;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("VREManager with vres ").appendValue(expectedVREManager.getAllVREs()) //
        .appendText(" and vre ids ").appendValue(expectedVREManager.getAvailableVREIds())//
        .appendText(" and indexes ").appendValue(expectedVREManager.getAllIndexes());
  }

  @Override
  protected void describeMismatchSafely(VREManager item, Description mismatchDescription) {
    mismatchDescription.appendText("VREManager with vres ").appendValue(item.getAllVREs()) //
        .appendText(" and vre ids ").appendValue(item.getAvailableVREIds())//
        .appendText(" and indexes ").appendValue(item.getAllIndexes());
  }

  @Override
  protected boolean matchesSafely(VREManager item) {
    boolean matches = everyItem(isIn(expectedVREManager.getAllIndexes())).matches(item.getAllIndexes());
    matches &= everyItem(isIn(expectedVREManager.getAllVREs())).matches(item.getAllVREs());
    matches &= everyItem(isIn(expectedVREManager.getAvailableVREIds())).matches(item.getAvailableVREIds());

    return matches;
  }

  @Factory
  public static VREManagerMatcher matchesVREManager(VREManager expectedVREManager) {
    return new VREManagerMatcher(expectedVREManager);
  }

}
