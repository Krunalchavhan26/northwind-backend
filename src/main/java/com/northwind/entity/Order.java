package com.northwind.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.northwind.enums.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
	private UUID id;

	/**
	 * FK → users.id (ON DELETE CASCADE). Mirrors ordersRelations: each order
	 * belongs to exactly one user.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_orders_user"))
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private OrderStatus status = OrderStatus.pending;

	/**
	 * Polar checkout reference — may be null if the order was created before
	 * checkout completed.
	 */
	@Column(name = "polar_checkout_id")
	private String polarCheckoutId;

	/** Polar's own order ID, unique once Polar confirms the order. */
	@Column(name = "polar_order_id", unique = true)
	private String polarOrderId;

	@Column(name = "total_cents", nullable = false)
	private int totalCents = 0;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private OffsetDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private OffsetDateTime updatedAt;

	// ── Relationships ───────────────────────────────────────────────────────────

	/**
	 * One order → many line items (mirrors ordersRelations). CascadeType.ALL +
	 * orphanRemoval mirrors ON DELETE CASCADE in DDL.
	 */
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	// ── Helpers ─────────────────────────────────────────────────────────────────

	/** Convenience: add a line item and keep the bi-directional link consistent. */
	public void addItem(OrderItem item) {
		items.add(item);
		item.setOrder(this);
	}

	public void removeItem(OrderItem item) {
		items.remove(item);
		item.setOrder(null);
	}

	// ── Constructors ────────────────────────────────────────────────────────────

	protected Order() {
	}

	public Order(User user) {
		this.user = user;
	}

	// ── Getters & Setters ───────────────────────────────────────────────────────

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public String getPolarCheckoutId() {
		return polarCheckoutId;
	}

	public void setPolarCheckoutId(String polarCheckoutId) {
		this.polarCheckoutId = polarCheckoutId;
	}

	public String getPolarOrderId() {
		return polarOrderId;
	}

	public void setPolarOrderId(String polarOrderId) {
		this.polarOrderId = polarOrderId;
	}

	public int getTotalCents() {
		return totalCents;
	}

	public void setTotalCents(int totalCents) {
		this.totalCents = totalCents;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}
}