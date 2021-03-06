/*
 * This is a common dao with basic CRUD operations and is not limited to any
 * persistent layer implementation
 *
 * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.smartitengineering.common.dao.search.solr;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.smartitengineering.common.dao.search.CommonFreeTextPersistentDao;
import com.smartitengineering.common.dao.search.solr.spi.ObjectIdentifierQuery;
import com.smartitengineering.dao.solr.MultivalueMap;
import com.smartitengineering.dao.solr.SolrWriteDao;
import com.smartitengineering.util.bean.adapter.GenericAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author imyousuf
 */
public class SolrFreeTextPersistentDao<T> implements CommonFreeTextPersistentDao<T> {

  @Inject
  private GenericAdapter<T, MultivalueMap<String, Object>> adapter;
  @Inject
  private SolrWriteDao writeDao;
  @Inject
  private ObjectIdentifierQuery<T> query;
  @Inject
  private ExecutorService executorService;
  @Inject
  @Named("waitTime")
  private long waitTime;
  @Inject
  @Named("waitTimeUnit")
  private TimeUnit waitTimeUnit;
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public void save(T... data) {
    List<Future<Status<T>>> deletes = new ArrayList<Future<Status<T>>>(data.length);
    for (final T datum : data) {
      deletes.add(executorService.submit(new Callable<Status<T>>() {

        @Override
        public Status<T> call() throws Exception {
          final boolean success;
          MultivalueMap<String, Object> fields = adapter.convert(datum);
          success = writeDao.add(fields);
          Status<T> status = new Status<T>();
          status.success = success;
          status.datum = datum;
          status.queryStr = fields.toString();
          return status;
        }
      }));
    }
    List<Status<T>> failures = new ArrayList<Status<T>>();
    for (Future<Status<T>> future : deletes) {
      try {
        Status<T> status = future.get(waitTime, waitTimeUnit);
        if (!status.success) {
          failures.add(status);
        }
      }
      catch (Exception ex) {
        logger.warn("Could not get add status!", ex);
      }
    }
    if (!failures.isEmpty()) {
      throw new IllegalStateException(new StringBuilder("Could add all data: ").append(Arrays.toString(failures.
          toArray())).toString());
    }
  }

  @Override
  public void update(T... data) {
    delete(data);
    save(data);
  }

  @Override
  public void delete(T... data) {
    List<Future<Status<T>>> deletes = new ArrayList<Future<Status<T>>>(data.length);
    for (final T datum : data) {
      deletes.add(executorService.submit(new Callable<Status<T>>() {

        @Override
        public Status<T> call() throws Exception {
          final boolean success;
          String queryStr = query.getQuery(datum);
          success = writeDao.deleteByQuery(queryStr);
          Status<T> status = new Status<T>();
          status.success = success;
          status.datum = datum;
          status.queryStr = queryStr;
          return status;
        }
      }));
    }
    List<Status<T>> failures = new ArrayList<Status<T>>();
    for (Future<Status<T>> future : deletes) {
      try {
        Status<T> status = future.get(waitTime, waitTimeUnit);
        if (!status.success) {
          failures.add(status);
        }
      }
      catch (Exception ex) {
        logger.warn("Could not get delete status!", ex);
      }
    }
    if (!failures.isEmpty()) {
      throw new IllegalStateException(new StringBuilder("Could delete all data: ").append(Arrays.toString(failures.
          toArray())).toString());
    }
  }

  private static class Status<T> {

    boolean success;
    String queryStr;
    T datum;

    @Override
    public String toString() {
      return "Status{" + "success=" + success + "queryStr=" + queryStr + "datum=" + datum + '}';
    }
  }

  public GenericAdapter<T, MultivalueMap<String, Object>> getAdapter() {
    return adapter;
  }

  public void setAdapter(GenericAdapter<T, MultivalueMap<String, Object>> adapter) {
    this.adapter = adapter;
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  public void setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public ObjectIdentifierQuery<T> getQuery() {
    return query;
  }

  public void setQuery(ObjectIdentifierQuery<T> query) {
    this.query = query;
  }

  public SolrWriteDao getWriteDao() {
    return writeDao;
  }

  public void setWriteDao(SolrWriteDao writeDao) {
    this.writeDao = writeDao;
  }

  public long getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(long waitTime) {
    this.waitTime = waitTime;
  }

  public TimeUnit getWaitTimeUnit() {
    return waitTimeUnit;
  }

  public void setWaitTimeUnit(TimeUnit waitTimeUnit) {
    this.waitTimeUnit = waitTimeUnit;
  }
}
