package nl.knaw.huygens.repository.server.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.junit.Test;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.spi.container.ResourceFilter;

public class AbstractRolesAllowedResourceFilterFactoryTest {

  private AbstractRolesAllowedResourceFilterFactory createInstance(boolean securityEnabled) {
    return new TestAbstractRolesAllowedResourceFilterFactorySubclass(securityEnabled);
  }

  @Test
  public void testCreateRolesAllowedMethod() {
    AbstractRolesAllowedResourceFilterFactory instance = createInstance(true);
    RolesAllowed rolesAllowed = generateAnnotation(RolesAllowed.class);
    AbstractMethod method = generateAbstractMethod(RolesAllowed.class, rolesAllowed);
    when(method.getResource()).thenReturn(mock(AbstractResource.class));

    List<ResourceFilter> filters = instance.create(method);

    checkList(1, TestAbstractRolesAllowedResourceFilterFactorySubclass.SecurityFilter.class, filters);

  }

  @Test
  public void testCreateRolesAllowedResource() {
    AbstractRolesAllowedResourceFilterFactory instance = createInstance(true);
    RolesAllowed rolesAllowed = generateAnnotation(RolesAllowed.class);
    AbstractResource resource = generateAbstractResource(RolesAllowed.class, rolesAllowed);
    AbstractMethod method = generateAbstractMethod(RolesAllowed.class, null);
    when(method.getResource()).thenReturn(resource);

    List<ResourceFilter> filters = instance.create(method);

    checkList(1, TestAbstractRolesAllowedResourceFilterFactorySubclass.SecurityFilter.class, filters);

  }

  @Test
  public void testCreateNoSecurity() {
    AbstractRolesAllowedResourceFilterFactory instance = createInstance(false);
    RolesAllowed rolesAllowed = generateAnnotation(RolesAllowed.class);
    AbstractMethod method = generateAbstractMethod(RolesAllowed.class, rolesAllowed);
    when(method.getResource()).thenReturn(mock(AbstractResource.class));

    List<ResourceFilter> filters = instance.create(method);

    checkList(1, TestAbstractRolesAllowedResourceFilterFactorySubclass.NoSecurityFilter.class, filters);
  }

  @Test
  public void testCreateNoRolesAllowedAnnotation() {
    AbstractRolesAllowedResourceFilterFactory instance = createInstance(true);
    AbstractResource resource = mock(AbstractResource.class);
    when(resource.getAnnotation(RolesAllowed.class)).thenReturn(null);
    AbstractMethod method = generateAbstractMethod(RolesAllowed.class, null);
    when(method.getResource()).thenReturn(resource);

    List<ResourceFilter> filters = instance.create(method);

    assertNull(filters);

  }

  private <T extends Annotation> AbstractResource generateAbstractResource(Class<T> type, T rolesAllowed) {
    AbstractResource resource = mock(AbstractResource.class);
    when(resource.getAnnotation(type)).thenReturn(rolesAllowed);
    return resource;
  }

  private <T extends Annotation> AbstractMethod generateAbstractMethod(Class<T> type, T annotation) {
    AbstractMethod method = mock(AbstractMethod.class);
    when(method.getAnnotation(type)).thenReturn(annotation);
    return method;
  }

  private <T extends Annotation> T generateAnnotation(Class<T> type) {
    T annotation = mock(type);
    doReturn(type).when(annotation).annotationType();
    return annotation;
  }

  private void checkList(int size, Class<?> expectedType, List<ResourceFilter> actualList) {
    assertEquals(size, actualList.size());
    assertEquals(expectedType, actualList.get(0).getClass());
  }
}
