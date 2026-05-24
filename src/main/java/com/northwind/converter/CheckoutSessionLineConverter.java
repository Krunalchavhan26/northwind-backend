package com.northwind.converter;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.northwind.model.CheckoutSessionLine;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts List<CheckoutSessionLine> to/from a JSON string for storage in a
 * PostgreSQL jsonb column.
 *
 * Requires Jackson (jackson-databind) on the classpath.
 */
@Converter
public class CheckoutSessionLineConverter implements AttributeConverter<List<CheckoutSessionLine>, String> {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Override
	public String convertToDatabaseColumn(List<CheckoutSessionLine> lines) {
		if (lines == null || lines.isEmpty()) {
			return "[]";
		}
		try {
			return MAPPER.writeValueAsString(lines);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not serialise CheckoutSessionLines", e);
		}
	}

	@Override
	public List<CheckoutSessionLine> convertToEntityAttribute(String json) {
		if (json == null || json.trim().isEmpty()) {
			return Collections.emptyList();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<CheckoutSessionLine>>() {
			});
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not deserialise CheckoutSessionLines", e);
		}
	}
}