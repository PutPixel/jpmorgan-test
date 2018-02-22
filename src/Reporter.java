import java.util.Collection;

import model.SalesStat;

public interface Reporter {
	void reportServiceOnPause();

	void reportSalesStats(Collection<SalesStat> values);

	void reportSwitchToPauseMode();

}
