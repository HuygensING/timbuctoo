package nl.knaw.huygens.timbuctoo.v5.graphql.security;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.BasicDataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.security.PermissionFetcher;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class UserPermissionCheckTest {
  @Test
  public void getPermissionsReturnsNoPermissionsForEmptyUserIfPrivateDataSet() {
    PermissionFetcher permissionFetcher = mock(PermissionFetcher.class);
    Set<Permission> defaultPermissions = Sets.newHashSet(Permission.READ);
    UserPermissionCheck userPermissionCheck = new UserPermissionCheck(Optional.empty(),
      permissionFetcher, defaultPermissions);
    DataSetMetaData dataSetMetaData = mock(BasicDataSetMetaData.class);
    given(dataSetMetaData.isPublic()).willReturn(false);

    Set<Permission> permissions = userPermissionCheck.getPermissions(dataSetMetaData);

    assertEquals(Sets.newHashSet(), permissions);
  }

  @Test
  public void getPermissionsReturnsNoPermissionsForEmptyUserIfPublicDataSet() {
    PermissionFetcher permissionFetcher = mock(PermissionFetcher.class);
    Set<Permission> defaultPermissions = Sets.newHashSet(Permission.READ);
    UserPermissionCheck userPermissionCheck = new UserPermissionCheck(Optional.empty(),
      permissionFetcher, defaultPermissions);
    DataSetMetaData dataSetMetaData = mock(BasicDataSetMetaData.class);
    given(dataSetMetaData.isPublic()).willReturn(true);

    Set<Permission> permissions = userPermissionCheck.getPermissions(dataSetMetaData);

    assertEquals(Sets.newHashSet(Permission.READ), permissions);
  }

}
