package nl.knaw.huygens.timbuctoo.v5.datastores.rssource;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.Capability;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.ResourceSyncContext;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsBuilder;
import nl.knaw.huygens.timbuctoo.remote.rs.xml.Urlset;
import nl.knaw.huygens.timbuctoo.util.UriHelper;
import nl.knaw.huygens.timbuctoo.v5.dataset.DataSetRepository;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSet;
import nl.knaw.huygens.timbuctoo.v5.dataset.dto.DataSetMetaData;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class RsDocumentBuilderTest {

  private static RsBuilder rsBuilder;

  private DataSetRepository dataSetRepository;
  private DataSet dataSet;
  private RsDocumentBuilder rsDocumentBuilder;

  @BeforeClass
  public static void initialize() throws Exception {
    rsBuilder = new RsBuilder(new ResourceSyncContext());
  }

  @Before
  public void init() throws Exception {
    dataSetRepository = mock(DataSetRepository.class);
    rsDocumentBuilder = new RsDocumentBuilder(dataSetRepository, new UriHelper(URI.create("http://example.com")));

    dataSet = mock(DataSet.class);
    DataSetMetaData dataSetMetaData1 = mock(DataSetMetaData.class);
    DataSetMetaData dataSetMetaData2 = mock(DataSetMetaData.class);
    when(dataSetMetaData1.getOwnerId()).thenReturn("u1");
    when(dataSetMetaData1.getDataSetId()).thenReturn("ds1");
    when(dataSetMetaData2.getOwnerId()).thenReturn("u2");
    when(dataSetMetaData2.getDataSetId()).thenReturn("ds2");
    when(dataSet.getMetadata()).thenReturn(dataSetMetaData1).thenReturn(dataSetMetaData2);
  }

  @Test
  public void emptySourceDescription() throws Exception {
    when(dataSetRepository.getDataSetsWithReadAccess(null)).thenReturn(Collections.emptyList());

    Urlset sourceDescription = rsDocumentBuilder.getSourceDescription(null);
    String xml = rsBuilder.toXml(sourceDescription, true);
    //System.out.println(xml);
    rsBuilder.setXmlString(xml).build();
    Urlset sd = rsBuilder.getUrlset().get();
    assertThat(sd.getCapability().get(), is(Capability.DESCRIPTION));
    assertThat(sd.getItemList().size(), is(0));
  }

  @Test
  public void sourceDescriptionWithCapabilityLists() throws Exception {
    when(dataSetRepository.getDataSetsWithReadAccess(null)).thenReturn(Arrays.asList(dataSet, dataSet));

    Urlset sourceDescription = rsDocumentBuilder.getSourceDescription(null);
    String xml = rsBuilder.toXml(sourceDescription, true);
    //System.out.println(xml);
    rsBuilder.setXmlString(xml).build();
    Urlset sd = rsBuilder.getUrlset().get();
    assertThat(sd.getCapability().get(), is(Capability.DESCRIPTION));
    assertThat(sd.getItemList().size(), is(2));
    assertThat(sd.getItemList().get(0).getLoc(), is("http://example.com/v5/resourcesync/u1/ds1/capabilitylist.xml"));
    assertThat(sd.getItemList().get(1).getLoc(), is("http://example.com/v5/resourcesync/u2/ds2/capabilitylist.xml"));
    assertThat(sd.getItemList().get(0).getMetadata().get().getCapability().get(),
      is(Capability.CAPABILITYLIST.xmlValue));
    assertThat(sd.getItemList().get(1).getLink("describedby").get().getHref(),
      is("http://example.com/v5/resourcesync/u2/ds2/description.xml"));
    assertThat(sd.getItemList().get(1).getLink("describedby").get().getType().get(),
      is("application/rdf+xml"));
  }

  @Test
  public void capabilityList() throws Exception {
    when(dataSetRepository.getDataSet(null, "u1", "ds1")).thenReturn(Optional.of(dataSet));

    Urlset capabilityList = rsDocumentBuilder.getCapabilityList(null, "u1", "ds1").get();
    String xml = rsBuilder.toXml(capabilityList, true);
    //System.out.println(xml);
    rsBuilder.setXmlString(xml).build();
    Urlset cl = rsBuilder.getUrlset().get();
    assertThat(cl.getCapability().get(), is(Capability.CAPABILITYLIST));
    assertThat(cl.getLink("up").get().getHref(),
      is("http://example.com/.well-known/resourcesync"));
    assertThat(cl.getLink("describedby").get().getHref(),
      is("http://example.com/v5/resourcesync/u1/ds1/description.xml"));
    assertThat(cl.getLink("describedby").get().getType().get(),
      is("application/rdf+xml"));
    assertThat(cl.getItemList().size(), is(1));
    assertThat(cl.getItemList().get(0).getLoc(), is("http://example.com/v5/resourcesync/u1/ds1/resourcelist.xml"));
    assertThat(cl.getItemList().get(0).getMetadata().get().getCapability().get(),
      is(Capability.RESOURCELIST.xmlValue));
  }

}
