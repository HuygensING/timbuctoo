package nl.knaw.huygens.timbuctoo.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import nl.knaw.huygens.timbuctoo.config.TypeRegistry;
import nl.knaw.huygens.timbuctoo.model.Relation;
import nl.knaw.huygens.timbuctoo.storage.Storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A class that determines which RelationRefCreator should be created for the type.
 */
@Singleton
public class RelationRefCreatorFactory {

  private static final Class<RefCreatorAnnotation> REF_CREATOR_ANNOTATION_TYPE = RefCreatorAnnotation.class;
  private final TypeRegistry typeRegistry;
  private final Storage storage;
  private final RelationRefCreator defaultRefCreator;

  @Inject
  public RelationRefCreatorFactory(TypeRegistry typeRegistry, Storage storage) {
    this.typeRegistry = typeRegistry;
    this.storage = storage;
    defaultRefCreator = new RelationRefCreator(typeRegistry, storage);
  }

  public RelationRefCreator create(Class<? extends Relation> type) {
    if (type.isAnnotationPresent(REF_CREATOR_ANNOTATION_TYPE)) {
      Class<? extends RelationRefCreator> refCreatorType = type.getAnnotation(REF_CREATOR_ANNOTATION_TYPE).value();

      try {
        return createNewInstance(refCreatorType);
      } catch (NoSuchMethodException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (SecurityException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IllegalArgumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return defaultRefCreator;
  }

  private <T extends RelationRefCreator> T createNewInstance(Class<T> refCreatorType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {

    Constructor<T> constructor = refCreatorType.getConstructor(TypeRegistry.class, Storage.class);

    return constructor.newInstance(typeRegistry, storage);
  }
}
