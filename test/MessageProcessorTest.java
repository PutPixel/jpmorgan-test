import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import model.Product;
import model.SalesStat;
import model.msg.AdjustAllSalesMessage;
import model.msg.BulkSaleMessage;
import model.msg.OperationType;
import model.msg.SingleSaleMessage;

public class MessageProcessorTest {

	private static final Product P1 = new Product("P1");
	private static final Product P2 = new Product("P2");

	private MessageProcessor processor;
	private ByteArrayOutputStream baos;

	private String getMessage() {
		return new String(baos.toByteArray(), StandardCharsets.UTF_8);
	}

	private BigDecimal p(String price) {
		return new BigDecimal(price);
	}

	@Before
	public void setUp() throws Exception {
		baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos, true, "utf-8");
		processor = new MessageProcessor(new OutStreamReporter(ps));
	}

	@After
	public void tearDown() {
		processor = null;
		baos = null;
	}

	@Test
	public void singleSaleMessages() {
		processor.processMessage(new SingleSaleMessage(P1, p("10")));
		assertThat(processor.getHistory().size(), is(1));
		assertThat(processor.getSnapshot().size(), is(1));
		SalesStat snapshot = processor.getSnapshot().get(P1);
		assertThat(snapshot.getProduct(), is(P1));
		assertThat(snapshot.getSales(), is(1L));
		assertThat(snapshot.getTotal(), is(p("10")));
	}

	@Test
	public void bulkSaleMessages() {
		processor.processMessage(new BulkSaleMessage(5, P1, p("10")));
		assertThat(processor.getHistory().size(), is(1));
		assertThat(processor.getSnapshot().size(), is(1));
		SalesStat snapshot = processor.getSnapshot().get(P1);
		assertThat(snapshot.getProduct(), is(P1));
		assertThat(snapshot.getSales(), is(5L));
		assertThat(snapshot.getTotal(), is(p("50")));
	}

	@Test
	public void saleSubtractAdjustmentMessages() {
		processor.processMessage(new SingleSaleMessage(P1, p("10")));
		processor.processMessage(new SingleSaleMessage(P1, p("10")));

		processor.processMessage(new AdjustAllSalesMessage(P1, OperationType.SUBTRACT, p("1")));

		assertThat(processor.getHistory().size(), is(3));
		assertThat(processor.getSnapshot().size(), is(1));

		SalesStat snapshot = processor.getSnapshot().get(P1);
		assertThat(snapshot.getProduct(), is(P1));
		assertThat(snapshot.getSales(), is(2L));
		assertThat(snapshot.getTotal(), is(p("18")));
	}

	@Test
	public void saleAddAdjustmentMessages() {
		processor.processMessage(new SingleSaleMessage(P1, p("10")));
		processor.processMessage(new SingleSaleMessage(P1, p("10")));

		processor.processMessage(new AdjustAllSalesMessage(P1, OperationType.ADD, p("1")));

		assertThat(processor.getHistory().size(), is(3));
		assertThat(processor.getSnapshot().size(), is(1));

		SalesStat snapshot = processor.getSnapshot().get(P1);
		assertThat(snapshot.getProduct(), is(P1));
		assertThat(snapshot.getSales(), is(2L));
		assertThat(snapshot.getTotal(), is(p("22")));
	}

	@Test
	public void saleMultAdjustmentMessages() {
		processor.processMessage(new SingleSaleMessage(P1, p("10")));
		processor.processMessage(new SingleSaleMessage(P1, p("10")));

		processor.processMessage(new AdjustAllSalesMessage(P1, OperationType.MULTIPLY, p("2")));

		assertThat(processor.getHistory().size(), is(3));
		assertThat(processor.getSnapshot().size(), is(1));

		SalesStat snapshot = processor.getSnapshot().get(P1);
		assertThat(snapshot.getProduct(), is(P1));
		assertThat(snapshot.getSales(), is(2L));
		assertThat(snapshot.getTotal(), is(p("40")));
	}

	@Test
	public void each10MessagesStatsArePrintedButNotBefore() {
		IntStream.range(1, 11).forEach(it -> {
			String lastMessage = getMessage();
			assertThat(lastMessage, is(""));
			processor.processMessage(new SingleSaleMessage(P1, p("10")));
		});

		String lastMessage = getMessage();
		assertThat(lastMessage, is("Product: P1, sales: 10, total: 100\r\n"
				+ "--------------------------------------\r\n"
				+ "All products total: 100\r\n"));
	}

	@Test
	public void each10MessagesStatsArePrinted() {
		IntStream.range(1, 50).forEach(it -> {
			processor.processMessage(new SingleSaleMessage(P1, p("10")));

			if (it % 10 == 0) {
				int total = 10 * it;
				String lastMessage = getMessage();
				assertThat(lastMessage, is("Product: P1, sales: " + it + ", total: " + total + "\r\n"
						+ "--------------------------------------\r\n"
						+ "All products total: " + total + "\r\n"));
				baos.reset();
			} else {
				String lastMessage = getMessage();
				assertThat(lastMessage, is(""));
			}
		});
	}

	@Test
	public void on50MessagesSwitchToPauseMode() {
		IntStream.range(1, 51).forEach(it -> {
			boolean messageProcessed = processor.processMessage(new SingleSaleMessage(P1, p("10")));
			assertThat(messageProcessed, is(true));
			if (it == 50) {
				int total = 10 * it;
				String lastMessage = getMessage();
				assertThat(lastMessage, is("Product: P1, sales: " + it + ", total: " + total + "\r\n"
						+ "--------------------------------------\r\n"
						+ "All products total: " + total + "\r\n"
						+ "Service on pause, messages won't be processed\r\n"));
			} else {
				baos.reset();
			}
		});
	}

	@Test
	public void after50MessagesLogPausedMode() {
		IntStream.range(1, 61).forEach(it -> {
			boolean messageProcessed = processor.processMessage(new SingleSaleMessage(P1, p("10")));
			if (it == 60) {
				assertThat(messageProcessed, is(false));
				String lastMessage = getMessage();
				int total = 10 * 50;
				assertThat(lastMessage, is("Service on pause, messages won't be processed\r\n"
						+ "Product: P1, sales: 50, total: " + total + "\r\n"
						+ "--------------------------------------\r\n"
						+ "All products total: " + total + "\r\n"));
			} else if (it > 50) {
				assertThat(messageProcessed, is(false));
				String lastMessage = getMessage();
				assertThat(lastMessage, is("Service on pause, messages won't be processed\r\n"));
			} else {
				assertThat(messageProcessed, is(true));
			}
			baos.reset();
		});
	}

	@Test
	public void multipleProductCase() {
		processor.processMessage(new SingleSaleMessage(P1, p("10")));
		processor.processMessage(new BulkSaleMessage(5, P1, p("11")));
		processor.processMessage(new AdjustAllSalesMessage(P1, OperationType.ADD, p("1")));

		processor.processMessage(new SingleSaleMessage(P2, p("11")));
		processor.processMessage(new BulkSaleMessage(50, P2, p("100")));
		processor.processMessage(new AdjustAllSalesMessage(P2, OperationType.SUBTRACT, p("10")));

		assertThat(processor.getHistory().size(), is(6));
		assertThat(processor.getSnapshot().size(), is(2));

		SalesStat snapshotP1 = processor.getSnapshot().get(P1);
		assertThat(snapshotP1.getProduct(), is(P1));
		assertThat(snapshotP1.getSales(), is(6L));
		assertThat(snapshotP1.getTotal(), is(p("71")));

		SalesStat snapshotP2 = processor.getSnapshot().get(P2);
		assertThat(snapshotP2.getProduct(), is(P2));
		assertThat(snapshotP2.getSales(), is(51L));
		assertThat(snapshotP2.getTotal(), is(p("4501")));
	}
}