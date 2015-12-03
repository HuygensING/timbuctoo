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

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import test.model.projectb.ProjectBPerson;

import com.google.common.collect.Lists;

public class DomainEntityConversionVerifierTest {
  private MongoConversionStorage mongoStorage;
  private TinkerPopConversionStorage graphStorage;
  private PropertyVerifier propertyVerifier;
  private DomainEntityConversionVerifier<ProjectBPerson> instance;
  private ProjectBPerson mongoVersion;
  private ProjectBPerson graphVersion;

  private static final String NEW_ID = "newId";
  private static final Object NEW_INTERNAL_ID = "newInternalId";
  private static final int REVISION = 12;
  private static final String OLD_ID = "oldId";
  private static final Class<ProjectBPerson> TYPE = ProjectBPerson.class;

  @Before
  public void setup() throws Exception {

    mongoStorage = mock(MongoConversionStorage.class);
    graphStorage = mock(TinkerPopConversionStorage.class);
    propertyVerifier = mock(PropertyVerifier.class);
    instance = new DomainEntityConversionVerifier<ProjectBPerson>(TYPE, mongoStorage, graphStorage, propertyVerifier, REVISION);

    mongoVersion = new ProjectBPerson();
    when(mongoStorage.getRevision(TYPE, OLD_ID, REVISION)).thenReturn(mongoVersion);
    graphVersion = new ProjectBPerson();
    when(graphStorage.getEntityByVertexId(TYPE, NEW_INTERNAL_ID)).thenReturn(graphVersion);
  }

  @Test
  public void verifyConversionRetrievesTheMongoVersionAndTheGraphVersionOfAnObject() throws Exception {
    // action
    instance.verifyConversion(OLD_ID, NEW_ID, NEW_INTERNAL_ID);

    // verify
    verify(mongoStorage).getRevision(TYPE, OLD_ID, REVISION);
    verify(graphStorage).getEntityByVertexId(TYPE, NEW_INTERNAL_ID);
  }

  @Test
  public void verifyConversionGetsTheLatestVersionFromMongoIfTheRevisionCannotBeFoundInTheVersionCollection() throws Exception {
    // setup
    when(mongoStorage.getRevision(TYPE, OLD_ID, REVISION)).thenReturn(null);
    when(mongoStorage.getEntity(TYPE, OLD_ID)).thenReturn(mongoVersion);

    // action
    instance.verifyConversion(OLD_ID, NEW_ID, NEW_INTERNAL_ID);

    // verify
    verify(mongoStorage).getRevision(TYPE, OLD_ID, REVISION);
    verify(mongoStorage).getEntity(TYPE, OLD_ID);
    verify(graphStorage).getEntityByVertexId(TYPE, NEW_INTERNAL_ID);
  }

  @Test
  public void verifyConversionVerifiesAllTheFieldsExceptId() throws Exception {
    // action
    instance.verifyConversion(OLD_ID, NEW_ID, NEW_INTERNAL_ID);

    // verify

    // entity
    verifyCheck("created");
    verifyCheck("modified");
    verifyCheck("rev");

    // domain entity
    verifyCheck("displayName");
    verifyCheck("pid");
    verifyCheck("deleted");
    verifyCheck("relationCount");
    verifyCheck("properties");
    verifyCheck("relations");
    verifyCheck("names");
    verifyCheck("variations");

    // person
    verifyCheck("names");
    verifyCheck("gender");
    verifyCheck("birthDate");
    verifyCheck("deathDate");
    verifyCheck("types");
    verifyCheck("links");
    verifyCheck("floruit");

  }

  private void verifyCheck(String fieldName) {
    verify(propertyVerifier).check(argThat(is(fieldName)), any(), any());
  }

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Test
  public void verifyConversionThrowsAnVerificationExceptionIfOneOrMoreOfTheFieldsContainsDifferentValues() throws Exception {
    // setup
    when(propertyVerifier.hasInconsistentProperties()).thenReturn(true);
    Mismatch mismatch = new Mismatch("fieldName", "oldValue", "newValue");
    ArrayList<Mismatch> mismatches = Lists.newArrayList(mismatch);
    when(propertyVerifier.getMismatches()).thenReturn(mismatches);

    exception.expect(VerificationException.class);
    exception.expectMessage(mismatch.toString());

    // instance
    instance.verifyConversion(OLD_ID, NEW_ID, NEW_INTERNAL_ID);
  }
}
