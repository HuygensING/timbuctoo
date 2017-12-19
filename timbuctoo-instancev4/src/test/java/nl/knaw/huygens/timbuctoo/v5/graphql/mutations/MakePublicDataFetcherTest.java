package nl.knaw.huygens.timbuctoo.v5.graphql.mutations;

import com.google.common.collect.Sets;
import graphql.schema.DataFetchingEnvironment;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import nl.knaw.huygens.timbuctoo.v5.dataset.exceptions.DataSetPublishException;
import nl.knaw.huygens.timbuctoo.v5.graphql.datafetchers.dto.ContextData;
import nl.knaw.huygens.timbuctoo.v5.graphql.security.UserPermissionCheck;
import nl.knaw.huygens.timbuctoo.v5.security.dto.Permission;
import nl.knaw.huygens.timbuctoo.v5.security.dto.User;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class MakePublicDataFetcherTest {
  @Test
  public void getReturnsDataSetMetadata() throws Exception, DataSetPublishException {
    User user = User.create("testDisplayName", "testUserId", "testUserId");
    UserPermissionCheck userPermissionCheck = mock(UserPermissionCheck.class);

    ContextData contextData = mock(ContextData.class);
    given(contextData.getUser()).willReturn(Optional.of(user));
    DataSetMetaData dataSetMetaData = mock(DataSetMetaData.class);
    given(dataSetMetaData.isPublished()).willReturn(false);
    given(dataSetMetaData.getDataSetId()).willReturn("testDataSetId");
    given(dataSetMetaData.getOwnerId()).willReturn("testOwnerId");
    given(dataSetMetaData.getCombinedId()).willReturn("testOwnerId__testDataSetId");
    given(userPermissionCheck.getPermissions(dataSetMetaData)).willReturn(Sets.newHashSet(
      Permission.READ,
      Permission.WRITE,
      Permission.ADMIN));

    DataSetRepository dataSetRepository = mock(DataSetRepository.class);
    DataSet dataSet = mock(DataSet.class);
    given(dataSet.getMetadata()).willReturn(dataSetMetaData);
    given(dataSetRepository.getDataSet(any(),any(),any())).willReturn(Optional.of(dataSet));
    DataFetchingEnvironment env = mock(DataFetchingEnvironment.class);
    given(env.getArgument("dataSet")).willReturn("testOwnerId__testDataSetId");
    given(env.getContext()).willReturn(contextData);
    given(contextData.getUserPermissionCheck()).willReturn(userPermissionCheck);
    MakePublicDataFetcher makePublicDataFetcher = new MakePublicDataFetcher(dataSetRepository);

    DataSetMetaData retrievedDataSetMetaData = (DataSetMetaData) makePublicDataFetcher.get(env);
    given(dataSetMetaData.isPublished()).willReturn(true);

    assertEquals(dataSetMetaData, retrievedDataSetMetaData);
    assertEquals(dataSetMetaData.isPublished(), retrievedDataSetMetaData.isPublished());
  }

}
