package test.model.derivedrelationtest;

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

import com.google.common.collect.ImmutableList;
import nl.knaw.huygens.timbuctoo.model.DerivedRelationDescription;
import nl.knaw.huygens.timbuctoo.model.Person;

import java.util.List;

public class DRTPerson extends Person {
  public static final String DERIVED_RELATION = "hasPersonLang";
  private static final DerivedRelationDescription PERSON_HAS_LANG = new DerivedRelationDescription(DERIVED_RELATION, "isPersonOf", "hasLanguage");
  private static final List<DerivedRelationDescription> DERIVED_RELATION_TYPES = ImmutableList.of(PERSON_HAS_LANG);

  @Override
  public List<DerivedRelationDescription> getDerivedRelationDescriptions() {
    return DERIVED_RELATION_TYPES;
  }
}
