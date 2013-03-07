package nl.knaw.huygens.repository.index;

import java.lang.reflect.Method;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import nl.knaw.huygens.repository.indexdata.IndexAnnotation;
import nl.knaw.huygens.repository.indexdata.IndexAnnotations;
import nl.knaw.huygens.repository.model.Document;

public class ModelIteratorTest {
  private ModelIterator instance;
  private AnnotatedMethodProcessorMock annotatedMethodProcessor;

  @Before
  public void setUp() {
    instance = new ModelIterator();
    annotatedMethodProcessor = new AnnotatedMethodProcessorMock();
  }

  @After
  public void tearDown() {
    instance = null;
  }

  @Test
  public void testProcesClassExplicitlyAnnoted() {
    instance.processClass(annotatedMethodProcessor, ExplicitlyAnnotatedModel.class);

    int actual = annotatedMethodProcessor.getNumberOfIndexAnnotations();

    Assert.assertEquals(3, actual);
  }

  @Test
  public void testProcesClassExplicitlyAnnotedWithIndexAnnotations() {
    instance.processClass(annotatedMethodProcessor, ExplicitlyAnnotatedModelWithIndexAnnotations.class);

    int actual = annotatedMethodProcessor.getNumberOfIndexAnnotations();

    Assert.assertEquals(4, actual);
  }

  @Test
  public void testProcesClassImplicitlyAnnoted() {
    instance.processClass(annotatedMethodProcessor, ImplicitlyAnnotatedModel.class);

    int actual = annotatedMethodProcessor.getNumberOfIndexAnnotations();

    Assert.assertEquals(3, actual);
  }

  @Test
  public void testProcesClassInheritedImplicitlyAnnotedWithIndexAnnotations() {
    instance.processClass(annotatedMethodProcessor, ImplicitlyAnnotatedModelWithIndexAnnotations.class);

    int actual = annotatedMethodProcessor.getNumberOfIndexAnnotations();

    Assert.assertEquals(4, actual);
  }

  @Test
  public void testProcesClassInheritedMethod() {
    instance.processClass(annotatedMethodProcessor, SubModel.class);

    int actual = annotatedMethodProcessor.getNumberOfIndexAnnotations();

    Assert.assertEquals(3, actual);
  }

  @Test
  public void testProcesClassInheritedMethodWithIndexAnnotations() {
    instance.processClass(annotatedMethodProcessor, SubModelWithIndexAnnotations.class);

    int actual = annotatedMethodProcessor.getNumberOfIndexAnnotations();

    Assert.assertEquals(4, actual);
  }

  @Test
  public void testProcessClassModelWithOverriddenIndexAnnotation() throws NoSuchMethodException, SecurityException {
    Class<? extends Document> cls = ModelWithOverriddenIndexAnnotation.class;
    
    instance.processClass(annotatedMethodProcessor, cls);
    
    Method method = cls.getMethod("getDescription", (Class[]) null);
    
    IndexAnnotation expectedAnnotation = (IndexAnnotation) method.getAnnotation(IndexAnnotation.class);
    
    IndexAnnotation actualAnnotation = this.annotatedMethodProcessor.getIndexAnnotationsForMethod(method).get(0);
    
    Assert.assertEquals(expectedAnnotation.fieldName(), actualAnnotation.fieldName());    
  }
  
  @Test
  public void testProcessClassModelWithOverriddenIndexAnnotations() throws NoSuchMethodException, SecurityException {
    Class<ModelWithOverriddenIndexAnnotations> cls = ModelWithOverriddenIndexAnnotations.class;
    
    instance.processClass(annotatedMethodProcessor, cls);
    
    Method method = cls.getMethod("getString", (Class[]) null);
    
    int expectNumberOfAnnotations = method.getAnnotation(IndexAnnotations.class).value().length;
    
    int actualNumberOfAnnotations = annotatedMethodProcessor.getIndexAnnotationsForMethod(method).size();
    
    Assert.assertEquals(expectNumberOfAnnotations, actualNumberOfAnnotations);    
  }

}
