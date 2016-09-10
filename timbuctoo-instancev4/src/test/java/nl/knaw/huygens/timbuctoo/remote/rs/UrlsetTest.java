package nl.knaw.huygens.timbuctoo.remote.rs;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class UrlsetTest {

  @Test
  public void testSerializeDeserialize() throws JAXBException {

    RsMdBean rsMd = new RsMdBean("description")
      .setAt(ZonedDateTime.now(ZoneOffset.UTC))
      .setCompleted(ZonedDateTime.now(ZoneOffset.UTC))
      .setFrom(ZonedDateTime.now(ZoneOffset.UTC))
      .setUntil(ZonedDateTime.now(ZoneOffset.UTC));

    RsMdBean urlRsMd = new RsMdBean("capabilitylist")
      .setChange("updated")
      .setEncoding("encoding")
      .setHash("hash")
      .setLength(2L)
      .setPath("another/path")
      .setType("text/plain");

    RsLnBean urlLink1 = new RsLnBean("describedby", "http://example.com/info_about_set1_of_resources.xml")
      .setEncoding("UTF-8")
      .setHash("md5:1584abdf8ebdc9802ac0c6a7402c03b6")
      .setLength(1000L)
      .setModified(ZonedDateTime.now(ZoneOffset.UTC))
      .setPath("foo/bar")
      .setPri(1)
      .setType("application/xml");

    UrlItem url1 = new UrlItem("http://example.com/capabilitylist1.xml")
      .setMetadata(urlRsMd)
      .add(urlLink1)
      .add(new RsLnBean("duplicate", "http://also.com/capabilitylist1.xml"));

    UrlItem url2 = new UrlItem("http://example.com/capabilitylist2.xml");

    Urlset urlset = new Urlset(rsMd)
      .add(new RsLnBean("describedby", "http://example.com/info_about_source.xml"))
      .add(new RsLnBean("up", "http://example.com/attic"))
      .add(url1)
      .add(url2);


    JAXBContext jaxbContext = JAXBContext.newInstance(Urlset.class);
    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    StringWriter writer = new StringWriter();
    jaxbMarshaller.marshal(urlset, writer);
    String output1 = writer.toString();

    //System.out.println(output1);

    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    InputStream in = IOUtils.toInputStream(writer.toString());
    Urlset returned = (Urlset) jaxbUnmarshaller.unmarshal(in);
    IOUtils.closeQuietly(in);

    writer = new StringWriter();
    jaxbMarshaller.marshal(returned, writer);
    String output2 = writer.toString();

    //System.out.println(output2);

    assertThat(output2, equalTo(output1));

  }

}
