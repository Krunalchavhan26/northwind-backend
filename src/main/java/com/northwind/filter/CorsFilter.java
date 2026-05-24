package com.northwind.filter;

import java.io.IOException;

import com.northwind.config.AppConfig;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CorsFilter implements ContainerResponseFilter {

	private static final String ALLOWED_ORIGINS = AppConfig.getRequired("CORS_ALLOWED_ORIGINs");

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		String origin = requestContext.getHeaderString("Origin");

		// Only set header if request origin matches allowed origins
		if (origin != null && isAllowed(origin)) {
			responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
			responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
			responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
			responseContext.getHeaders().add("Access-Control-Allow-Headers",
					"Origin, Content-Type, Accept, Authorization");
		}

	}

	private boolean isAllowed(String origin) {
		// Supports comma-separated list: http://localhost:3000,https://myapp.com
		String[] allowed = ALLOWED_ORIGINS.split(",");
		for (String a : allowed) {
			if (a.trim().equalsIgnoreCase(origin)) {
				return true;
			}
		}
		return false;
	}

}
