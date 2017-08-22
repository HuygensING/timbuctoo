package nl.knaw.huygens.timbuctoo.v5.jsonldimport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class JsonLdImportTest {
  @Test
  public void handlesJsonProperly() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new GuavaModule());
    JsonLdImport myvalue = objectMapper.readValue("{\n" +
      "\t\"@type\": \"Activity\",\n" +
      "\t\"@id\": \"testID\",\n" +
      "\t\"prov:qualifiedAssociation\": [{\n" +
      "\t\t\"agent\": \"http://pratham\",\n" +
      "\t\t\"prov:hadRole\": \"editor\"\n" +
      "\t}],\n" +
      "\t\"prov:used\": [{\n" +
      "\t\t\t\"prov:entity\": \"http://frontend-app\",\n" +
      "\t\t\t\"prov:hadRole\": \"http://edit-interface\"\n" +
      "\t\t},\n" +
      "\t\t{\n" +
      "\t\t\t\"prov:entity\": \"http://dbpedia.org/a_recherche_du_temps_perdu\",\n" +
      "\t\t\t\"prov:hadRole\": \"http://source/material\"\n" +
      "\t\t}\n" +
      "\t],\n" +
      "\t\"prov:generates\": [{\n" +
      "\t\t\"entityType\": \"Entity\",\n" +
      "\t\t\"specializationOf\": \"http://example.com/the/actual/entity\",\n" +
      "\t\t\"wasRevisionOf\": {\n" +
      "\t\t\t\"@id\": \"http://previous/mutation\"\n" +
      "\t\t},\n" +
      "\t\t\"tim:additions\": {\n" +
      "\t\t\t\"name\": \"extra name\"\n" +
      "\t\t},\n" +
      "\t\t\"tim:deletions\": {\n" +
      "\t\t\t\"name\": \"extra name\"\n" +
      "\t\t},\n" +
      "\t\t\"tim:replacements\": {\n" +
      "\t\t\t\"name\": \"extra name\"\n" +
      "\t\t}\n" +
      "\t}]\n" +
      "}", JsonLdImport.class);

    assertThat(myvalue, is(not(nullValue())));
  }
}

