/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.eulerframework.cache.inMemoryCache;


import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import net.eulerframework.common.base.log.LogSupport;

/**
 * Created by cFrost on 16/10/17.
 */
public abstract class AbstractObjectCache<KEY_T, DATA_T> extends LogSupport {
    protected final HashMap<KEY_T, DataStore<DATA_T>> dataMap = new HashMap<>();

    protected ReentrantLock cacheWriteLock = new ReentrantLock();

    /**
     * 向缓存添加缓存对象,如果缓存已被其他线程锁定,则放弃添加,返回{@code false}
     * @param key 缓存索引键值
     * @param data 缓存对象
     * @return 成功返回{@code true};失败返回{@code false}
     */
    public boolean put(KEY_T key, DATA_T data) {
        if(!this.isEnable())
            return false;

        if(this.cacheWriteLock.tryLock()) {
            try {
                this.dataMap.put(key, new DataStore<DATA_T>(data));
                if(this.logger.isDebugEnabled()) {
                    this.logger.debug("Data key '" + key + "' has been added to cache.");
                }
                return true;
            } finally {
                this.cacheWriteLock.unlock();
            }
        }

        return false;
    }

    /**
     * 删除缓存对象,如果缓存已被其他线程锁定,则放弃删除,返回{@code false}
     * @param key 缓存索引键值
     * @return 成功返回{@code true};失败返回{@code false}
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
     * 清除所有缓存对象,如果缓存已被其他线程锁定,则放弃清除,返回{@code false}
     * @return 成功返回{@code true};失败返回{@code false}
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
     * 清理缓存,尝试删除所有过期缓存对象
     */
    public void clean() {
        Set<KEY_T> keySet = this.dataMap.keySet();
        Set<KEY_T> keySetNeedRemove = new HashSet<>();
        for(KEY_T key : keySet) {
            DataStore<DATA_T> storedData = this.dataMap.get(key);

            if(this.isExpired(storedData)) {
                keySetNeedRemove.add(key);
                if(this.logger.isDebugEnabled()) {
                    this.logger.debug("Data key '" + key + "' was time out and will be removed.");
                }

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
    
    /**
     * 查询缓存对象
     * 
     * <p>在缓存对象不存在或已过期时,会调用{@link DataGetter#getData(Object)}获取数据,并更新缓存池</p>
     * 
     * @param key 缓存对象键值
     * @param dataGetter 缓存对象不存在或已过期时的数据读取回调
     * @return 缓存对象
     */
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
     * @param storedData 待判断的缓存对象存储类
     * @return 过期返回{@code true}, 未过期返回{@code false}
     */
    public abstract boolean isExpired(DataStore<DATA_T> storedData);

    /**
     * 判断缓存是否启用
     * @return 启用返回{@code true}, 未启用返回{@code false}
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
        
        /**
         * 从数据源读取数据
         * 
         * <p>当被查询的缓存对象不存在或已过期时回调用该方法, 该方法中应有真实数据的读取逻辑</p>
         * 
         * @param key 缓存对象键值
         * @return 读取到的最新数据
         */
        DATA_T getData(KEY_T key);
    }
}
