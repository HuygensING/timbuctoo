package nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto;


import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExplicitTypeTest {
  @Test
  public void convertToTypeConvertsExplicitTypeWithoutFieldsToType() throws Exception {
    ExplicitType explicitType = new ExplicitType("TestType", null);

    Type type = explicitType.convertToType();

    assertThat(type.getName(), is("TestType"));
  }

  @Test
  public void convertToTypeConvertsExplicitTypeWithFieldsToType() throws Exception {
    ExplicitField title = new ExplicitField("title", "", "", false,
      null,null, new ExplicitType("String", null));
    List<ExplicitField> fieldList = new ArrayList<>();
    fieldList.add(title);
    ExplicitType explicitType = new ExplicitType("TestType", fieldList);

    Type type = explicitType.convertToType();

    assertThat(type.getName(), is("TestType"));
  }
}

