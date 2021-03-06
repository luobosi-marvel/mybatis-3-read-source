/**
 * Copyright 2009-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ibatis.cache.Cache;

/**
 * ScheduledCache 是周期性清理缓存的装饰器，它的 clearInterval 字段记录了两次缓存清理
 * 之间的时间间隔，默认是 1h，lastClear 字段记录了最近一次清理的时间戳。ScheduledCache
 * 的 getObject()、putObject()、removeObject() 等核心方法在执行时，都会根据这两个字段
 * 检测是否需要进行清理操作，清理操作会清空缓存中所有缓存项。
 *
 * @author Clinton Begin
 */
public class ScheduledCache implements Cache {

    /**
     * 被装饰的缓存
     */
    private final Cache delegate;
    /**
     * 两次缓存清理之间的间隔时间，默认 1h
     */
    protected long clearInterval;
    /**
     * 最近一次清理的时间戳
     */
    protected long lastClear;

    public ScheduledCache(Cache delegate) {
        this.delegate = delegate;
        // 清理间隔，默认 1h
        this.clearInterval = 60 * 60 * 1000; // 1 hour
        this.lastClear = System.currentTimeMillis();
    }

    public void setClearInterval(long clearInterval) {
        this.clearInterval = clearInterval;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public int getSize() {
        clearWhenStale();
        return delegate.getSize();
    }

    @Override
    public void putObject(Object key, Object object) {
        clearWhenStale();
        delegate.putObject(key, object);
    }

    @Override
    public Object getObject(Object key) {
        return clearWhenStale() ? null : delegate.getObject(key);
    }

    @Override
    public Object removeObject(Object key) {
        clearWhenStale();
        return delegate.removeObject(key);
    }

    @Override
    public void clear() {
        lastClear = System.currentTimeMillis();
        delegate.clear();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return null;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    private boolean clearWhenStale() {
        if (System.currentTimeMillis() - lastClear > clearInterval) {
            clear();
            return true;
        }
        return false;
    }

}
