package com.northwind;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.northwind.config.AppConfig;
import com.northwind.util.JPAUtil;

public class Main {
	public static void main(String[] args) {
		// ── 1. Init JPA ───────────────────────────────────────────────────
		JPAUtil.init();

		// ── 2. Configure Jersey ───────────────────────────────────────────
		ResourceConfig config = new ResourceConfig();
		config.packages("com.northwind.resource");
		config.packages("com.northwind.filter");

		// ── 3. Wrap Jersey in a Jetty servlet holder ──────────────────────
		ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));

		// ── 4. Boot embedded Jetty ────────────────────────────────────────
		int port = Integer.parseInt(AppConfig.getRequired("SERVER_PORT"));
		Server server = new Server(port);

		// ── 5. Context rooted at "/" ──────────────────────────────────────
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);

		// ── 6. Mount Jersey at /api/* ─────────────────────────────────────
		context.addServlet(jerseyServlet, "/api/*");

		// ── 7. Start ──────────────────────────────────────────────────────
		try {
			server.start();
			System.out.println("✅ Server started  → http://localhost:6080");
			System.out.println("✅ API available   → http://localhost:6080/api");

			// ── 8. Keep alive ─────────────────────────────────────────────
			server.join();

		} catch (Exception e) {
			System.err.println("❌ Failed to start Jetty: " + e.getMessage());
			e.printStackTrace();

		} finally {
			// ── 9. Cleanup on shutdown ────────────────────────────────────
			try {
				server.stop();
				server.destroy();
			} catch (Exception e) {
				System.err.println("❌ Failed to stop Jetty: " + e.getMessage());
			}
			JPAUtil.close();
		}
	}
}