package nl.knaw.huygens.repository.server.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
  public void testCreateAnnotatedMethod() {
    AbstractRolesAllowedResourceFilterFactory instance = createInstance(true);
    RolesAllowed rolesAllowed = generateRolesAllowed();
    AbstractMethod method = generateAbstractMethod(rolesAllowed);

    List<ResourceFilter> filters = instance.create(method);

    checkList(1, TestAbstractRolesAllowedResourceFilterFactorySubclass.SecurityFilter.class, filters);

  }

  @Test
  public void testCreateAnnotatedResource() {
    AbstractRolesAllowedResourceFilterFactory instance = createInstance(true);
    RolesAllowed rolesAllowed = generateRolesAllowed();
    AbstractResource resource = mock(AbstractResource.class);
    when(resource.getAnnotation(RolesAllowed.class)).thenReturn(rolesAllowed);

    AbstractMethod method = generateAbstractMethod(null);
    when(method.getResource()).thenReturn(resource);

    List<ResourceFilter> filters = instance.create(method);

    checkList(1, TestAbstractRolesAllowedResourceFilterFactorySubclass.SecurityFilter.class, filters);

  }

  @Test
  public void testCreateNoSecurity() {
    AbstractRolesAllowedResourceFilterFactory instance = createInstance(false);
    RolesAllowed rolesAllowed = generateRolesAllowed();
    AbstractMethod method = generateAbstractMethod(rolesAllowed);

    List<ResourceFilter> filters = instance.create(method);

    checkList(1, TestAbstractRolesAllowedResourceFilterFactorySubclass.NoSecurityFilter.class, filters);
  }

  @Test
  public void testCreateNoRolesAllowedAnnotation() {
    AbstractRolesAllowedResourceFilterFactory instance = createInstance(true);

    AbstractResource resource = mock(AbstractResource.class);
    when(resource.getAnnotation(RolesAllowed.class)).thenReturn(null);

    AbstractMethod method = generateAbstractMethod(null);
    when(method.getResource()).thenReturn(resource);

    List<ResourceFilter> filters = instance.create(method);

    assertNull(filters);

  }

  private AbstractMethod generateAbstractMethod(RolesAllowed rolesAllowed) {
    AbstractMethod method = mock(AbstractMethod.class);
    when(method.getAnnotation(RolesAllowed.class)).thenReturn(rolesAllowed);
    return method;
  }

  private RolesAllowed generateRolesAllowed() {
    RolesAllowed rolesAllowed = mock(RolesAllowed.class);
    doReturn(RolesAllowed.class).when(rolesAllowed).annotationType();
    return rolesAllowed;
  }

  private void checkList(int size, Class<?> expectedType, List<ResourceFilter> actualList) {
    assertEquals(size, actualList.size());
    assertEquals(expectedType, actualList.get(0).getClass());
  }
}
