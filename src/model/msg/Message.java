package model.msg;

import model.Product;

public class Message {

	private final Product product;

	public Message(Product product) {
		this.product = product;
	}

	public Product getProduct() {
		return product;
	}
}
