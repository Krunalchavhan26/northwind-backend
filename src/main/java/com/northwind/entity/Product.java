package com.northwind.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
	private UUID id;

	/** URL-friendly unique identifier for the product. */
	@Column(name = "slug", nullable = false, unique = true)
	private String slug;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "category", nullable = false)
	private String category = "General";

	@Column(name = "description", nullable = false, columnDefinition = "TEXT")
	private String description = "";

	/** Price in the smallest currency unit (e.g. cents for USD). */
	@Column(name = "price_cents", nullable = false)
	private int priceCents;

	@Column(name = "currency", nullable = false)
	private String currency = "usd";

	@Column(name = "image_url")
	private String imageUrl;

	/** ImageKit fileId used when deleting the image from ImageKit. */
	@Column(name = "image_kit_file_id")
	private String imageKitFileId;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private OffsetDateTime createdAt;

	// ── Relationships ───────────────────────────────────────────────────────────

	/**
	 * One product → many order-item lines (mirrors productsRelations in Drizzle).
	 */
	@OneToMany(mappedBy = "product")
	private List<OrderItem> orderItems = new ArrayList<>();

	// ── Constructors ────────────────────────────────────────────────────────────

	protected Product() {
	}

	public Product(String slug, String name, int priceCents) {
		this.slug = slug;
		this.name = name;
		this.priceCents = priceCents;
	}

	// ── Getters & Setters ───────────────────────────────────────────────────────

	public UUID getId() {
		return id;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPriceCents() {
		return priceCents;
	}

	public void setPriceCents(int priceCents) {
		this.priceCents = priceCents;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getImageKitFileId() {
		return imageKitFileId;
	}

	public void setImageKitFileId(String imageKitFileId) {
		this.imageKitFileId = imageKitFileId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public List<OrderItem> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}
}