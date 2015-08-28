package nl.knaw.huygens.timbuctoo.util;

/*
 * #%L
 * Timbuctoo core
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

import nl.knaw.huygens.timbuctoo.model.Relation;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * A class that determines which RelationRefCreator should be created for the type.
 */
@Singleton
public class RelationRefAdderFactory {

  private static final Class<RelationRefCreator> DEFAULT_RELATION_REF_CREATOR_TYPE = RelationRefCreator.class;
  private static final Class<RefCreatorAnnotation> REF_CREATOR_ANNOTATION_TYPE = RefCreatorAnnotation.class;
  private Injector injector;

  @Inject
  public RelationRefAdderFactory(Injector injector) {
    this.injector = injector;
  }

  public RelationRefAdder create(Class<? extends Relation> type) {
    return new RelationRefAdder(getRefCreator(type));
  }

  private RelationRefCreator getRefCreator(Class<? extends Relation> type) {
    if (type.isAnnotationPresent(REF_CREATOR_ANNOTATION_TYPE)) {
      Class<? extends RelationRefCreator> refCreatorType = type.getAnnotation(REF_CREATOR_ANNOTATION_TYPE).value();

      return injector.getInstance(refCreatorType);
    }

    return injector.getInstance(DEFAULT_RELATION_REF_CREATOR_TYPE);
  }
}
