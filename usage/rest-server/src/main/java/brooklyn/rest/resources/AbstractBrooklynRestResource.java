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
package brooklyn.rest.resources;

import io.brooklyn.camp.CampPlatform;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.codehaus.jackson.map.ObjectMapper;

import brooklyn.config.BrooklynServerConfig;
import brooklyn.config.BrooklynServiceAttributes;
import brooklyn.management.ManagementContext;
import brooklyn.management.ManagementContextInjectable;
import brooklyn.rest.util.BrooklynRestResourceUtils;
import brooklyn.rest.util.WebResourceUtils;
import brooklyn.rest.util.json.BrooklynJacksonJsonProvider;
import brooklyn.util.guava.Maybe;
import brooklyn.util.task.Tasks;
import brooklyn.util.time.Duration;

import com.google.common.annotations.VisibleForTesting;

public abstract class AbstractBrooklynRestResource implements ManagementContextInjectable {

    @VisibleForTesting
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    
    // can be injected by jersey when ManagementContext in not injected manually
    // (seems there is no way to make this optional so note it _must_ be injected;
    // most of the time that happens for free, but with test framework it doesn't,
    // so we have set up a NullServletContextProvider in our tests) 
    @Context ServletContext servletContext;
    
    private ManagementContext managementContext;
    private BrooklynRestResourceUtils brooklynRestResourceUtils;
    private ObjectMapper mapper;

    public ManagementContext mgmt() {
        return mgmtMaybe().get();
    }
    
    protected synchronized Maybe<ManagementContext> mgmtMaybe() {
        if (managementContext!=null) return Maybe.of(managementContext);
        managementContext = (ManagementContext) servletContext.getAttribute(BrooklynServiceAttributes.BROOKLYN_MANAGEMENT_CONTEXT);
        if (managementContext!=null) return Maybe.of(managementContext);
        
        return Maybe.absent("ManagementContext not available for Brooklyn Jersey Resource "+this);
    }
    
    public void injectManagementContext(ManagementContext managementContext) {
        if (this.managementContext!=null) {
            if (this.managementContext.equals(managementContext)) return;
            throw new IllegalStateException("ManagementContext cannot be changed: specified twice for Brooklyn Jersey Resource "+this);
        }
        this.managementContext = managementContext;
    }

    public synchronized BrooklynRestResourceUtils brooklyn() {
        if (brooklynRestResourceUtils!=null) return brooklynRestResourceUtils;
        brooklynRestResourceUtils = new BrooklynRestResourceUtils(mgmt());
        return brooklynRestResourceUtils;
    }
    
    protected ObjectMapper mapper() {
        if (mapper==null)
            mapper = BrooklynJacksonJsonProvider.findAnyObjectMapper(servletContext, managementContext);
        return mapper;
    }

    /** returns an object which jersey will handle nicely, converting to json,
     * sometimes wrapping in quotes if needed (for outermost json return types) */ 
    protected Object getValueForDisplay(Object value, boolean preferJson, boolean isJerseyReturnValue) {
        Object immediate = getImmediateValue(value);
        return WebResourceUtils.getValueForDisplay(mapper(), immediate, preferJson, isJerseyReturnValue);
    }

    private Object getImmediateValue(Object value) {
        return Tasks.resolving(value).as(Object.class).defaultValue(null).timeout(Duration.ZERO).swallowExceptions().get();
    }

    protected CampPlatform camp() {
        return BrooklynServerConfig.getCampPlatform(mgmt()).get();
    }
    
}
