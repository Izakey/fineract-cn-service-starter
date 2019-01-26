/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.cn.test.servicestarter;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class MicroserviceRunner {

  private ConfigurableApplicationContext appContext;
  private final Class<?> microserviceManagerClass;
  private Object monitor = new Object();
  private boolean shouldWait;

  protected MicroserviceRunner(final Class<?> microserviceManagerClass) {
    this.microserviceManagerClass = microserviceManagerClass;
  }

  public void run() {
    if(appContext != null) {
      throw new IllegalStateException("AppContext must be null to run this backend");
    }
    runBackendInThread();
    waitUntilBackendIsStarted();
  }

  private void runBackendInThread() {
    final Thread runnerThread = new MicroserviceRunnerInThread();
    shouldWait = true;
    runnerThread.setContextClassLoader(microserviceManagerClass.getClassLoader());
    runnerThread.start();
  }

  private void waitUntilBackendIsStarted() {
    try {
      synchronized (monitor) {
        if(shouldWait) {
          monitor.wait();
        }
      }
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  public void stop() {
    SpringApplication.exit(appContext);
    appContext = null;
  }

  private class MicroserviceRunnerInThread extends Thread {
    @Override
    public void run() {
      appContext = SpringApplication.run(microserviceManagerClass);
      synchronized (monitor) {
        shouldWait = false;
        monitor.notify();
      }
    }
  }
}
