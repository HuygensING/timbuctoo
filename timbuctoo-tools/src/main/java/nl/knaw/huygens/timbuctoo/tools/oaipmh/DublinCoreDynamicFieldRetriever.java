package nl.knaw.huygens.timbuctoo.tools.oaipmh;

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
