package digital.bangie.bstate.persistence;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BStateSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int snapshotVersion;
    private final Instant createdAt;
    private final List<BStoreSnapshot> stores;

    public BStateSnapshot(int snapshotVersion, Instant createdAt, List<BStoreSnapshot> stores) {
        this.snapshotVersion = snapshotVersion;
        this.createdAt = createdAt;
        this.stores = new ArrayList<>(stores);
    }

    public int getSnapshotVersion() {
        return snapshotVersion;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<BStoreSnapshot> getStores() {
        return Collections.unmodifiableList(stores);
    }
}