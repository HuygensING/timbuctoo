package test.model.projecta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
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

  public static final VirtualProperty VIRTUAL_PROPERTY = new VirtualProperty(VIRTUAL_CLIENT, "getVirtualProperty");
  public static final VirtualProperty VIRTUAL_PROPERTY_ACCESSOR_IN_SUPER = new VirtualProperty("virtualClient2", "getVirtualSuperProperty");
  public static final List<VirtualProperty> VIRTUAL_PROPERTIES = ImmutableList.of(VIRTUAL_PROPERTY, VIRTUAL_PROPERTY_ACCESSOR_IN_SUPER);

  public List<VirtualProperty> getVirtualProperties() {
    return VIRTUAL_PROPERTIES;
  }

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
  public String getVirtualProperty(){
    return null;
  }

  @Override
  public String getVirtualSuperProperty() {
    return super.getVirtualSuperProperty();
  }
}
