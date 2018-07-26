package nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.v5.datastores.prefixstore.TypeNameStore;
import nl.knaw.huygens.timbuctoo.v5.datastores.quadstore.dto.Direction;
import nl.knaw.huygens.timbuctoo.v5.datastores.schemastore.dto.Predicate;
import nl.knaw.huygens.timbuctoo.v5.util.RdfConstants;
import org.junit.Test;

import static nl.knaw.huygens.timbuctoo.v5.graphql.derivedschema.PredicateBuilder.predicate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DerivedInputTypeSchemaGeneratorTest {

  @Test
  public void createsAnInputType() {
    String typeUri = "http://example.org/type";
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeGraphQlname(typeUri)).thenReturn("Type");
    DerivedInputTypeSchemaGenerator instance = new DerivedInputTypeSchemaGenerator(typeUri, typeNameStore);
    Predicate valueNonList = predicate().withName("http://example.com/valueNonList")
                                        .hasDirection(Direction.OUT)
                                        .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/valueNonList", Direction.OUT, false))
      .thenReturn("short_singleValue");
    Predicate valueList = predicate().withName("http://example.com/valueList")
                                     .isList()
                                     .hasDirection(Direction.OUT)
                                     .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/valueList", Direction.OUT, true))
      .thenReturn("short_multiValueList");
    instance.valueField(null, valueNonList, RdfConstants.STRING);
    instance.valueField(null, valueList, RdfConstants.STRING);


    String schema = instance.getSchema().toString();

    assertThat(schema, containsString("input TypeInput {\n" +
      "  additions: TypeAdditionsInput\n" +
      "  deletions: TypeDeletionsInput\n" +
      "  replacements: TypeReplacementsInput\n" +
      "}\n\n"));
  }

  @Test
  public void addsAdditionsDeletionsAndReplacementsInput() {
    String typeUri = "http://example.org/type";
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeGraphQlname(typeUri)).thenReturn("Type");
    DerivedInputTypeSchemaGenerator instance = new DerivedInputTypeSchemaGenerator(typeUri, typeNameStore);
    Predicate valueNonList = predicate().withName("http://example.com/valueNonList")
                                        .hasDirection(Direction.OUT)
                                        .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/valueNonList", Direction.OUT, false))
      .thenReturn("short_singleValue");
    Predicate valueList = predicate().withName("http://example.com/valueList")
                                     .isList()
                                     .hasDirection(Direction.OUT)
                                     .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/valueList", Direction.OUT, true))
      .thenReturn("short_multiValueList");
    instance.valueField(null, valueNonList, RdfConstants.STRING);
    instance.valueField(null, valueList, RdfConstants.STRING);

    String schema = instance.getSchema().toString();

    assertThat(schema, allOf(
      containsString("input TypeAdditionsInput {\n" +
        "  short_multiValueList: [PropertyInput!]\n" +
        "}\n\n"),
      containsString("input TypeDeletionsInput {\n" +
        "  short_multiValueList: [PropertyInput!]\n" +
        "}\n\n"),
      containsString("input TypeReplacementsInput {\n" +
        "  short_singleValue: PropertyInput\n" +
        "  short_multiValueList: [PropertyInput!]\n" +
        "}\n\n")
    ));
  }

  @Test
  public void createAnEmptySchemaWhenNoPropertiesAreAdded() {
    String typeUri = "http://example.org/type";
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeGraphQlname(typeUri)).thenReturn("Type");
    DerivedInputTypeSchemaGenerator instance = new DerivedInputTypeSchemaGenerator(typeUri, typeNameStore);

    String schema = instance.getSchema().toString();

    assertThat(schema, isEmptyString());
  }

  @Test
  public void addsNoAdditionsOrDeletionsWhenNoListPredicatesAreAdded() {
    String typeUri = "http://example.org/type";
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeGraphQlname(typeUri)).thenReturn("Type");
    DerivedInputTypeSchemaGenerator instance = new DerivedInputTypeSchemaGenerator(typeUri, typeNameStore);
    Predicate valueNonList = predicate().withName("http://example.com/valueNonList")
                                        .hasDirection(Direction.OUT)
                                        .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/valueNonList", Direction.OUT, false))
      .thenReturn("short_singleValue");
    instance.valueField(null, valueNonList, RdfConstants.STRING);

    String schema = instance.getSchema().toString();

    assertThat(schema, allOf(
      not(containsString("input TypeAdditionsInput {\n" +
        "}\n\n")),
      not(containsString("input TypeDeletionsInput {\n" +
        "}\n\n")),
      containsString("input TypeReplacementsInput {\n" +
        "  short_singleValue: PropertyInput\n" +
        "}\n\n"),
      containsString("input TypeInput {\n" +
        "  replacements: TypeReplacementsInput\n" +
        "}\n\n")
    ));
  }

  @Test
  public void addsADeprecatedInputFieldForSingleForAListThatWasASingle() {
    String typeUri = "http://example.org/type";
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeGraphQlname(typeUri)).thenReturn("Type");
    DerivedInputTypeSchemaGenerator instance = new DerivedInputTypeSchemaGenerator(typeUri, typeNameStore);
    Predicate valueList = predicate().withName("http://example.com/wasSingleList")
                                     .isList()
                                     .hasBeenSingular()
                                     .hasDirection(Direction.OUT)
                                     .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/wasSingleList", Direction.OUT, false))
      .thenReturn("short_wasSingle");
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/wasSingleList", Direction.OUT, true))
      .thenReturn("short_wasSingleList");
    instance.valueField(null, valueList, RdfConstants.STRING);

    String schema = instance.getSchema().toString();

    assertThat(schema, allOf(
      containsString("input TypeReplacementsInput {\n" +
        "  short_wasSingleList: [PropertyInput!]\n" +
        "  short_wasSingle: PropertyInput" +
        " @deprecated(reason: \"This property only returns the first value of the list. Use the *List version\")\n" +
        "}\n\n")
    ));
  }

  @Test
  public void addsADeprecatedInputFieldForAListThatIsNotUsed() {
    String typeUri = "http://example.org/type";
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeGraphQlname(typeUri)).thenReturn("Type");
    DerivedInputTypeSchemaGenerator instance = new DerivedInputTypeSchemaGenerator(typeUri, typeNameStore);
    Predicate unusedPred = predicate().withName("http://example.com/unused")
                                      .notInUse()
                                      .hasBeenList()
                                      .hasDirection(Direction.OUT)
                                      .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/unused", Direction.OUT, true))
      .thenReturn("short_wasListList");
    instance.valueField(null, unusedPred, RdfConstants.STRING);

    String schema = instance.getSchema().toString();

    assertThat(schema, allOf(
      containsString("input TypeReplacementsInput {\n" +
        "  short_wasListList: [PropertyInput!]" +
        " @deprecated(reason: \"There used to be entities with this property, but that is no longer the case.\")\n" +
        "}\n\n")
    ));
  }

  @Test
  public void addsADeprecatedInputFieldForASingleValueThatIsNotUsed() {
    String typeUri = "http://example.org/type";
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeGraphQlname(typeUri)).thenReturn("Type");
    DerivedInputTypeSchemaGenerator instance = new DerivedInputTypeSchemaGenerator(typeUri, typeNameStore);
    Predicate unusedPred = predicate().withName("http://example.com/unused")
                                      .notInUse()
                                      .hasBeenSingular()
                                      .hasDirection(Direction.OUT)
                                      .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/unused", Direction.OUT, false))
      .thenReturn("short_unUsed");
    instance.valueField(null, unusedPred, RdfConstants.STRING);

    String schema = instance.getSchema().toString();

    assertThat(schema,containsString("input TypeReplacementsInput {\n" +
        "  short_unUsed: PropertyInput" +
        " @deprecated(reason: \"There used to be entities with this property, but that is no " +
        "longer the case.\")\n" +
        "}\n\n"));
  }

  @Test
  public void addObjectFieldAddsField() {
    String typeUri = "http://example.org/type";
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeGraphQlname(typeUri)).thenReturn("Type");
    DerivedInputTypeSchemaGenerator instance = new DerivedInputTypeSchemaGenerator(typeUri, typeNameStore);
    Predicate valueList = predicate().withName("http://example.com/valueList")
                                     .isList()
                                     .hasDirection(Direction.OUT)
                                     .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/valueList", Direction.OUT, true))
      .thenReturn("short_multiValueList");
    instance.objectField(null, valueList, "http://example.org/person");

    String schema = instance.getSchema().toString();

    assertThat(schema, allOf(
      containsString("input TypeAdditionsInput {\n" +
        "  short_multiValueList: [PropertyInput!]\n" +
        "}\n\n"),
      containsString("input TypeDeletionsInput {\n" +
        "  short_multiValueList: [PropertyInput!]\n" +
        "}\n\n"),
      containsString("input TypeReplacementsInput {\n" +
        "  short_multiValueList: [PropertyInput!]\n" +
        "}\n\n")
    ));
  }

  @Test
  public void addUnionAddsField() {
    String typeUri = "http://example.org/type";
    TypeNameStore typeNameStore = mock(TypeNameStore.class);
    when(typeNameStore.makeGraphQlname(typeUri)).thenReturn("Type");
    DerivedInputTypeSchemaGenerator instance = new DerivedInputTypeSchemaGenerator(typeUri, typeNameStore);
    Predicate valueList = predicate().withName("http://example.com/valueList")
                                     .isList()
                                     .hasDirection(Direction.OUT)
                                     .build();
    when(typeNameStore.makeGraphQlnameForPredicate("http://example.com/valueList", Direction.OUT, true))
      .thenReturn("short_multiValueList");

    instance.unionField(null, valueList, Sets.newHashSet("http://example.org/person", "http://example.org/person2"));
    String schema = instance.getSchema().toString();

    assertThat(schema, allOf(
      containsString("input TypeAdditionsInput {\n" +
        "  short_multiValueList: [PropertyInput!]\n" +
        "}\n\n"),
      containsString("input TypeDeletionsInput {\n" +
        "  short_multiValueList: [PropertyInput!]\n" +
        "}\n\n"),
      containsString("input TypeReplacementsInput {\n" +
        "  short_multiValueList: [PropertyInput!]\n" +
        "}\n\n")
    ));
  }


  // TODO add test for isExplicit

}
