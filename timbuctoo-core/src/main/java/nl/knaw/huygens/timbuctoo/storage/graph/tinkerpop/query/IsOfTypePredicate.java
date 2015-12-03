package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public final class IsOfTypePredicate implements com.tinkerpop.blueprints.Predicate {

  public static final Logger LOG = LoggerFactory.getLogger(IsOfTypePredicate.class);
  private final LoadingCache<Object, String> cache;

  public IsOfTypePredicate() {
    cache = CacheBuilder.newBuilder().maximumSize(100).build(new CacheLoader<Object, String>() {
      @Override
      public String load(Object key) throws Exception {
        return "\"" + key + "\"";
      }
    });
  }

  @Override
  public boolean evaluate(Object object, Object shouldApplyTo) {
    if (object != null && (object instanceof String)) {
      return ((String) object).contains(getSubstring(shouldApplyTo));
    }
    return false;
  }

  protected String getSubstring(Object shouldApplyTo) {
    String value = null;
    try { 
      value = cache.get(shouldApplyTo);
    } catch (ExecutionException e) {
      LOG.error("Error retrieving item from cache", e);
    }
    return value;
  }
}
