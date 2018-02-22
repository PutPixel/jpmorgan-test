import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Collectors;

import model.SalesStat;

class OutStreamReporter implements Reporter {

	private PrintStream out;

	public OutStreamReporter(PrintStream out) {
		this.out = out;
	}

	@Override
	public void reportServiceOnPause() {
		out.println("Service on pause, messages won't be processed");
	}

	@Override
	public void reportSalesStats(Collection<SalesStat> values) {
		String message = values.parallelStream()
				.map(this::buildStatisticRow)
				.collect(Collectors.joining("\n"));
		out.println(message);
		out.println("--------------------------------------");
		BigDecimal total = values.parallelStream().map(SalesStat::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		out.println("All products total: " + total);
	}

	private String buildStatisticRow(SalesStat it) {
		String productName = it.getProduct().getName();
		return "Product: " + productName + ", sales: " + it.getSales() + ", total: " + it.getTotal();
	}

	@Override
	public void reportSwitchToPauseMode() {
		out.println("Service on pause, messages won't be processed");
	}
}
