package com.northwind.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.northwind.converter.CheckoutSessionLineConverter;
import com.northwind.model.CheckoutSessionLine;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "checkout_sessions")
public class CheckoutSession {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", updatable = false, nullable = false, columnDefinition = "uuid")
	private UUID id;

	/**
	 * FK → users.id (ON DELETE CASCADE — if the user is deleted, their sessions go
	 * too). Lazy loading is default for @ManyToOne in Hibernate but set explicitly
	 * for clarity.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_checkout_sessions_user"))
	private User user;

	/** Polar's own checkout identifier — nullable until Polar responds. */
	@Column(name = "polar_checkout_id", unique = true)
	private String polarCheckoutId;

	/**
	 * JSONB column: serialised as a JSON array of {productId, quantity,
	 * unitPriceCents}. The converter handles Jackson serialisation;
	 * columnDefinition keeps Hibernate from mapping it as plain VARCHAR.
	 */
	@Convert(converter = CheckoutSessionLineConverter.class)
	@Column(name = "lines", nullable = false, columnDefinition = "jsonb")
	private List<CheckoutSessionLine> lines = new ArrayList<>();

	/** Pre-computed total in the smallest currency unit. */
	@Column(name = "total_cents", nullable = false)
	private int totalCents;

	@Column(name = "currency", nullable = false)
	private String currency;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private OffsetDateTime createdAt;

	// ── Constructors ────────────────────────────────────────────────────────────

	protected CheckoutSession() {
	}

	public CheckoutSession(User user, List<CheckoutSessionLine> lines, int totalCents, String currency) {
		this.user = user;
		this.lines = lines;
		this.totalCents = totalCents;
		this.currency = currency;
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

	public String getPolarCheckoutId() {
		return polarCheckoutId;
	}

	public void setPolarCheckoutId(String polarCheckoutId) {
		this.polarCheckoutId = polarCheckoutId;
	}

	public List<CheckoutSessionLine> getLines() {
		return lines;
	}

	public void setLines(List<CheckoutSessionLine> lines) {
		this.lines = lines;
	}

	public int getTotalCents() {
		return totalCents;
	}

	public void setTotalCents(int totalCents) {
		this.totalCents = totalCents;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}