package com.northwind.resource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.northwind.config.AppConfig;
import com.northwind.dao.UserDAO;
import com.northwind.entity.User;
import com.northwind.enums.UserRole;
import com.svix.Webhook;
import com.svix.exceptions.WebhookVerificationException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/webhooks")
public class ClerkWebhookResource {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private final UserDAO userDAO = new UserDAO();

	@POST
	@Path("/clerk")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response handleClerkWebhook(byte[] rawBody, @Context HttpHeaders jerseyHeaders) {

		// ── 1. Check webhook secret is configured ─────────────────────────
		String secret = AppConfig.get("CLERK_WEBHOOK_SECRET");
		if (secret == null || secret.trim().isEmpty()) {
			return Response.status(503).entity("{\"error\":\"Webhook secret not configured\"}").build();
		}

		// ── 2. Extract svix headers ───────────────────────────────────────
		String svixId = jerseyHeaders.getHeaderString("svix-id");
		String svixTimestamp = jerseyHeaders.getHeaderString("svix-timestamp");
		String svixSignature = jerseyHeaders.getHeaderString("svix-signature");

		// Debug logs
		System.out.println("🔑 Secret loaded: [" + (secret != null ? secret.substring(0, 10) : "NULL") + "]");
		System.out.println("📨 svix-id: " + svixId);
		System.out.println("📨 svix-timestamp: " + svixTimestamp);
		System.out.println("📨 svix-signature: " + (svixSignature != null ? svixSignature.substring(0, 20) : "NULL"));

		if (svixId == null || svixTimestamp == null || svixSignature == null) {
			return Response.status(400).entity("{\"error\":\"Missing svix headers\"}").build();
		}

		// ── 3. Verify signature using Svix ────────────────────────────────
		try {
			Webhook webhook = new Webhook(secret);

			// Build java.net.http.HttpHeaders from the svix header values
			HashMap<String, List<String>> headerMap = new HashMap<>();
			headerMap.put("svix-id", Arrays.asList(svixId));
			headerMap.put("svix-timestamp", Arrays.asList(svixTimestamp));
			headerMap.put("svix-signature", Arrays.asList(svixSignature));

			// Convert HashMap → java.net.http.HttpHeaders
			java.net.http.HttpHeaders svixHeaders = java.net.http.HttpHeaders.of(headerMap, (name, value) -> true // accept
																													// all
																													// headers
			);

			// Convert raw bytes to String
			String rawBodyString = new String(rawBody, StandardCharsets.UTF_8);

			// throws WebhookVerificationException if signature is invalid
			webhook.verify(rawBodyString, svixHeaders);

		} catch (WebhookVerificationException e) {
			System.err.println("❌ Clerk webhook signature invalid: " + e.getMessage());
			return Response.status(400).entity("{\"error\":\"Invalid webhook signature\"}").build();
		} catch (Exception e) {
			System.err.println("❌ Svix verification error: " + e.getMessage());
			return Response.status(400).entity("{\"error\":\"Verification error\"}").build();
		}

		// ── 4. Parse the verified payload ─────────────────────────────────
		try {
			JsonNode root = MAPPER.readTree(rawBody);
			String type = root.path("type").asText();
			JsonNode data = root.path("data");

			System.out.println("📩 Clerk webhook received: " + type);

			// ── 5. Handle user.created and user.updated ───────────────────
			if (type.equals("user.created") || type.equals("user.updated")) {

				String clerkUserId = data.path("id").asText();

				// Extract primary email
				String primaryEmailId = data.path("primary_email_address_id").asText();
				String email = "";
				for (JsonNode emailNode : data.path("email_addresses")) {
					if (emailNode.path("id").asText().equals(primaryEmailId)) {
						email = emailNode.path("email_address").asText();
						break;
					}
				}
				// Fallback to first email if primary not matched
				if (email.trim().isEmpty() && data.path("email_addresses").size() > 0) {
					email = data.path("email_addresses").get(0).path("email_address").asText();
				}

				// Build display name
				String firstName = data.path("first_name").asText("");
				String lastName = data.path("last_name").asText("");
				String username = data.path("username").asText("");
				String displayName = (firstName + " " + lastName).trim();
				if (displayName.isEmpty()) {
					displayName = username;
				}
				if (displayName.isEmpty()) {
					displayName = null;
				}

				// Parse role from public_metadata
				String roleStr = data.path("public_metadata").path("role").asText("customer");
				UserRole role = parseRole(roleStr);

				// Upsert — insert if new, update if exists
				Optional<User> existing = userDAO.findByClerkUserId(clerkUserId);
				if (existing.isEmpty()) {
					existing = userDAO.findByEmail(email); // fallback check by email
				}

				if (existing.isPresent()) {
					// UPDATE
					User user = existing.get();
					user.setClerkUserId(clerkUserId); // update clerk ID in case it changed
					user.setEmail(email);
					user.setDisplayName(displayName);
					user.setRole(role);
					userDAO.update(user);
					System.out.println("✅ User updated: " + clerkUserId);
				} else {
					// INSERT
					User user = new User(clerkUserId, email);
					user.setDisplayName(displayName);
					user.setRole(role);
					userDAO.save(user);
					System.out.println("✅ User created: " + clerkUserId);
				}
			}

			// ── 6. Handle user.deleted ────────────────────────────────────
			if (type.equals("user.deleted")) {
				String clerkUserId = data.path("id").asText();
				if (!clerkUserId.trim().isEmpty()) {
					userDAO.deleteByClerkUserId(clerkUserId);
					System.out.println("✅ User deleted: " + clerkUserId);
				}
			}

			return Response.ok("{\"ok\":true}").build();

		} catch (Exception e) {
			System.err.println("❌ Clerk webhook processing error: " + e.getMessage());
			e.printStackTrace();
			return Response.status(400).entity("{\"error\":\"Invalid webhook\"}").build();
		}
	}

	// ── Role parser ───────────────────────────────────────────────────────────
	private UserRole parseRole(String role) {
		if (role == null) {
			return UserRole.customer;
		}
		switch (role.toLowerCase()) {
		case "admin":
			return UserRole.admin;
		case "support":
			return UserRole.support;
		default:
			return UserRole.customer;
		}
	}
}