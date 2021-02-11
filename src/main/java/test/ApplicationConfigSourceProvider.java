/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.smallrye.config.PropertiesConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfigSourceProvider implements ConfigSourceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfigSourceProvider.class);

    @Override
    public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
        LOGGER.info("Getting config sources for class loader {}", forClassLoader);
        return ApplicationConfigSourceHolder.getProperties();
    }

    /**
     * We use this singleton class in order to workaround the fact that Quarkus runtime may call the ConfigSourceProvider twice.
     * see https://github.com/quarkusio/quarkus/issues/8145 and fix when it's solved by Quarkus.
     */
    private static class ApplicationConfigSourceHolder {
        private static final String RUNTIME_CLASS = "io.quarkus.deployment.steps.RuntimeConfigSetup";
        private static final ApplicationConfigSourceHolder INSTANCE = new ApplicationConfigSourceHolder();
        private final List<ConfigSource> runtimeProperties;

        private ApplicationConfigSourceHolder() {
            if (isRuntimePhase()) {
                LOGGER.info("Runtime phase");
                this.runtimeProperties = fetchProperties();
            } else {
                LOGGER.info("Deploy phase");
                this.runtimeProperties = Collections.emptyList();
            }
        }

        public static List<ConfigSource> getProperties() {
            return INSTANCE.runtimeProperties;
        }

        private List<ConfigSource> fetchProperties() {
            LOGGER.info("Fetching application and user properties");
            final Map<String, String> appProperties = Collections.singletonMap("greeting.message","hello world!");

            return List.of(
                    new PropertiesConfigSource(appProperties, "camel-k-app", ConfigSource.DEFAULT_ORDINAL)
            );
        }

        private boolean isRuntimePhase() {
            try {
                Class.forName(RUNTIME_CLASS);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

}
