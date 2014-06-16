package nl.knaw.huygens.solr;

import static nl.knaw.huygens.solr.AbstractSolrServerBuilderProvider.COMMIT_TIME;
import static nl.knaw.huygens.solr.AbstractSolrServerBuilderProvider.SERVER_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import nl.knaw.huygens.solr.AbstractSolrServerBuilder.SolrServerType;
import nl.knaw.huygens.timbuctoo.config.Configuration;

import org.apache.solr.core.CoreDescriptor;
import org.junit.Before;
import org.junit.Test;

public class AbstractSolrServerBuilderProviderTest {

  private AbstractSolrServerBuilderProvider instance;
  private final AbstractSolrServerBuilder abstractSolrServerBuilderMock = mock(AbstractSolrServerBuilder.class);
  private Configuration configurationMock;
  private File configFile;

  @Before
  public void setUp() {
    configurationMock = mock(Configuration.class);
    configFile = new File("");
    when(configurationMock.getIntSetting(COMMIT_TIME)).thenReturn(Integer.MAX_VALUE);

    instance = new AbstractSolrServerBuilderProvider(configurationMock) {
      @Override
      protected AbstractSolrServerBuilder createAbstractSolrServer(SolrServerType serverType, int commitTimeInSeconds) {
        return abstractSolrServerBuilderMock;
      }

      @Override
      protected File getSolrConfigFile(String solrDir) {
        return configFile;
      }

    };
  }

  @Test
  public void testGetLocalSolrServer() {
    // setup
    String solrDir = "directory/to/solr";

    when(abstractSolrServerBuilderMock.setSolrDir(solrDir)).thenReturn(abstractSolrServerBuilderMock);
    when(abstractSolrServerBuilderMock.setConfigFile(configFile)).thenReturn(abstractSolrServerBuilderMock);
    when(abstractSolrServerBuilderMock.addProperty(CoreDescriptor.CORE_LOADONSTARTUP, true)).thenReturn(abstractSolrServerBuilderMock);

    when(configurationMock.getSetting(SERVER_TYPE)).thenReturn("LOCAL");
    when(configurationMock.getSolrHomeDir()).thenReturn(solrDir);

    // action
    AbstractSolrServerBuilder builder = instance.get();

    // verify
    verify(configurationMock).getSetting(SERVER_TYPE);
    verify(configurationMock).getIntSetting(COMMIT_TIME);
    verify(configurationMock).getSolrHomeDir();
    verify(abstractSolrServerBuilderMock).setSolrDir(solrDir);
    verify(abstractSolrServerBuilderMock).setConfigFile(configFile);
    verify(abstractSolrServerBuilderMock).addProperty(CoreDescriptor.CORE_LOADONSTARTUP, true);

    assertThat(builder, equalTo(abstractSolrServerBuilderMock));
  }

  @Test
  public void testGetRemoteSolrServer() {
    String baseUrl = "http://test.com/solr";

    when(abstractSolrServerBuilderMock.setSolrUrl(baseUrl)).thenReturn(abstractSolrServerBuilderMock);

    when(configurationMock.getSetting(SERVER_TYPE)).thenReturn("REMOTE");
    when(configurationMock.getSetting(AbstractSolrServerBuilderProvider.SOLR_URL)).thenReturn(baseUrl);

    // action
    AbstractSolrServerBuilder builder = instance.get();

    // verify
    verify(configurationMock).getSetting(SERVER_TYPE);
    verify(configurationMock).getIntSetting(COMMIT_TIME);
    verify(configurationMock).getSetting(AbstractSolrServerBuilderProvider.SOLR_URL);
    verify(abstractSolrServerBuilderMock).setSolrUrl(baseUrl);

    assertThat(builder, equalTo(abstractSolrServerBuilderMock));

  }
}
