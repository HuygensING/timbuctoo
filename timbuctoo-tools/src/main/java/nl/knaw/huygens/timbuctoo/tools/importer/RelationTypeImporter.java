package nl.knaw.huygens.timbuctoo.tools.importer;

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

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;

import com.google.common.collect.Sets;

import nl.knaw.huygens.timbuctoo.Repository;
import nl.knaw.huygens.timbuctoo.model.RelationType;

/**
 * Imports all relation types.
 * Relies on the storage manager for validation.
 */
public class RelationTypeImporter extends CSVImporter {

  /** File with {@code RelationType} definitions; must be present on classpath. */
  public static final String RELATION_TYPE_DEFS = "relationtype-defs.txt";

  private final Repository repository;

  private Set<String> names;

  public RelationTypeImporter(Repository repository) {
    super(new PrintWriter(System.err), ';', '"', 5);
    this.repository = repository;
    names = Sets.newTreeSet();
  }

  public RelationTypeImporter() {
    this(null);
  }

  public Set<String> getNames() {
    return names;
  }

  /**
   * Reads {@code RelationType} definitions from the specified file which must
   * be present on the classpath.
   */
  public void call(String fileName) throws Exception {
    InputStream stream = Repository.class.getClassLoader().getResourceAsStream(fileName);
    handleFile(stream, 6, false);
  }

  @Override
  protected void handleLine(String[] items) throws Exception {
    RelationType entity = new RelationType();
    entity.setRegularName(items[0]);
    entity.setInverseName(items[1]);
    entity.setSourceTypeName(items[2].toLowerCase());
    entity.setTargetTypeName(items[3].toLowerCase());
    entity.setReflexive(Boolean.parseBoolean(items[4]));
    entity.setSymmetric(Boolean.parseBoolean(items[5]));
    entity.setDerived(Boolean.parseBoolean(items[6]));

    String name = entity.getRegularName();
    if (repository != null && repository.getRelationTypeByName(name) == null) {
      repository.addSystemEntity(RelationType.class, entity);
    }
    names.add(name);
  }

}
