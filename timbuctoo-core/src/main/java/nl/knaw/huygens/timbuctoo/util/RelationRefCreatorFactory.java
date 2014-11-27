package nl.knaw.huygens.timbuctoo.util;

import nl.knaw.huygens.timbuctoo.model.Relation;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * A class that determines which RelationRefCreator should be created for the type.
 */
@Singleton
public class RelationRefCreatorFactory {

  private static final Class<RelationRefCreator> DEFAULT_RELATION_REF_CREATOR_TYPE = RelationRefCreator.class;
  private static final Class<RefCreatorAnnotation> REF_CREATOR_ANNOTATION_TYPE = RefCreatorAnnotation.class;
  private Injector injector;

  @Inject
  public RelationRefCreatorFactory(Injector injector) {
    this.injector = injector;
  }

  public RelationRefCreator create(Class<? extends Relation> type) {
    if (type.isAnnotationPresent(REF_CREATOR_ANNOTATION_TYPE)) {
      Class<? extends RelationRefCreator> refCreatorType = type.getAnnotation(REF_CREATOR_ANNOTATION_TYPE).value();

      return injector.getInstance(refCreatorType);
    }

    return injector.getInstance(DEFAULT_RELATION_REF_CREATOR_TYPE);
  }
}
