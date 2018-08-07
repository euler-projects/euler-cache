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
