package u1171639.test.utilities;

import java.rmi.RemoteException;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

public class TestUtils {
	
	public static <T extends Entry> int removeAllFromSpace(T template, JavaSpace space) {
		int objectCounter = 0;
		boolean somethingToTake = true;
		
		while(somethingToTake) {
			Entry entry;
			try {
				entry = space.takeIfExists(template, null, 0);
				
				if(entry != null) {
					objectCounter++;
				} else {
					somethingToTake = false;
				}
			} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return objectCounter;
	}
}
