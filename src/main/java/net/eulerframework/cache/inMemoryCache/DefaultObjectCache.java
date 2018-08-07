package net.eulerframework.cache.inMemoryCache;

import java.util.Date;

/**
 * 预先指定数据生命周期的对象缓存
 * 
 * <p>如果把生命周期指定为{@link Long#MAX_VALUE}表示缓存永不过期</p>
 * 
 * Created by cFrost on 16/10/17.
 */
public class DefaultObjectCache<KEY_T, DATA_T> extends AbstractObjectCache<KEY_T, DATA_T> {

    protected long dataLife;

    public void setDataLife(long dataLife) {
        this.dataLife = dataLife;
    }

    protected DefaultObjectCache() {
    }

    protected DefaultObjectCache(long dataLife) {
        this.dataLife = dataLife;
    }

    @Override
    public boolean isExpired(DataStore<DATA_T> storedData) {
        //指定为Long.MAX_VALUE表示数据永不过期
        if(this.dataLife == Long.MAX_VALUE ) 
            return false;
        
        if(storedData == null || new Date().getTime() - storedData.getAddTime() >= this.dataLife) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isEnable() {
        return this.dataLife > 0 ? true : false;
    }
}
