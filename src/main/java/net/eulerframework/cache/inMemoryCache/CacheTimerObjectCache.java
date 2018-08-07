package net.eulerframework.cache.inMemoryCache;

/**
 * Created by cFrost on 16/10/17.
 */
public class CacheTimerObjectCache<KEY_T, DATA_T> extends AbstractObjectCache<KEY_T, DATA_T> {

    protected CacheTimer<DATA_T> cahceTimer;

    public void setCahceTimer(CacheTimer<DATA_T> cahceTimer) {
        this.cahceTimer = cahceTimer;
    }

    protected CacheTimerObjectCache() {
    }

    protected CacheTimerObjectCache(CacheTimer<DATA_T> cahceTimer) {
        this.cahceTimer = cahceTimer;
    }

    @Override
    public boolean isExpired(DataStore<DATA_T> storedData) {
        if(storedData == null) {
            return true;
        }

        return this.cahceTimer.isTimeout(storedData.getData(), storedData.getAddTime());
    }

    @Override
    public boolean isEnable() {
        return this.cahceTimer == null ? false : true;
    }

    public interface CacheTimer<T> {
        boolean isTimeout(T data, long addTime);
    }
}
