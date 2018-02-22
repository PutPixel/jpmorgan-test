import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Product;
import model.SalesStat;
import model.msg.AdjustAllSalesMessage;
import model.msg.BulkSaleMessage;
import model.msg.Message;
import model.msg.SingleSaleMessage;

public class MessageProcessor {

	private Reporter out;

	private boolean paused = false;
	private int messageCount = 1;

	private Map<Product, SalesStat> snapshot = new HashMap<>();

	private List<Message> history = new ArrayList<>();

	public MessageProcessor(Reporter out) {
		this.out = out;
	}

	public boolean processMessage(Message msg) {
		try {
			history.add(msg);
			if (paused) {
				out.reportServiceOnPause();
				reportSaleStatsIfNeeded();
				return false;
			}

			processMessageInternal(msg);

			reportSaleStatsIfNeeded();

			if (messageCount == 50) {
				out.reportSwitchToPauseMode();
				paused = true;
			}
			return true;
		} finally {
			messageCount++;
		}
	}

	private void reportSaleStatsIfNeeded() {
		if (messageCount % 10 == 0) {
			out.reportSalesStats(snapshot.values());
		}
	}

	private void processMessageInternal(Message msg) {
		Product product = msg.getProduct();
		SalesStat salesStat = snapshot.get(product);
		if (salesStat == null) {
			salesStat = new SalesStat(product);
			snapshot.put(product, salesStat);
		}

		if (msg instanceof SingleSaleMessage) {
			SingleSaleMessage single = (SingleSaleMessage) msg;
			salesStat.newSale(single.getPrice());
		} else if (msg instanceof BulkSaleMessage) {
			BulkSaleMessage bulk = (BulkSaleMessage) msg;
			salesStat.bulkSale(bulk.getSalesCount(), bulk.getPrice());
		} else if (msg instanceof AdjustAllSalesMessage) {
			AdjustAllSalesMessage adj = (AdjustAllSalesMessage) msg;
			salesStat.adjust(adj.getOp(), adj.getAdjustment());
		} else {
			throw new RuntimeException("Unknown message type : " + msg.getClass());
		}
	}

	protected List<Message> getHistory() {
		return history;
	}

	protected Map<Product, SalesStat> getSnapshot() {
		return snapshot;
	}
}
