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

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.eulerframework.cache.inMemoryCache.CacheTimerObjectCache.CacheTimer;

/**
 * Created by cFrost on 16/10/17.
 */
public class ObjectCachePool {

    private final static Set<AbstractObjectCache<?, ?>> CACHE_POOL = new HashSet<>();

    /**
     * 清理缓存池中的过期数据
     */
    public static void clean() {
        for (AbstractObjectCache<?, ?> cache : CACHE_POOL) {
            if (cache.isEnable())
                cache.clean();
        }
    }

    /**
     * 将一个对象缓存加入缓存池
     * 
     * @param cache
     *            对象缓存
     */
    public static void add(AbstractObjectCache<?, ?> cache) {
        CACHE_POOL.add(cache);
    }

    /**
     * 将一个对象缓存从缓存池移除
     * 
     * @param cache
     */
    public static void remove(AbstractObjectCache<?, ?> cache) {
        CACHE_POOL.remove(cache);
    }

    /**
     * 生成自定义的对象缓存
     * 
     * @param newCache
     *            自定义的对象缓存
     * @return 自定义的对象缓存本身,但此时它已经受缓存池管理
     */
    public static <T extends AbstractObjectCache<?, ?>> T generateObjectCache(T newCache) {
        add(newCache);
        return newCache;
    }

    /**
     * 生成默认对象缓存
     * 
     * @param dataLife
     *            缓存生命周期
     * @return 生成的缓存
     */
    public static <KEY_T, DATA_T> DefaultObjectCache<KEY_T, DATA_T> generateDefaultObjectCache(long dataLife) {

        DefaultObjectCache<KEY_T, DATA_T> newCache = new DefaultObjectCache<>(dataLife);
        CACHE_POOL.add(newCache);

        return newCache;
    }

    /**
     * 生成计时器对象缓存
     * 
     * @param cacheTimer
     *            自定义计时器
     * @return 计时器对象缓存
     */
    public static <KEY_T, DATA_T> CacheTimerObjectCache<KEY_T, DATA_T> generateCacheTimerObjectCache(
            CacheTimer<DATA_T> cacheTimer) {

        CacheTimerObjectCache<KEY_T, DATA_T> newCache = new CacheTimerObjectCache<>(cacheTimer);
        CACHE_POOL.add(newCache);

        return newCache;
    }

    /**
     * 初始化缓存池
     * 
     * @param delay
     *            启动延时
     * @param period
     *            清理频率
     */
    public static void initEulerCachePoolCleaner(long delay, long period) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                ObjectCachePool.clean();
            }
        }, delay, period);
    }
}
