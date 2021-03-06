/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package brooklyn.entity.rebind.persister;

import java.io.File;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import brooklyn.entity.rebind.PersistenceExceptionHandlerImpl;
import brooklyn.entity.rebind.RebindManagerImpl;
import brooklyn.management.internal.LocalManagementContext;
import brooklyn.test.entity.LocalManagementContextForTests;
import brooklyn.util.javalang.JavaClassNames;
import brooklyn.util.os.Os;
import brooklyn.util.time.Duration;

/**
 * @author Andrea Turli
 * @deprecated just tests the deprecated {@link BrooklynMementoPersisterToMultiFile}
 */
public class BrooklynMementoPersisterToMultiFileTest extends BrooklynMementoPersisterTestFixture {

    protected File mementoDir;
    
    @Override
    protected LocalManagementContext newPersistingManagementContext() {
        mementoDir = Os.newTempDir(JavaClassNames.cleanSimpleClassName(this));
        Os.deleteOnExitRecursively(mementoDir);
        
        LocalManagementContext mgmt = new LocalManagementContextForTests();
        ((RebindManagerImpl) mgmt.getRebindManager()).setPeriodicPersistPeriod(Duration.millis(100));
        persister = new BrooklynMementoPersisterToMultiFile(mementoDir, BrooklynMementoPersisterToMultiFileTest.class.getClassLoader());
        mgmt.getRebindManager().setPersister(persister, PersistenceExceptionHandlerImpl.builder().build());
        mgmt.getHighAvailabilityManager().disabled();
        mgmt.getRebindManager().startPersistence();
        return mgmt;
    }

    @AfterMethod(alwaysRun=true)
    public void tearDown() throws Exception {
        super.tearDown();
        mementoDir = Os.deleteRecursively(mementoDir).asNullOrThrowing();
    }

    @Test(groups="Integration")
    public void testLoadAndCheckpointRawMemento() throws Exception {
        // test here is deliberately no-op; 
        // checkpoint routines not supported for this (deprecated) persister
    }
    
}
