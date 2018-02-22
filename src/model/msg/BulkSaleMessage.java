package model.msg;

import java.math.BigDecimal;

import model.Product;

public class BulkSaleMessage extends Message {

	private final long salesCount;

	private final BigDecimal price;

	public BulkSaleMessage(long salesCount, Product product, BigDecimal price) {
		super(product);
		this.salesCount = salesCount;
		this.price = price;
	}

	public long getSalesCount() {
		return salesCount;
	}

	public BigDecimal getPrice() {
		return price;
	}
}
