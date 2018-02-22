package model;

import java.math.BigDecimal;

import model.msg.OperationType;

public class SalesStat {

	private final Product product;

	private long sales = 0;
	private BigDecimal total = BigDecimal.ZERO;

	public SalesStat(Product product) {
		this.product = product;
	}

	public void newSale(BigDecimal price) {
		sales++;
		total = total.add(price);
	}

	public void bulkSale(long count, BigDecimal price) {
		sales += count;
		total = total.add(price.multiply(new BigDecimal(count)));
	}

	public void adjust(OperationType op, BigDecimal adjustment) {
		if (op == OperationType.MULTIPLY) {
			total = total.multiply(adjustment);
		} else {
			BigDecimal totalAdjustment = adjustment.multiply(new BigDecimal(sales));
			if (op == OperationType.SUBTRACT) {
				totalAdjustment = totalAdjustment.negate();
			}
			total = total.add(totalAdjustment);
		}
	}

	public Product getProduct() {
		return product;
	}

	public long getSales() {
		return sales;
	}

	public BigDecimal getTotal() {
		return total;
	}
}
