package it.unimore.dipi.iot.wldt.cache;

import it.unimore.dipi.iot.wldt.exception.WldtCacheException;

import java.util.Optional;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 09/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public interface IWldtCache<K,V> {

    public void initCache() throws WldtCacheException;

    public void delete() throws WldtCacheException;

    public void putData(K key, V value) throws WldtCacheException;;

    public Optional<V> getData(K key) throws WldtCacheException;

    public void removeData(K key) throws WldtCacheException;

}

