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

import static nl.knaw.huygens.timbuctoo.tools.conversion.PersonMatcher.likePerson;
import static nl.knaw.huygens.timbuctoo.tools.conversion.PersonMatcher.likeProjectAPerson;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import test.model.projecta.ProjectAPerson;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class RevisionConverterTest {
  private static final String OLD_ID = "oldId";
  private static final String NEW_ID = "newId";
  private static final Object NEW_INTERNAL_ID = "newInternalId";
  private static final int REVISION = 1;
  private static final Class<ProjectAPerson> TYPE2 = ProjectAPerson.class;
  private static final Class<Person> TYPE1 = Person.class;
  private Graph graph;
  private VariationConverter variationConverter;
  private RevisionConverter instance;
  private Vertex vertex;
  private ConversionVerifierFactory verifierFactory;

  @Before
  public void setup() {
    setupGraph();
    verifierFactory = mock(ConversionVerifierFactory.class);
    variationConverter = mock(VariationConverter.class);
    instance = new RevisionConverter(graph, variationConverter, verifierFactory);
  }

  private void setupGraph() {
    graph = mock(Graph.class);
    vertex = mock(Vertex.class);
    when(vertex.getId()).thenReturn(NEW_INTERNAL_ID);
    when(graph.addVertex(null)).thenReturn(vertex);
  }

  @Test
  public void convertAddsAllTheVariationsToTheCreatedVertex() throws IllegalArgumentException, IllegalAccessException, StorageException {
    // setup
    Person variant1 = new Person();
    ProjectAPerson variant2 = new ProjectAPerson();

    EntityConversionVerifier conversionVerifier1 = mock(EntityConversionVerifier.class);
    when(verifierFactory.createFor(TYPE1, REVISION)).thenReturn(conversionVerifier1);
    EntityConversionVerifier conversionVerifier2 = mock(EntityConversionVerifier.class);
    when(verifierFactory.createFor(TYPE2, REVISION)).thenReturn(conversionVerifier2);

    // action
    instance.convert(OLD_ID, NEW_ID, Lists.newArrayList(variant1, variant2), REVISION);

    // verify
    InOrder inOrder1 = inOrder(variationConverter, conversionVerifier1);
    inOrder1.verify(variationConverter).addDataToVertex(//
        argThat(is(vertex)), //
        argThat(likePerson().withId(NEW_ID)));
    inOrder1.verify(conversionVerifier1).verifyConversion(OLD_ID, NEW_ID, NEW_INTERNAL_ID);

    InOrder inOrder2 = inOrder(variationConverter, conversionVerifier2);
    inOrder2.verify(variationConverter).addDataToVertex(argThat(is(vertex)), argThat(likeProjectAPerson().withId(NEW_ID)));
    inOrder2.verify(conversionVerifier2).verifyConversion(OLD_ID, NEW_ID, NEW_INTERNAL_ID);
  }
}
