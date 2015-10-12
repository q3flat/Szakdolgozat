package hu.elte.bfw1p6.poker.client.observer.nemtudom;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import hu.elte.bfw1p6.poker.model.entity.PokerTable;

public class TableListerObserver implements Observer, Serializable {

	private static final long serialVersionUID = 1L;

	private RemoteObserver ro = null;

	public TableListerObserver(RemoteObserver ro) {
		this.ro = ro;
	}

	@Override
	public void update(Observable o, Object arg) {
		try {
			if (arg instanceof List<?>) {
				List<?> maybeTables = (List<?>)arg;
				
				if (maybeTables.size() > 0 && (maybeTables.get(0) instanceof PokerTable)) {
					ro.update(o.toString(), arg);
				}
			}
		} catch (RemoteException e) {
			System.out
			.println("Remote exception removing observer:" + this);
			o.deleteObserver(this);
		}
	}
	
	public RemoteObserver getRo() {
		return ro;
	}

}