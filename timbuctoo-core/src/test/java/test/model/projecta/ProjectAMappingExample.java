package test.model.projecta;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.model.DerivedProperty;
import nl.knaw.huygens.timbuctoo.model.mapping.VirtualProperty;
import test.model.MappingExample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectAMappingExample extends MappingExample {
  public static final String CLIENT_AND_INDEX_SUB_INDEX = "test";
  public static final String CLIENT_AND_INDEX_SUB_CLIENT = "test2";
  public static final String DERIVED_1 = "derived1";
  public static final String DERIVED_2 = "derived2";
  public static final String LOCAL_ACCESSOR_1 = "getDerived1";
  public static final String LOCAL_ACCESSOR_2 = "getDerived2";

  public static final String DERIVED1_INDEX = "derived1_index";
  public static final String DERIVED2_INDEX = "derived2_index";

  public static final String VIRTUAL_INDEX = "virtualIndex";
  public static final String VIRTUAL_CLIENT = "virtualClient";

  public static final String VIRTUAL_CLIENT_2 = "virtualClient2";

  @JsonProperty(CLIENT_AND_INDEX_SUB_CLIENT)
  private Object clientAndIndexSub;

  @IndexAnnotation(fieldName = CLIENT_AND_INDEX_SUB_INDEX)
  public Object getClientAndIndexSub() {
    return clientAndIndexSub;
  }


  public static final DerivedProperty DERIVED_PROPERTY_1 = new DerivedProperty(DERIVED_1, "", "", LOCAL_ACCESSOR_1);
  public static final DerivedProperty DERIVED_PROPERTY_2 = new DerivedProperty(DERIVED_2, "", "", LOCAL_ACCESSOR_2);
  public static final ArrayList<DerivedProperty> DERIVED_PROPERTIES = Lists.newArrayList(DERIVED_PROPERTY_1, DERIVED_PROPERTY_2);

  @Override
  public List<DerivedProperty> getDerivedProperties() {

    return Collections.unmodifiableList(DERIVED_PROPERTIES);
  }

  @IndexAnnotation(fieldName = DERIVED1_INDEX)
  public String getDerived1() {
    return "";
  }

  @IndexAnnotation(fieldName = DERIVED2_INDEX)
  public static String getDerived2() {
    return "";
  }

  @IndexAnnotation(fieldName = VIRTUAL_INDEX)
  @VirtualProperty(propertyName = VIRTUAL_CLIENT)
  public String getVirtualProperty() {
    return null;
  }

  @Override
  @VirtualProperty(propertyName = VIRTUAL_CLIENT_2)
  public String getVirtualSuperProperty() {
    return super.getVirtualSuperProperty();
  }
}
