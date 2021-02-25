/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.nativex;

import java.util.Properties;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.ClassUtils;

public class NativeListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

	static {
		String imagecode = "org.graalvm.nativeimage.imagecode";
		if (System.getProperty(imagecode) == null) {
			if (!ClassUtils.isPresent("org.springframework.aot.StaticSpringFactories", null)) {
				throw new IllegalStateException("Mandatory generated class org.springframework.aot.StaticSpringFactories not found, " +
						"please make sure spring-aot-maven-plugin or spring-aot-gradle-plugin are configured properly, " +
						"and that code generation has been properly performed.");
			}
			System.setProperty(imagecode, "runtime");
		}
		if (ClassUtils.isPresent("org.hibernate.Session", null)) {
			System.setProperty("hibernate.bytecode.provider", "none");
		}
	}

	public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
		ConfigurableEnvironment environment = event.getEnvironment();
		Properties props = new Properties();
		props.put("spring.aop.proxy-target-class", "false"); // Not supported in native images
		props.put("spring.cloud.refresh.enabled", "false"); // Sampler is a class and can't be proxied
		props.put("spring.sleuth.async.enabled", "false"); // Too much proxy created
		props.put("spring.devtools.restart.enabled", "false"); // Deactivate dev tools
		environment.getPropertySources().addFirst(new PropertiesPropertySource("native", props));
	}
}