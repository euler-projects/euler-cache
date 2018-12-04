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
package org.eulerframework.cache.inMemoryCache.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eulerframework.cache.inMemoryCache.DataNotFoundException;
import org.eulerframework.cache.inMemoryCache.DefaultObjectCache;

/**
 * 带有模糊查询功能的对象缓存,缓存键值必须为<code>String</code>类型
 * @author cFrost
 *
 * @param <DATA_T> 缓存对象类型
 * 
 * @see DefaultObjectCache
 */
public class FuzzyObjectCache<DATA_T> extends DefaultObjectCache<String, DATA_T> {

    /**
     * 新建缓存对象,默认数据生命周期为0s
     */
    public FuzzyObjectCache() {
        super();
    }

    /**
     * 新建缓存对象并指定数据生命周期
     * @param dataLife 数据生命周期,单位:毫秒,设为<code>&lt;=0</code>的值表示禁用缓存
     */
    public FuzzyObjectCache(long dataLife) {
        super(dataLife);
    }

    /**
     * 可按<code>key</code>模糊查询缓存内对象,如果符合条件的对象至少有一个过期,则返回空结果
     * @param key 缓存索引键值,不区分大小写
     * @return 符合条件的对象集合
     */
    public List<DATA_T> getFuzzy(String key){
        Set<String> keySet = this.dataMap.keySet();
        Set<String> targetKeySet = new HashSet<>();
        for(String each : keySet) {
            if(each.toUpperCase().contains(key.toUpperCase())){
                targetKeySet.add(each);
            }
        }
        return this.getAll(targetKeySet);
    }

    /**
     * 根据指定的<code>key</code>集合查询缓存对象,如果符合条件的对象至少有一个过期,则返回空结果
     * @param keySet 缓存索引键值集合,区分大小写
     * @return 符合条件的对象集合
     */
    public List<DATA_T> getAll(Set<String> keySet) {
        List<DATA_T> result = new ArrayList<>();
        for(String key : keySet) {
            DATA_T data;
            try {
                data = super.get(key);
            } catch (DataNotFoundException e) {
                return null;
            }
            result.add(data);
        }
        return result;
    }
    
    /**
     * 批量添加缓存对象
     * 如果添加成功,返回<code>true</code><br>
     * 如果缓存已被其他线程锁定,则放弃添加,返回<code>false</code><br>
     * 如果数据生命周期被设为<code>&lt;=0</code>的值,则放弃添加,返回<code>false</code><br>
     * @param dataMap 缓存对象索引键值与缓存对象的Map集合
     * @return
     */
    public boolean putAll(Map<String, DATA_T> dataMap){

        if(this.dataLife <= 0)
            return false;
        
        if (this.cacheWriteLock.tryLock()) {
            try {
                Map<String, DataStore<DATA_T>> dataStoreMap = new HashMap<>();
                
                Set<Entry<String, DATA_T>> entrySet = dataMap.entrySet();
                for(Entry<String, DATA_T> entry : entrySet){
                    String key = entry.getKey();
                    DATA_T obj = entry.getValue();
                    dataStoreMap.put(key, new DataStore<>(obj));
                }
                
                this.dataMap.putAll(dataStoreMap);
                return true;
            } finally {
                this.cacheWriteLock.unlock();
            }
        }
        
        return false;
    }
}
