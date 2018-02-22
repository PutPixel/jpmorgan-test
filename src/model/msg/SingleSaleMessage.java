package model.msg;

import java.math.BigDecimal;

import model.Product;

public class SingleSaleMessage extends Message {

	private final BigDecimal price;

	public SingleSaleMessage(Product product, BigDecimal price) {
		super(product);
		this.price = price;
	}

	public BigDecimal getPrice() {
		return price;
	}
}
