package com.northwind.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
	private static final Properties props = new Properties();

	static {
		try {
			// 1. Try external file first (same dir as JAR) — used on EC2
			String externalPath = System.getProperty("user.dir") + "/application.properties";
			java.io.File externalFile = new java.io.File(externalPath);

			InputStream is;
			if (externalFile.exists()) {
				is = new FileInputStream(externalFile);
			} else {
				// 2. Fallback to inside JAR — used in local dev
				is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties");
			}

			if (is != null) {
				props.load(is);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to load application.properties", e);
		}
	}

	public static String get(String key) {
		return props.getProperty(key);
	}

	public static String getRequired(String key) {
		String value = props.getProperty(key);
		if (value == null || value.isBlank()) {
			throw new RuntimeException("Missing required config: " + key);
		}
		return value;
	}
}