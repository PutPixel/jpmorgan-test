import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;

import model.Product;
import model.msg.AdjustAllSalesMessage;
import model.msg.BulkSaleMessage;
import model.msg.Message;
import model.msg.OperationType;
import model.msg.SingleSaleMessage;

public class Main {

	@SuppressWarnings("InfiniteLoopStatement")
	public static void main(String[] args) {
		Reporter reporter = new OutStreamReporter(System.out);
		MessageProcessor messageProcessor = new MessageProcessor(reporter);
		Scanner in = new Scanner(System.in);
		while (true) {
			Message msg = waitForNextMessage(in);
			if (msg != null) {
				messageProcessor.processMessage(msg);
			}
		}
	}

	private static Message waitForNextMessage(Scanner in) {
		try {
			System.out.println("Enter product name");
			Product product = new Product(in.next());

			System.out.println("Enter message type");
			int message = in.nextInt();
			if (message == 1) {
				System.out.println("Enter sale price");
				BigDecimal price = in.nextBigDecimal();
				return new SingleSaleMessage(product, price);
			} else if (message == 2) {
				System.out.println("Enter sales count");
				long count = in.nextLong();
				System.out.println("Enter sales price");
				BigDecimal price = in.nextBigDecimal();
				return new BulkSaleMessage(count, product, price);
			} else if (message == 3) {
				System.out.println("Enter adjustment value");
				BigDecimal adj = in.nextBigDecimal();

				System.out.println("Enter operation (" + Arrays.toString(OperationType.values()) + ")");
				OperationType op = OperationType.valueOf(in.next());

				return new AdjustAllSalesMessage(product, op, adj);
			} else {
				System.out.println("Invalid message number");
			}
		} catch (Exception ignore) {
			System.out.println("Invalid input!");
		}
		return null;
	}

}
