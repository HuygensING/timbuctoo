package nl.knaw.huygens.timbuctoo.index;

/*
 * #%L
 * Timbuctoo search
 * =======
 * Copyright (C) 2012 - 2013 Huygens ING
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

import java.lang.reflect.Method;

import junit.framework.Assert;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotation;
import nl.knaw.huygens.timbuctoo.facet.IndexAnnotations;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.ExplicitlyAnnotatedModelWithIndexAnnotations;
import nl.knaw.huygens.timbuctoo.index.model.ImplicitlyAnnotatedModel;
import nl.knaw.huygens.timbuctoo.index.model.ImplicitlyAnnotatedModelWithIndexAnnotations;
import nl.knaw.huygens.timbuctoo.index.model.ModelWithOverriddenIndexAnnotation;
import nl.knaw.huygens.timbuctoo.index.model.ModelWithOverriddenIndexAnnotations;
import nl.knaw.huygens.timbuctoo.index.model.SubModel;
import nl.knaw.huygens.timbuctoo.index.model.SubModelWithIndexAnnotations;
import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Test;

public class ModelIteratorTest {

  private AnnotatedMethodProcessorMock processor;
  private ModelIterator instance;

  @Before
  public void setUp() {
    processor = new AnnotatedMethodProcessorMock();
    instance = new ModelIterator();
  }

  private void assertNumberOfIndexAnnotations(int expected, Class<? extends Entity> type) {
    instance.processClass(processor, type);
    Assert.assertEquals(expected, processor.getNumberOfIndexAnnotations());
  }

  @Test
  public void testProcesClassExplicitlyAnnoted() {
    assertNumberOfIndexAnnotations(3, ExplicitlyAnnotatedModel.class);
  }

  @Test
  public void testProcesClassExplicitlyAnnotedWithIndexAnnotations() {
    assertNumberOfIndexAnnotations(4, ExplicitlyAnnotatedModelWithIndexAnnotations.class);
  }

  @Test
  public void testProcesClassImplicitlyAnnoted() {
    assertNumberOfIndexAnnotations(3, ImplicitlyAnnotatedModel.class);
  }

  @Test
  public void testProcesClassInheritedImplicitlyAnnotedWithIndexAnnotations() {
    assertNumberOfIndexAnnotations(4, ImplicitlyAnnotatedModelWithIndexAnnotations.class);
  }

  @Test
  public void testProcesClassInheritedMethod() {
    assertNumberOfIndexAnnotations(3, SubModel.class);
  }

  @Test
  public void testProcesClassInheritedMethodWithIndexAnnotations() {
    assertNumberOfIndexAnnotations(4, SubModelWithIndexAnnotations.class);
  }

  @Test
  public void testProcessClassModelWithOverriddenIndexAnnotation() throws NoSuchMethodException, SecurityException {
    Class<? extends Entity> cls = ModelWithOverriddenIndexAnnotation.class;
    instance.processClass(processor, cls);
    Method method = cls.getMethod("getDisplayName", (Class[]) null);
    IndexAnnotation expectedAnnotation = method.getAnnotation(IndexAnnotation.class);
    IndexAnnotation actualAnnotation = this.processor.getIndexAnnotationsForMethod(method).get(0);
    Assert.assertEquals(expectedAnnotation.fieldName(), actualAnnotation.fieldName());
  }

  @Test
  public void testProcessClassModelWithOverriddenIndexAnnotations() throws NoSuchMethodException, SecurityException {
    Class<ModelWithOverriddenIndexAnnotations> cls = ModelWithOverriddenIndexAnnotations.class;
    instance.processClass(processor, cls);
    Method method = cls.getMethod("getString", (Class[]) null);
    int expectNumberOfAnnotations = method.getAnnotation(IndexAnnotations.class).value().length;
    int actualNumberOfAnnotations = processor.getIndexAnnotationsForMethod(method).size();
    Assert.assertEquals(expectNumberOfAnnotations, actualNumberOfAnnotations);
  }

}
