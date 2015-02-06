package nl.knaw.huygens.timbuctoo.tools.oaipmh;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import nl.knaw.huygens.oaipmh.metadata.DublinCoreRecord;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.oaipmh.DublinCoreMetadataField;
import nl.knaw.huygens.timbuctoo.oaipmh.OAIDublinCoreField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class DublinCoreDynamicFieldRetriever {

  private static final Logger LOG = LoggerFactory.getLogger(DublinCoreDynamicFieldRetriever.class);
  private static final Class<OAIDublinCoreField> DUBLIN_CORE_FIELD_TYPE = OAIDublinCoreField.class;
  private final DublinCoreDynamicFieldFiller dynamicFieldFiller;

  @Inject
  public DublinCoreDynamicFieldRetriever(DublinCoreDynamicFieldFiller dynamicFieldFiller) {
    this.dynamicFieldFiller = dynamicFieldFiller;
  }

  public void retrieve(DublinCoreRecord dublinCoreRecord, DomainEntity domainEntity) {
    for (Method method : domainEntity.getClass().getMethods()) {
      if (isGetter(method) && method.isAnnotationPresent(DUBLIN_CORE_FIELD_TYPE)) {
        DublinCoreMetadataField field = method.getAnnotation(DUBLIN_CORE_FIELD_TYPE).dublinCoreField();
        try {
          Object value = method.invoke(domainEntity);
          dynamicFieldFiller.addTo(dublinCoreRecord, field, value);
        } catch (IllegalAccessException e) {
          LOG.error("Could not access \"{}\".", method);
          LOG.error("Exception was thrown", e);
        } catch (IllegalArgumentException e) {
          LOG.error("Could not access \"{}\".", method);
          LOG.error("Exception was thrown", e);
        } catch (InvocationTargetException e) {
          LOG.error("Could not access \"{}\".", method);
          LOG.error("Exception was thrown", e);
        }
      }
    }
  }

  private boolean isGetter(Method method) {
    return method.getReturnType() != Void.TYPE && method.getParameterTypes().length == 0;
  }
}
