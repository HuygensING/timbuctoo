package nl.knaw.huygens.timbuctoo.remote.rs.download;

import nl.knaw.huygens.timbuctoo.remote.rs.download.ResourceSyncFileLoader.RemoteFilesList;
import nl.knaw.huygens.timbuctoo.remote.rs.exceptions.CantDetermineDataSetException;
import nl.knaw.huygens.timbuctoo.v5.dataset.ImportManager;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ResourceSyncImportTest {

  private String baseUrl = "http://127.0.0.1:8080/v5/resourcesync/u33707283d426f900d4d33707283d426f900d4d0d/clusius/";

  private Metadata metadata = new Metadata();


  @Test
  public void filterReturnsListWithChangeFilesIfChangesPresent() throws Exception {
    final ResourceSyncFileLoader resourceSyncFileLoader = mock(ResourceSyncFileLoader.class);

    List<RemoteFile> changes = new ArrayList<>();

    changes.add(RemoteFile.create(baseUrl + "files/changes1.nqud", () -> null, "", metadata));

    changes.add(RemoteFile.create(baseUrl + "files/changes2.nqud", () -> null, "", metadata));

    List<RemoteFile> resources = new ArrayList<>();

    resources.add(RemoteFile.create(baseUrl + "files/dataset.nq", () -> null, "", metadata));

    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);

    String capabilityListUri = baseUrl + "capabilitylist.xml";

    given(resourceSyncFileLoader.getRemoteFilesList(capabilityListUri)).willReturn(remoteFilesList);

    ResourceSyncImport resourceSyncImport = new ResourceSyncImport(resourceSyncFileLoader, null, true);

    List<RemoteFile> filteredFiles = resourceSyncImport.filter(capabilityListUri);

    assertThat(filteredFiles, containsInAnyOrder(
      hasProperty("url", is(baseUrl + "files/changes1.nqud")),
      hasProperty("url", is(baseUrl + "files/changes2.nqud"))
    ));
  }

  @Test
  public void filterAndImportImportsAllChanges() throws Exception {
    final ResourceSyncFileLoader resourceSyncFileLoader = mock(ResourceSyncFileLoader.class);

    List<RemoteFile> changes = new ArrayList<>();

    changes.add(RemoteFile.create(baseUrl + "files/changes1.nqud", () -> null, "", metadata));

    changes.add(RemoteFile.create(baseUrl + "files/changes2.nqud", () -> null, "", metadata));

    List<RemoteFile> resources = new ArrayList<>();

    resources.add(RemoteFile.create(baseUrl + "files/dataset.nq", () -> null, "", metadata));

    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);

    String capabilityListUri = baseUrl + "capabilitylist.xml";

    given(resourceSyncFileLoader.getRemoteFilesList(capabilityListUri)).willReturn(remoteFilesList);

    ImportManager importManager = mock(ImportManager.class);


    given(importManager.isRdfTypeSupported(any(MediaType.class))).willReturn(true);

    DataSet dataSet = mock(DataSet.class);

    DataSetMetaData dataSetMetaData = mock(DataSetMetaData.class);

    given(dataSetMetaData.getBaseUri()).willReturn(baseUrl);

    given(dataSet.getMetadata()).willReturn(dataSetMetaData);

    given(dataSet.getImportManager()).willReturn(importManager);

    ResourceSyncImport resourceSyncImport = new ResourceSyncImport(resourceSyncFileLoader, dataSet, true);


    ResourceSyncImport.ResourceSyncReport resourceSyncReport = resourceSyncImport.filterAndImport(capabilityListUri,
      null);

    assertThat(resourceSyncReport.importedFiles, containsInAnyOrder(is(baseUrl + "files/changes1.nqud"),
      is(baseUrl + "files/changes2.nqud")
    ));
  }

  @Test
  public void filterReturnsListWithSingleDataSetFileIfChangesNotPresentAndSingleResource() throws Exception {
    ResourceSyncFileLoader resourceSyncFileLoader = mock(ResourceSyncFileLoader.class);

    String capabilityListUri = baseUrl + "capabilitylist.xml";

    List<RemoteFile> changes = Collections.emptyList();

    List<RemoteFile> resources = new ArrayList<>();

    resources.add(RemoteFile.create(baseUrl + "files/dataset.nq", () -> null, "", metadata));

    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);

    given(resourceSyncFileLoader.getRemoteFilesList(capabilityListUri)).willReturn(remoteFilesList);

    ResourceSyncImport resourceSyncImport = new ResourceSyncImport(resourceSyncFileLoader, null, true);

    List<RemoteFile> filteredFiles = resourceSyncImport.filter(capabilityListUri);

    assertThat(filteredFiles, contains(
      hasProperty("url", is(baseUrl + "files/dataset.nq"))
    ));

  }

  @Test
  public void filterReturnsListWithSingleDataSetFileIfChangesNotPresentAndDataSetPropertySet() throws Exception {
    final ResourceSyncFileLoader resourceSyncFileLoader = mock(ResourceSyncFileLoader.class);

    List<RemoteFile> resources = new ArrayList<>();

    resources.add(RemoteFile.create(baseUrl + "files/dataset.nq", () -> null, "", metadata));

    Metadata metadata2 = new Metadata();

    metadata2.setIsDataset(true);

    resources.add(RemoteFile.create(baseUrl + "files/dataset.rdf", () -> null, "", metadata2));

    List<RemoteFile> changes = Collections.emptyList();

    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);

    String capabilityListUri = baseUrl + "capabilitylist.xml";

    given(resourceSyncFileLoader.getRemoteFilesList(capabilityListUri)).willReturn(remoteFilesList);

    ResourceSyncImport resourceSyncImport = new ResourceSyncImport(resourceSyncFileLoader, null, true);

    List<RemoteFile> filteredFiles = resourceSyncImport.filter(capabilityListUri);

    assertThat(filteredFiles, contains(
      hasProperty("url", is(baseUrl + "files/dataset.rdf"))
    ));
  }

  @Test
  public void filterReturnsListWithUserSpecifiedDatasetFileIfPresent() throws Exception {
    ResourceSyncFileLoader resourceSyncFileLoader = mock(ResourceSyncFileLoader.class);

    String capabilityListUri = baseUrl + "capabilitylist.xml";

    List<RemoteFile> changes = Collections.emptyList();

    List<RemoteFile> resources = new ArrayList<>();

    resources.add(RemoteFile.create(baseUrl + "files/dataset.nq", () -> null, "", metadata));


    resources.add(RemoteFile.create(baseUrl + "files/dataset.rdf", () -> null, "", metadata));

    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);

    given(resourceSyncFileLoader.getRemoteFilesList(capabilityListUri)).willReturn(remoteFilesList);

    ResourceSyncImport resourceSyncImport = new ResourceSyncImport(resourceSyncFileLoader, null, true);

    String userSpecifiedDataSet = baseUrl + "files/dataset.rdf";

    List<RemoteFile> filteredFiles = resourceSyncImport.filter(capabilityListUri, userSpecifiedDataSet);

    assertThat(filteredFiles, contains(
      hasProperty("url", is(baseUrl + "files/dataset.rdf"))
    ));
  }

  @Test(expected = CantDetermineDataSetException.class)
  public void filterReturnsMessageWithFilesListIfMultipleFilesAndNoneSpecifiedAsDataset() throws Exception {
    ResourceSyncFileLoader resourceSyncFileLoader = mock(ResourceSyncFileLoader.class);

    String capabilityListUri = baseUrl + "capabilitylist.xml";

    List<RemoteFile> changes = Collections.emptyList();

    List<RemoteFile> resources = new ArrayList<>();

    resources.add(RemoteFile.create(baseUrl + "files/dataset.nq", () -> null, "", metadata));


    resources.add(RemoteFile.create(baseUrl + "files/dataset.rdf", () -> null, "", metadata));

    RemoteFilesList remoteFilesList = new RemoteFilesList(changes, resources);

    given(resourceSyncFileLoader.getRemoteFilesList(capabilityListUri)).willReturn(remoteFilesList);

    ResourceSyncImport resourceSyncImport = new ResourceSyncImport(resourceSyncFileLoader, null, true);

    resourceSyncImport.filter(capabilityListUri);

  }
}
