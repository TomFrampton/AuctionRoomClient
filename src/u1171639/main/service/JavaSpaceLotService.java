package u1171639.main.service;

import java.rmi.RemoteException;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;
import u1171639.main.model.Lot;
import u1171639.main.utilities.LotIDCounter;

public class JavaSpaceLotService implements LotService {
	private JavaSpace space;
	
	public JavaSpaceLotService(JavaSpace space) {
		this.space = space;
	}
	
	@Override
	public int addLot(Lot lot) {
		try {
			LotIDCounter counter = (LotIDCounter) space.take(new LotIDCounter(), null, Lease.FOREVER);
			
			lot.id = counter.id;
			counter.increment();
			
			space.write(lot, null, Lease.FOREVER);
			space.write(counter, null, Lease.FOREVER);
			
			return lot.id;
			
		} catch (RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
	}
}
