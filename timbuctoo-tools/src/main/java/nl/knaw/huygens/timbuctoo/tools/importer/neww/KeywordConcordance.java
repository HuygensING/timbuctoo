package nl.knaw.huygens.timbuctoo.tools.importer.neww;

/*
 * #%L
 * Timbuctoo tools
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import com.google.common.collect.Maps;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.Keyword;
import nl.knaw.huygens.timbuctoo.model.Reference;
import nl.knaw.huygens.timbuctoo.model.neww.WWKeyword;
import nl.knaw.huygens.timbuctoo.model.util.Change;
import nl.knaw.huygens.timbuctoo.storage.ValidationException;
import nl.knaw.huygens.timbuctoo.tools.importer.CSVImporter;

/**
 * Imports keywords and make references available.
 */
public class KeywordConcordance extends CSVImporter {

  private final Repository repository;
  private final Change change;

  private final Map<String, Reference> map = Maps.newHashMap();

  public KeywordConcordance(Repository repository, Change change) {
    super(new PrintWriter(System.err));
    this.repository = repository;
    this.change = change;
  }

  @Override
  protected void handleLine(String[] items) throws IOException, ValidationException {
    if (items.length < 3) {
      throw new ValidationException("Lines must have at least 3 items");
    }
    WWKeyword keyword = new WWKeyword();
    keyword.setType(items[0]);
    keyword.setValue(items[1]);
    String storedId = repository.addDomainEntity(WWKeyword.class, keyword, change);
    Reference reference = new Reference(Keyword.class, storedId);
    for (int index = 2; index < items.length; index++) {
      String key = items[index];
      if (map.containsKey(key)) {
        throw new ValidationException("Duplicate key " + key);
      }
      map.put(key, reference);
    }
  }

  @Override
  protected void handleEndOfFile() {
    System.out.printf("Keyword concordance size : %d%n", map.size());
  };

  public Reference lookup(String key) {
    return map.get(key);
  }

}
