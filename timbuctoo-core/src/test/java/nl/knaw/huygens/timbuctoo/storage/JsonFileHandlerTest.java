package nl.knaw.huygens.timbuctoo.storage;

import static nl.knaw.huygens.timbuctoo.storage.JsonFileHandler.CONFIG_DIR_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.Configuration;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileHandlerTest {
  private static final String CONFIG_DIR = "configDir";
  private static final String FILE_NAME = "test";
  private static final File FILE = new File(createFilePath(CONFIG_DIR, FILE_NAME));
  private static final UserFileCollection COLLECTION = new UserFileCollection();
  private static final Class<UserFileCollection> COLLECTION_TYPE = UserFileCollection.class;
  private JsonFileHandler instance;
  private ObjectMapper objectMapper;
  private Configuration config;

  @Before
  public void setUp() {
    config = mock(Configuration.class);
    objectMapper = mock(ObjectMapper.class);
    instance = new JsonFileHandler(config, objectMapper);

    when(config.getDirectory(CONFIG_DIR_KEY)).thenReturn(CONFIG_DIR);
  }

  @Test
  public void testSaveCollection() throws Exception {
    // action
    instance.saveCollection(COLLECTION, FILE_NAME);

    // verify
    verify(objectMapper).writeValue(FILE, COLLECTION);
  }

  @Test(expected = StorageException.class)
  public void testSaveCollectionObjectMapperThrowsJsonGenerationException() throws Exception {
    testSaveCollectionObjectMapperThrowsAnException(JsonGenerationException.class);
  }

  @Test(expected = StorageException.class)
  public void testSaveCollectionObjectMapperThrowsJsonMappingException() throws Exception {
    testSaveCollectionObjectMapperThrowsAnException(JsonMappingException.class);
  }

  @Test(expected = StorageException.class)
  public void testSaveCollectionObjectMapperThrowsIOException() throws Exception {
    testSaveCollectionObjectMapperThrowsAnException(IOException.class);
  }

  private void testSaveCollectionObjectMapperThrowsAnException(Class<? extends Exception> exceptionToThrow) throws Exception {
    // setup
    doThrow(exceptionToThrow).when(objectMapper).writeValue(FILE, COLLECTION);

    try {
      // action
      instance.saveCollection(COLLECTION, FILE_NAME);
    } finally {
      // verify
      verify(objectMapper).writeValue(FILE, COLLECTION);
    }
  }

  @Test
  public void testGetCollectionWhenTheCollectionIsFound() throws Exception {
    // setup
    when(objectMapper.readValue(FILE, COLLECTION_TYPE)).thenReturn(COLLECTION);

    // action
    UserFileCollection actualCollection = instance.getCollection(COLLECTION_TYPE, FILE_NAME);

    // verify
    assertThat(actualCollection, is(equalTo(COLLECTION)));
  }

  @Test
  public void testGetCollectionWhenTheCollectionIsNotFound() throws Exception {
    // setup
    doThrow(FileNotFoundException.class).when(objectMapper).readValue(FILE, COLLECTION_TYPE);

    // action
    UserFileCollection actualCollection = instance.getCollection(UserFileCollection.class, FILE_NAME);

    // verify
    assertThat(actualCollection, is(nullValue(COLLECTION_TYPE)));

  }

  @Test(expected = StorageException.class)
  public void testGetCollectionObjectMapperThrowsJsonGenerationException() throws Exception {
    testGetCollectionObjectMapperThrowsAnException(JsonGenerationException.class);
  }

  @Test(expected = StorageException.class)
  public void testGetCollectionObjectMapperThrowsJsonMappingException() throws Exception {
    testGetCollectionObjectMapperThrowsAnException(JsonMappingException.class);
  }

  @Test(expected = StorageException.class)
  public void testGetCollectionObjectMapperThrowsIOException() throws Exception {
    testGetCollectionObjectMapperThrowsAnException(IOException.class);
  }

  private void testGetCollectionObjectMapperThrowsAnException(Class<? extends Exception> exceptionToThrow) throws Exception {
    // setup
    doThrow(exceptionToThrow).when(objectMapper).readValue(FILE, COLLECTION_TYPE);

    // action
    instance.getCollection(UserFileCollection.class, FILE_NAME);

  }

  private static String createFilePath(String dir, String fileName) {
    return String.format("%s%s%s", dir, File.separator, fileName);
  }
}
