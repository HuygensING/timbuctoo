package nl.knaw.huygens.timbuctoo.storage.file;

/*
 * #%L
 * Timbuctoo core
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

import static nl.knaw.huygens.timbuctoo.storage.file.JsonFileHandler.CONFIG_DIR_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.storage.StorageException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileHandlerTest {

  private static final String CONFIG_DIR = "configDir";
  private static final String FILE_NAME = "test";
  private static final Class<UserFileCollection> COLLECTION_TYPE = UserFileCollection.class;

  private JsonFileHandler instance;
  private ObjectMapper objectMapper;
  private UserFileCollection collection;
  private File file;

  @Before
  public void setup() {
    Configuration config = mock(Configuration.class);
    when(config.getDirectory(CONFIG_DIR_KEY)).thenReturn(CONFIG_DIR);
    objectMapper = mock(ObjectMapper.class);
    instance = new JsonFileHandler(config, objectMapper);
    collection = new UserFileCollection();
    file = new File(new File(CONFIG_DIR), FILE_NAME);
  }

  @Test
  public void testSaveCollection() throws Exception {
    // action
    instance.saveCollection(collection, FILE_NAME);

    // verify
    verify(objectMapper).writeValue(file, collection);
  }

  @Test(expected = StorageException.class)
  public void testSaveCollectionObjectMapperThrowsJsonProcessingException() throws Exception {
    doThrow(JsonProcessingException.class).when(objectMapper).writeValue(file, collection);
    instance.saveCollection(collection, FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void testSaveCollectionObjectMapperThrowsIOException() throws Exception {
    doThrow(IOException.class).when(objectMapper).writeValue(file, collection);
    instance.saveCollection(collection, FILE_NAME);
  }

  @Test
  public void testGetCollectionWhenTheCollectionIsFound() throws Exception {
    // setup
    when(objectMapper.readValue(file, COLLECTION_TYPE)).thenReturn(collection);

    // action
    UserFileCollection actualCollection = instance.getCollection(COLLECTION_TYPE, FILE_NAME);

    // verify
    assertThat(actualCollection, is(equalTo(collection)));
  }

  @Test
  public void testGetCollectionWhenTheCollectionIsNotFoundReturnsAnEmptyCollection() throws Exception {
    // setup
    doThrow(FileNotFoundException.class).when(objectMapper).readValue(file, COLLECTION_TYPE);

    // action
    UserFileCollection actualCollection = instance.getCollection(UserFileCollection.class, FILE_NAME);

    // verify
    assertThat(actualCollection, is(notNullValue(UserFileCollection.class)));
    assertThat(actualCollection.getIds(), emptyCollectionOf(String.class));
  }

  @Test(expected = StorageException.class)
  public void testGetCollectionObjectMapperThrowsJsonProcessingException() throws Exception {
    doThrow(JsonProcessingException.class).when(objectMapper).readValue(file, COLLECTION_TYPE);
    instance.getCollection(UserFileCollection.class, FILE_NAME);
  }

  @Test(expected = StorageException.class)
  public void testGetCollectionObjectMapperThrowsIOException() throws Exception {
    doThrow(IOException.class).when(objectMapper).readValue(file, COLLECTION_TYPE);
    instance.getCollection(UserFileCollection.class, FILE_NAME);
  }

}
