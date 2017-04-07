/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2017 cFrost.sun(孙宾, SUN BIN)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * For more information, please visit the following website
 *
 * https://eulerproject.io
 * https://github.com/euler-projects/euler-cache
 * https://cfrost.net
 */
package net.eulerframework.cache.inMemoryCache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cFrost on 16/10/17.
 */
public abstract class AbstractObjectCache<KEY_T, DATA_T> {
    protected Logger logger = LogManager.getLogger(this.getClass());

    protected final HashMap<KEY_T, DataStore<DATA_T>> dataMap = new HashMap<>();

    protected ReentrantLock cacheWriteLock = new ReentrantLock();

    /**
     * 向缓存添加缓存对象<br>
     * 如果缓存已被其他线程锁定,则放弃添加,返回<code>false</code>
     * @param key 缓存索引键值
     * @param data 缓存对象
     * @return 成功返回<code>true</code>;失败返回<code>false</code>
     */
    public boolean put(KEY_T key, DATA_T data) {
        if(!this.isEnable())
            return false;

        if(this.cacheWriteLock.tryLock()) {
            try {
                this.dataMap.put(key, new DataStore<DATA_T>(data));
                return true;
            } finally {
                this.cacheWriteLock.unlock();
            }
        }

        return false;
    }

    /**
     * 删除缓存对象<br>
     * 如果缓存已被其他线程锁定,则放弃删除,返回<code>false</code>
     * @param key 缓存索引键值
     * @return 成功返回<code>true</code>;失败返回<code>false</code>
     */
    public boolean remove(KEY_T key) {
        if(this.cacheWriteLock.tryLock()) {
            try {
                this.dataMap.remove(key);
                return true;
            } finally {
                this.cacheWriteLock.unlock();
            }
        }
        return false;
    }

    /**
     * 清除所有缓存对象<br>
     * 如果缓存已被其他线程锁定,则放弃清除,返回<code>false</code>
     * @return 成功返回<code>true</code>;失败返回<code>false</code>
     */
    public boolean clear() {
        if(this.cacheWriteLock.tryLock()) {
            try {
                this.dataMap.clear();
                return true;
            } finally {
                this.cacheWriteLock.unlock();
            }
        }
        return false;
    }

    /**
     * 清理缓存<br>
     * 尝试删除所有过期缓存对象
     */
    public void clean() {
        Set<KEY_T> keySet = this.dataMap.keySet();
        Set<KEY_T> keySetNeedRemove = new HashSet<>();
        for(KEY_T key : keySet) {
            DataStore<DATA_T> storedData = this.dataMap.get(key);

            if(this.isExpired(storedData)) {
                keySetNeedRemove.add(key);

                this.logger.info("Data key = " + key + " was time out and will be removed.");

            }
        }

        for(KEY_T key : keySetNeedRemove) {
            this.remove(key);
        }
    }

    /**
     * 查询缓存对象
     * @param key 缓存索引键值
     * @return 缓存对象
     * @throws DataNotFoundException 缓存对象不存在或已过期
     */
    public DATA_T get(KEY_T key) throws DataNotFoundException {
        if(!this.isEnable()) {
            throw new DataNotFoundException();            
        }
        
        DataStore<DATA_T> storedData = this.dataMap.get(key);

        if(storedData == null) {
            throw new DataNotFoundException();            
        }   

        if(this.isExpired(storedData)) {
            this.remove(key);
            throw new DataNotFoundException();
        }

        return storedData.getData();
    }
    
    public DATA_T get(KEY_T key, DataGetter<KEY_T, DATA_T> dataGetter) {
        DATA_T data;
        
        try {
            data = this.get(key);
        } catch (DataNotFoundException e) {
            // 缓存对象不存在或过期,从实际位置查询
            data = dataGetter.getData(key);
            this.put(key, data);
        }
        
        return data;
    }

    /**
     * 判断缓存对象是否过期
     * @param storedData
     * @return
     */
    public abstract boolean isExpired(DataStore<DATA_T> storedData);

    /**
     * 判断缓存是否启用
     * @return
     */
    public abstract boolean isEnable();
    
    protected class DataStore<T> {
        private final T data;
        private final long addTime;

        public DataStore(T data) {
            this.data = data;
            this.addTime = new Date().getTime();
        }

        public T getData() {
            return data;
        }

        public long getAddTime() {
            return addTime;
        }
    }
    
    public interface DataGetter<KEY_T, DATA_T> {
        public DATA_T getData(KEY_T key);
    }
}
