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
