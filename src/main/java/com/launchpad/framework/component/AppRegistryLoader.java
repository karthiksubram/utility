package com.launchpad.framework.component;

import com.launchpad.framework.dto.AppMetadata;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AppRegistryLoader implements CommandLineRunner {
	private static final String APP_REGISTRY_FILE = "apps_registry.txt";
	private final Map<Long, AppMetadata> appRegistry = new HashMap<>();
	private static final AtomicLong appIdCounter = new AtomicLong(1);
	private final ResourceLoader resourceLoader;

	@Override
	public void run(String... args) {
		loadRegistry();
	}

	public AppRegistryLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void loadRegistry() {
//		File file = new File(APP_REGISTRY_FILE);
		Resource resource = resourceLoader.getResource("classpath:" + APP_REGISTRY_FILE);
		if (resource.exists()) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
				String line;
				AppMetadata metadata;
				while ((line = reader.readLine()) != null) {
					metadata = new AppMetadata();
					String[] parts = line.split(",", 4);
					if (parts.length == 4) {
						long appId = Long.parseLong(parts[0]);
						metadata.setId(appId);
						metadata.setName(parts[1]);
						metadata.setDescription(parts[2]);
						metadata.setFilePath(parts[3]);
						appRegistry.put(appId, metadata);
						appIdCounter.set(Math.max(appIdCounter.get(), appId + 1));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Map<Long, AppMetadata> getAppRegistry() {
		return appRegistry;
	}

	public void addApp(long appId, AppMetadata metadata) {
		appRegistry.put(appId, metadata);
	}
}
