package u1171639.main.utilities;

import java.rmi.RemoteException;

import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class LotIDCounter implements Entry {
	public Integer id;
	
	public LotIDCounter() {
		
	}
	
	public LotIDCounter(int id) {
		this.id = id;
	}
	
	public void increment() {
		this.id++;
	}
	
	public static void initialiseInSpace(JavaSpace space) {
		try {
			LotIDCounter template = new LotIDCounter();
			
			LotIDCounter counter = (LotIDCounter) space.readIfExists(template, null, Lease.FOREVER);
			
			if(counter == null) {
				space.write(new LotIDCounter(0), null, Lease.FOREVER);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
