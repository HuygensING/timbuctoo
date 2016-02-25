package nl.knaw.huygens.timbuctoo.logging;

import com.google.common.base.Stopwatch;
import com.google.common.io.CountingOutputStream;
import org.glassfish.jersey.message.MessageUtils;
import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@PreMatching
@Priority(Integer.MIN_VALUE)
public final class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

  private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());
  private static final String WRAPPED_STREAM_PROPERTY = LoggingFilter.class.getName() + "wrappedStream";
  private static final String LOG_TEXT_PROPERTY = LoggingFilter.class.getName() + "logText";
  private static final String STOPWATCH_PROPERTY = LoggingFilter.class.getName() + "stopwatch";

  private static final String MDC_ID = "requestId";

  private static final Comparator<Map.Entry<String, List<String>>> COMPARATOR =
    (o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey());

  private final int entityLogSize;

  public LoggingFilter(final int entityLogSize) {
    this.entityLogSize = entityLogSize;
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
    MDC.put("REQUEST_ENTITY", builder.toString());
    stream.reset();
    return stream;
  }

  @Override
  public void filter(final ContainerRequestContext context) throws IOException {
    final UUID id = UUID.randomUUID();
    MDC.put(MDC_ID, id.toString());
    MDC.put("PRE_LOG", "yes");

    //Log a very minimal message. Mostly to make sure that we notice requests that never log in the response filter
    LOGGER.info(">     " + context.getMethod() + " " + context.getUriInfo().getRequestUri().toASCIIString());
    final Stopwatch stopwatch = Stopwatch.createUnstarted();
    context.setProperty(STOPWATCH_PROPERTY, stopwatch);
    //FIXME check if thread name is added to MDC (Thread.currentThread().getName())

    MDC.put("REQUEST_HEADERS", formatHeaders(context.getHeaders()));

    if (context.hasEntity()) {
      context.setEntityStream(
        addInboundEntityToMdc(context.getEntityStream(), MessageUtils.getCharset(context.getMediaType()))
      );
    }
    stopwatch.start();
  }

  @Override
  public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
    throws IOException {
    MDC.remove("PRE_LOG");
    MDC.put("RESPONSE_HEADERS", formatHeaders(responseContext.getStringHeaders()));

    //Actual log is done when the response stream has finished in aroundWriteTo
    String log = "< " +
      Integer.toString(responseContext.getStatus()) + " " +
      requestContext.getMethod() + " " +
      requestContext.getUriInfo().getRequestUri().toASCIIString();

    if (responseContext.hasEntity()) {
      //delay logging until the result has been sent to the client so we can measure the size of the result
      requestContext.setProperty(LOG_TEXT_PROPERTY, log);

      //wrap the outputstream in one that measures the size
      final CountingOutputStream stream = new CountingOutputStream(responseContext.getEntityStream());
      responseContext.setEntityStream(stream);
      requestContext.setProperty(WRAPPED_STREAM_PROPERTY, stream);
    } else {
      //log now, because the writeTo wrapper will not be called
      MDC.put("OUTPUT_BYTECOUNT", "0");
      String size = " (0 bytes)";

      String durationLog = getDuration((Stopwatch) requestContext.getProperty(STOPWATCH_PROPERTY));

      LOGGER.info(log + size + durationLog);
    }
  }

  private String getDuration(Stopwatch stopWatch) {
    String durationLog;
    if (stopWatch != null && stopWatch.isRunning()) {
      long duration;
      stopWatch.stop();
      duration = stopWatch.elapsed(TimeUnit.MILLISECONDS);
      MDC.put("DURATION_MILLISECONDS", duration + "");
      durationLog = " (" + duration + " ms)";
    } else {
      durationLog = " (duration unknown)";
    }
    return durationLog;
  }

  @Override
  public void aroundWriteTo(final WriterInterceptorContext context)
    throws IOException, WebApplicationException {

    final CountingOutputStream stream = (CountingOutputStream) context.getProperty(WRAPPED_STREAM_PROPERTY);
    context.proceed();

    String durationLog = getDuration((Stopwatch) context.getProperty(STOPWATCH_PROPERTY));

    String size;
    if (stream != null) {
      MDC.put("OUTPUT_BYTECOUNT", stream.getCount() + "");
      size = " (" + stream.getCount() + " bytes)";
    } else {
      size = " (outputSize unknown)";
    }

    LOGGER.info(context.getProperty(LOG_TEXT_PROPERTY) + size + durationLog);
  }
}
