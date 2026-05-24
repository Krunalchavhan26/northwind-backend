package com.northwind.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Represents a single line item in an order. Mirrors the order_items table
 * with: - ON DELETE CASCADE on order_id → handled by Order.items cascade - ON
 * DELETE RESTRICT on product_id → no cascade; deletion of a Product that still
 * has OrderItems should be prevented at the DB level.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
	private UUID id;

	/**
	 * FK → orders.id (ON DELETE CASCADE). The cascade is managed by Order.items =
	 * CascadeType.ALL + orphanRemoval.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_items_order"))
	private Order order;

	/**
	 * FK → products.id (ON DELETE RESTRICT). No JPA cascade — the DB constraint
	 * prevents deleting a product that still has order lines.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_items_product"))
	private Product product;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	/** Snapshot of the unit price at the time of purchase (in cents). */
	@Column(name = "unit_price_cents", nullable = false)
	private int unitPriceCents;

	// ── Constructors ────────────────────────────────────────────────────────────

	protected OrderItem() {
	}

	public OrderItem(Order order, Product product, int quantity, int unitPriceCents) {
		this.order = order;
		this.product = product;
		this.quantity = quantity;
		this.unitPriceCents = unitPriceCents;
	}

	// ── Getters & Setters ───────────────────────────────────────────────────────

	public UUID getId() {
		return id;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public int getUnitPriceCents() {
		return unitPriceCents;
	}

	public void setUnitPriceCents(int unitPriceCents) {
		this.unitPriceCents = unitPriceCents;
	}

	/** Derived: total value of this line in cents. */
	public int getLineTotalCents() {
		return quantity * unitPriceCents;
	}
}