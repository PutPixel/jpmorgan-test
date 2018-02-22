package model.msg;

import java.math.BigDecimal;

import model.Product;

public class AdjustAllSalesMessage extends Message {

	private final OperationType op;
	private final BigDecimal adjustment;

	public AdjustAllSalesMessage(Product product, OperationType op, BigDecimal adjustment) {
		super(product);
		this.op = op;
		this.adjustment = adjustment;
	}

	public OperationType getOp() {
		return op;
	}

	public BigDecimal getAdjustment() {
		return adjustment;
	}
}
