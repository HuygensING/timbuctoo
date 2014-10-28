package nl.knaw.huygens.timbuctoo.storage;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import nl.knaw.huygens.timbuctoo.config.Configuration;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * A class that writes and reads json files.
 */
public class JsonFileHandler {
  static final String CONFIG_DIR_KEY = "admin_data.directory";
  private ObjectMapper objectMapper;
  private Configuration config;

  @Inject
  public JsonFileHandler(Configuration config, ObjectMapper objectMapper) {
    this.config = config;
    this.objectMapper = objectMapper;
  }

  public <T extends FileCollection<? extends SystemEntity>> void saveCollection(T collection, String fileName) throws StorageException {
    try {
      objectMapper.writeValue(getFile(fileName), collection);
    } catch (JsonGenerationException e) {
      throw new StorageException(e);
    } catch (JsonMappingException e) {
      throw new StorageException(e);
    } catch (IOException e) {
      throw new StorageException(e);
    }
  }

  private File getFile(String fileName) {
    return new File(createPath(fileName));
  }

  private String createPath(String fileName) {
    return String.format("%s%s%s", config.getDirectory(CONFIG_DIR_KEY), File.separator, fileName);
  }

  /**
   * Get the collection.
   * @param type type of the collection to get
   * @param fileName the fileName where the collection is stored
   * @return null if the collection is not found, else the collection
   * @throws StorageException 
   */
  public <T extends FileCollection<? extends SystemEntity>> T getCollection(Class<T> type, String fileName) throws StorageException {
    try {
      return objectMapper.readValue(getFile(fileName), type);
    } catch (JsonParseException e) {
      throw new StorageException(e);
    } catch (JsonMappingException e) {
      throw new StorageException(e);
    } catch (FileNotFoundException e) {
      try {
        return type.newInstance();
      } catch (InstantiationException e1) {
        throw new StorageException(e1);
      } catch (IllegalAccessException e1) {
        throw new StorageException(e1);
      }
    } catch (IOException e) {
      throw new StorageException(e);
    }
  }
}
