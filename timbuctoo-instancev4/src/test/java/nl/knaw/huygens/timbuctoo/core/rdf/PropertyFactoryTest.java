package nl.knaw.huygens.timbuctoo.core.rdf;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.core.RdfImportErrorReporter;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.CreateProperty;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.ImmutablePredicateInUse;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.ImmutableValueTypeInUse;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.PredicateInUse;
import nl.knaw.huygens.timbuctoo.core.dto.rdf.ValueTypeInUse;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PropertyFactoryTest {

  private RdfImportErrorReporter importErrorReporter;
  private PropertyFactory instance;

  @Before
  public void setUp() throws Exception {
    importErrorReporter = mock(RdfImportErrorReporter.class);
    instance = new PropertyFactory(importErrorReporter);
  }

  @Test
  public void fromRdfPredicatesCreatesThePropertyWithTheValueTypeThatHasTheMostEntities() {
    List<PredicateInUse> predicates = createPredicates();

    List<CreateProperty> createProperties = instance.fromPredicates(predicates);

    assertThat(createProperties, containsInAnyOrder(
      allOf(
        hasProperty("clientName", equalTo("pred1")),
        hasProperty("typeUri", equalTo("http://example.org/type1")),
        hasProperty("rdfUri", equalTo("http://example.org/pred1"))
      ),
      allOf(
        hasProperty("clientName", equalTo("pred2")),
        hasProperty("typeUri", equalTo("http://example.org/type4")),
        hasProperty("rdfUri", equalTo("http://example.org/pred2"))
      )
    ));
  }

  @Test
  public void fromRdfPredicateLogsEveryEntityWithThatHasADifferentValueTypeThanTheMajority() {
    List<PredicateInUse> predicates = createPredicates();

    instance.fromPredicates(predicates);

    verify(importErrorReporter).entityHasWrongTypeForProperty(
      "entity3",
      "http://example.org/pred1",
      "http://example.org/type1",
      "http://example.org/type2"
    );
    verify(importErrorReporter).entityHasWrongTypeForProperty(
      "entity1",
      "http://example.org/pred2",
      "http://example.org/type4",
      "http://example.org/type3"
    );
  }

  private List<PredicateInUse> createPredicates() {
    ValueTypeInUse valueType1 = ImmutableValueTypeInUse.builder()
                                                       .typeUri("http://example.org/type1")
                                                       .addEntitiesConnected("entity1", "entity2")
                                                       .build();
    ValueTypeInUse valueType2 = ImmutableValueTypeInUse.builder()
                                                       .typeUri("http://example.org/type2")
                                                       .addEntitiesConnected("entity3")
                                                       .build();
    PredicateInUse pred1 = ImmutablePredicateInUse.builder()
                                                  .predicateUri("http://example.org/pred1")
                                                  .addValueTypes(valueType1, valueType2)
                                                  .build();
    ValueTypeInUse valueType3 = ImmutableValueTypeInUse.builder()
                                                       .typeUri("http://example.org/type3")
                                                       .addEntitiesConnected("entity1")
                                                       .build();
    ValueTypeInUse valueType4 = ImmutableValueTypeInUse.builder()
                                                       .typeUri("http://example.org/type4")
                                                       .addEntitiesConnected("entity3", "entity2")
                                                       .build();
    PredicateInUse pred2 = ImmutablePredicateInUse.builder()
                                                  .predicateUri("http://example.org/pred2")
                                                  .addValueTypes(valueType3, valueType4)
                                                  .build();
    return Lists.newArrayList(pred1, pred2);
  }


}
