package com.northwind.util;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

	private static final String PERSISTENCE_UNIT = "northwindPU";
	private static EntityManagerFactory emf;

	public static void init() {
		try {
			// ── 1. Read application.properties ───────────────────────────
			Properties appProps = new Properties();
			InputStream is = JPAUtil.class.getClassLoader().getResourceAsStream("application.properties");

			if (is == null) {
				throw new RuntimeException("application.properties not found in classpath");
			}
			appProps.load(is);

			String databaseUrl = appProps.getProperty("DATABASE_URL");
			if (databaseUrl == null || databaseUrl.isBlank()) {
				throw new RuntimeException("DATABASE_URL is missing in application.properties");
			}

			// ── 2. Parse the connection string ────────────────────────────
			// postgresql://user:password@host/dbname?sslmode=require
			// change scheme so java.net.URI can parse it
			URI uri = new URI(databaseUrl.replace("postgresql://", "http://"));

			String host = uri.getHost();
			int port = uri.getPort() == -1 ? 5432 : uri.getPort();
			String dbName = uri.getPath().replace("/", "");
			String userInfo = uri.getUserInfo(); // "user:password"
			String user = userInfo.split(":")[0];
			String password = userInfo.split(":")[1];
			String query = uri.getQuery(); // "sslmode=require"

			// ── 3. Build the JDBC URL ─────────────────────────────────────
			String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s?%s", host, port, dbName,
					query != null ? query : "");

			System.out.println("✅ Connecting to: " + host + "/" + dbName);

			// ── 4. Pass properties to JPA ─────────────────────────────────
			Map<String, Object> props = new HashMap<>();
			props.put("jakarta.persistence.jdbc.url", jdbcUrl);
			props.put("jakarta.persistence.jdbc.user", user);
			props.put("jakarta.persistence.jdbc.password", password);
			props.put("jakarta.persistence.jdbc.driver", "org.postgresql.Driver");
			props.put("hibernate.hbm2ddl.auto", "update");
			props.put("hibernate.show_sql", "true");
			props.put("hibernate.format_sql", "true");

			emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, props);
			System.out.println("✅ EntityManagerFactory initialized.");

		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize JPAUtil", e);
		}
	}

	public static void close() {
		if (emf != null && emf.isOpen()) {
			emf.close();
			System.out.println("EntityManagerFactory closed.");
		}
	}

	public static EntityManager getEntityManager() {
		if (emf == null) {
			throw new IllegalStateException("JPAUtil not initialized.");
		}
		return emf.createEntityManager();
	}
}