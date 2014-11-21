package u1171639.main.model;

import java.util.UUID;

import net.jini.core.entry.Entry;

public interface DistributedObject extends Entry {
	public static final UUID EXECUTION_ID = UUID.randomUUID();
}
