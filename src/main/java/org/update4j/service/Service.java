/*
 * Copyright 2018 Mordechai Meisels
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.update4j.service;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;

import org.update4j.util.StringUtils;

public interface Service {

	long version();

	public static <T extends Service> T loadService(ModuleLayer layer, ClassLoader classLoader, Class<T> type,
					String override) {
		if (override != null && !StringUtils.isClassName(override)) {
			throw new IllegalArgumentException(override + " is not a valid Java class name.");
		}

		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		ServiceLoader<T> loader;
		List<Provider<T>> providers = new ArrayList<>();

		if (layer != null) {
			loader = ServiceLoader.load(layer, type);
			providers.addAll(loader.stream().collect(Collectors.toList()));
		}

		loader = ServiceLoader.load(type, classLoader);
		providers.addAll(loader.stream().collect(Collectors.toList()));

		if (providers.isEmpty()) {
			throw new IllegalStateException("No provider found for " + type.getCanonicalName());
		}

		if (override != null) {
			for (Provider<T> p : providers) {
				if (p.type().getCanonicalName().equals(override))
					return p.get();
			}

			System.err.print(override + " not found between providers for " + type.getCanonicalName() + ", ");
		}

		List<T> values = providers.stream().map(Provider::get).collect(Collectors.toList());

		long maxVersion = Long.MIN_VALUE;
		T maxValue = null;
		for (T t : values) {
			long version = t.version();
			if (maxVersion <= version) {
				maxVersion = version;
				maxValue = t;
			}
		}

		if (override != null) {
			System.err.println("using " + maxValue.getClass().getCanonicalName() + " instead.");
		}

		return maxValue;
	}

	public static <T extends Service> T loadService(ModuleLayer layer, ClassLoader classLoader, Class<T> type) {
		return loadService(layer, classLoader, type, null);
	}

	public static <T extends Service> T loadService(ModuleLayer layer, Class<T> type, String override) {
		return loadService(layer, null, type, override);
	}

	public static <T extends Service> T loadService(ModuleLayer layer, Class<T> type) {
		return loadService(layer, null, type, null);
	}

	public static <T extends Service> T loadService(ClassLoader classLoader, Class<T> type, String override) {
		return loadService(null, classLoader, type, override);
	}

	public static <T extends Service> T loadService(ClassLoader classLoader, Class<T> type) {
		return loadService(null, classLoader, type, null);
	}

	public static <T extends Service> T loadService(Class<T> type, String override) {
		return loadService(null, null, type, override);
	}

	public static <T extends Service> T loadService(Class<T> type) {
		return loadService(type, null);
	}

}
