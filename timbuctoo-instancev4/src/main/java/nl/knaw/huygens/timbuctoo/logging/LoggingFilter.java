package nl.knaw.huygens.timbuctoo.logging;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import org.glassfish.jersey.message.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import java.io.BufferedInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@PreMatching
@Priority(Integer.MIN_VALUE)
public final class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {


  private static final Logger LOG = LoggerFactory.getLogger(LoggingFilter.class);
  private static final String STOPWATCH_PROPERTY = LoggingFilter.class.getName() + "stopwatch";

  private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR =
    (o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey());

  private static final String MDC_ID = "request_id";
  private static final String MDC_OUTPUT_BYTECOUNT = "output_bytecount";
  private static final String MDC_DURATION_MILLISECONDS = "duration_milliseconds";
  private static final String MDC_PRE_LOG = "request_log";
  private static final String MDC_POST_LOG = "response_log";
  private static final String MDC_HTTP_METHOD = "http_method";
  private static final String MDC_HTTP_URI = "http_uri";
  private static final String MDC_HTTP_PATH = "http_path";
  private static final String MDC_HTTP_AUTHORITY = "http_authority";
  private static final String MDC_HTTP_QUERY = "http_query";
  private static final String MDC_REQUEST_HEADERS = "http_request_headers";
  private static final String MDC_REQUEST_ENTITY = "http_request_content";
  private static final String MDC_HTTP_STATUS = "http_status";
  private static final String MDC_RESPONSE_HEADERS = "http_response_headers";
  private static final String MDC_RELEASE_HASH = "git_hash";

  private final int entityLogSize;
  private final String releaseHash;

  public LoggingFilter(final int entityLogSize, String releaseHash) {
    this.entityLogSize = entityLogSize;
    this.releaseHash = releaseHash;
  }

  private String formatHeaders(final MultivaluedMap<String, String> headers) {
    final StringBuilder builder = new StringBuilder();
    for (final Map.Entry<String, List<String>> headerEntry : getSortedHeaders(headers.entrySet())) {
      final List<?> val = headerEntry.getValue();
      final String header = headerEntry.getKey();

      builder.append(header).append(": ");
      if (val.size() == 1) {
        builder.append(val.get(0));
      } else {
        boolean add = false;
        for (final Object s : val) {
          if (add) {
            builder.append(',');
          }
          add = true;
          builder.append(s);
        }
      }
      builder.append("\n");
    }
    return builder.toString();
  }

  private Set<Map.Entry<String, List<String>>> getSortedHeaders(final Set<Map.Entry<String, List<String>>> headers) {
    final TreeSet<Map.Entry<String, List<String>>> sortedHeaders = new TreeSet<>(COMPARATOR);
    sortedHeaders.addAll(headers);
    return sortedHeaders;
  }

  private InputStream addInboundEntityToMdc(InputStream stream, final Charset charset) throws IOException {
    final StringBuilder builder = new StringBuilder();
    if (!stream.markSupported()) {
      stream = new BufferedInputStream(stream);
    }
    stream.mark(entityLogSize + 1);
    final byte[] entity = new byte[entityLogSize + 1];
    final int entitySize = stream.read(entity);
    builder.append(new String(entity, 0, Math.min(entitySize, entityLogSize), charset));
    if (entitySize > entityLogSize) {
      builder.append(" (capped at ").append(entityLogSize).append(" bytes)");
    }
    MDC.put(MDC_REQUEST_ENTITY, builder.toString());
    stream.reset();
    return stream;
  }

  @Override
  public void filter(final ContainerRequestContext context) throws IOException {
    if (!"1".equals(context.getHeaderString("Is-Healthcheck"))) {
      final UUID id = UUID.randomUUID();
      MDC.put(MDC_ID, id.toString());
      MDC.put(MDC_RELEASE_HASH, releaseHash);

      MDC.put(MDC_PRE_LOG, "true");
      //Log a very minimal message. Mostly to make sure that we notice requests that never log in the response filter
      LOG.info(">     " + context.getMethod() + " " + context.getUriInfo().getRequestUri().toASCIIString());
      MDC.remove(MDC_PRE_LOG);
      final Stopwatch stopwatch = Stopwatch.createUnstarted();
      context.setProperty(STOPWATCH_PROPERTY, stopwatch);

      if (context.hasEntity()) {
        context.setEntityStream(
          addInboundEntityToMdc(context.getEntityStream(), MessageUtils.getCharset(context.getMediaType()))
        );
      }
      stopwatch.start();
    }
  }

  @Override
  public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
    throws IOException {
    if (!"1".equals(requestContext.getHeaderString("Is-Healthcheck"))) {
      //Actual log is done when the response stream has finished in aroundWriteTo
      String log = "< " +
        Integer.toString(responseContext.getStatus()) + " " +
        requestContext.getMethod() + " " +
        requestContext.getUriInfo().getRequestUri().toASCIIString();

      Stopwatch stopwatch = (Stopwatch) requestContext.getProperty(STOPWATCH_PROPERTY);
      if (responseContext.hasEntity()) {
        //delay logging until the result has been sent to the client so we can measure the size of the result
        responseContext.setEntityStream(new LoggingOutputStream(
          responseContext.getEntityStream(),
          stopwatch,
          log,
          requestContext,
          responseContext,
          MDC.getCopyOfContextMap()
        ));
      } else {
        //log now, because the writeTo wrapper will not be called
        doLog(log, 0, stopwatch, requestContext, responseContext, MDC.getCopyOfContextMap());
      }
    }
  }

  private void doLog(String log, long bytecount, Stopwatch stopwatch, ContainerRequestContext requestContext,
                     ContainerResponseContext responseContext, Map<String, String> mdcVals) {
    //store current MDC state somewhere
    final Map<String, String> curMdc = MDC.getCopyOfContextMap();
    clearMdc();
    MDC.setContextMap(mdcVals);
    MDC.put(MDC_POST_LOG, "true");
    MDC.put(MDC_HTTP_METHOD, requestContext.getMethod());
    MDC.put(MDC_HTTP_URI, requestContext.getUriInfo().getRequestUri().toASCIIString());
    MDC.put(MDC_HTTP_PATH, requestContext.getUriInfo().getRequestUri().getPath());
    MDC.put(MDC_HTTP_AUTHORITY, requestContext.getUriInfo().getRequestUri().getAuthority());
    MDC.put(MDC_HTTP_QUERY, requestContext.getUriInfo().getRequestUri().getQuery());
    MDC.put(MDC_REQUEST_HEADERS, formatHeaders(requestContext.getHeaders()));

    MDC.put(MDC_HTTP_STATUS, Integer.toString(responseContext.getStatus()));
    MDC.put(MDC_RESPONSE_HEADERS, formatHeaders(responseContext.getStringHeaders()));

    MDC.put(MDC_OUTPUT_BYTECOUNT, bytecount + "");
    String size = " (" + bytecount + " bytes)";

    String durationLog = getDuration(stopwatch);
    LOG.info(log + size + durationLog);
    clearMdc();
    MDC.setContextMap(curMdc);
  }

  private void clearMdc() {
    MDC.remove(MDC_ID);
    MDC.remove(MDC_OUTPUT_BYTECOUNT);
    MDC.remove(MDC_DURATION_MILLISECONDS);
    MDC.remove(MDC_PRE_LOG);
    MDC.remove(MDC_POST_LOG);
    MDC.remove(MDC_HTTP_METHOD);
    MDC.remove(MDC_HTTP_URI);
    MDC.remove(MDC_HTTP_PATH);
    MDC.remove(MDC_HTTP_AUTHORITY);
    MDC.remove(MDC_HTTP_QUERY);
    MDC.remove(MDC_REQUEST_HEADERS);
    MDC.remove(MDC_HTTP_STATUS);
    MDC.remove(MDC_RESPONSE_HEADERS);
    MDC.remove(MDC_RELEASE_HASH);
    MDC.remove(MDC_REQUEST_ENTITY);
  }

  private String getDuration(Stopwatch stopWatch) {
    String durationLog;
    if (stopWatch != null && stopWatch.isRunning()) {
      long duration;
      stopWatch.stop();
      duration = stopWatch.elapsed(TimeUnit.MILLISECONDS);
      MDC.put(MDC_DURATION_MILLISECONDS, duration + "");
      durationLog = " (" + duration + " ms)";
    } else {
      durationLog = " (duration unknown)";
    }
    return durationLog;
  }

  private class LoggingOutputStream extends FilterOutputStream {
    private final Stopwatch stopwatch;
    private final String log;
    private final ContainerRequestContext requestContext;
    private final ContainerResponseContext responseContext;
    private final Map<String, String> contextMap;
    private long count;

    public LoggingOutputStream(OutputStream out, Stopwatch stopwatch, String log,
                               ContainerRequestContext requestContext, ContainerResponseContext responseContext,
                               Map<String, String> contextMap) {
      super(Preconditions.checkNotNull(out));
      this.stopwatch = stopwatch;
      this.log = log;
      this.requestContext = requestContext;
      this.responseContext = responseContext;
      this.contextMap = contextMap;
    }

    public long getCount() {
      return this.count;
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
      this.out.write(bytes, off, len);
      this.count += (long)len;
    }

    public void write(int someByte) throws IOException {
      this.out.write(someByte);
      ++this.count;
    }

    public void close() throws IOException {
      this.out.close();
      doLog(log, count, stopwatch, requestContext, responseContext, contextMap);
    }
  }
}
