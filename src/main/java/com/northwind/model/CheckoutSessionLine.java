package com.northwind.model;

import java.io.Serializable;

/**
 * Represents a single line inside the checkout_sessions.lines JSONB column. Not
 * a managed entity — serialised/deserialised via a JPA AttributeConverter.
 */
public class CheckoutSessionLine implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String productId;
	private int quantity;
	private int unitPriceCents;

	public CheckoutSessionLine() {
	}

	public CheckoutSessionLine(String productId, int quantity, int unitPriceCents) {
		this.productId = productId;
		this.quantity = quantity;
		this.unitPriceCents = unitPriceCents;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
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
}