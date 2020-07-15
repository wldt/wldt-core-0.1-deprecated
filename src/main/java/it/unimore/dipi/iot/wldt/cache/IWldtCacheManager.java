package it.unimore.dipi.iot.wldt.cache;

import it.unimore.dipi.iot.wldt.exception.WldtCacheException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 09/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public interface IWldtCacheManager<K,V> {

    public void initCache(String cacheIdentifier, long expirationTimeValue, TimeUnit timeUnit, int maximumSize) throws WldtCacheException;

    public void deleteCache(String cacheIdentifier) throws WldtCacheException;

    public void addData(String cacheIdentifier, K key, V value) throws WldtCacheException;;

    public Optional<V> getData(String cacheIdentifier, K key) throws WldtCacheException;

}
