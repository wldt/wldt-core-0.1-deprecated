package it.unimore.dipi.iot.wldt.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimore.dipi.iot.wldt.exception.WldtCacheException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 09/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtCache<K, V> implements IWldtCache<K,V> {

    private static final Logger logger = LoggerFactory.getLogger(WldtCache.class);

    private long expirationTimeValue;

    private TimeUnit timeUnit;

    private int maximumSize;

    private String cacheIdentifier;

    private Cache<K, V> cache = null;

    public WldtCache(String cacheIdentifier, long expirationTimeValue, TimeUnit timeUnit,  int maximumSize) {
        this.cacheIdentifier = cacheIdentifier;
        this.expirationTimeValue = expirationTimeValue;
        this.timeUnit = timeUnit;
        this.maximumSize = maximumSize;
    }

    public WldtCache(long expirationTimeValue, TimeUnit timeUnit,  int maximumSize) {
        this.cacheIdentifier = UUID.randomUUID().toString();
        this.expirationTimeValue = expirationTimeValue;
        this.timeUnit = timeUnit;
        this.maximumSize = maximumSize;
    }

    public WldtCache(long expirationTimeValue, TimeUnit timeUnit) {
        this.cacheIdentifier = UUID.randomUUID().toString();
        this.expirationTimeValue = expirationTimeValue;
        this.timeUnit = timeUnit;
        this.maximumSize = 0;
    }

    public WldtCache() {
        this.cacheIdentifier = UUID.randomUUID().toString();
        this.expirationTimeValue = 0;
        this.timeUnit = null;
        this.maximumSize = 0;
    }

    @Override
    public void initCache() throws WldtCacheException {

        try{

            if(this.cache != null)
                throw new WldtCacheException("Cache already initialized !");

            @NonNull Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();

            if(expirationTimeValue > 0 && timeUnit != null)
                cacheBuilder.expireAfterWrite(expirationTimeValue, timeUnit);

            if(maximumSize > 0)
                cacheBuilder.maximumSize(maximumSize);

            this.cache = cacheBuilder.build();

        }catch (Exception e){
            logger.error("Error initializing cache ! Error: {}", e.getLocalizedMessage());
            throw new WldtCacheException(String.format("Error initializing cache ! Error: %s", e.getLocalizedMessage()));
        }
    }

    @Override
    public void delete() throws WldtCacheException {

        if(this.cache == null)
            throw new WldtCacheException(String.format("Cache %s not found !", cacheIdentifier));

        this.cache.cleanUp();
    }

    @Override
    public void putData(K key, V value) throws WldtCacheException {

        if(this.cache == null)
            throw new WldtCacheException(String.format("Cache %s not found !", cacheIdentifier));

        this.cache.put(key, value);
    }

    @Override
    public Optional<V> getData(K key) throws WldtCacheException {

        if(this.cache == null)
            throw new WldtCacheException(String.format("Cache %s not found !", cacheIdentifier));

        return Optional.ofNullable(this.cache.getIfPresent(key));
    }

    @Override
    public void removeData(K key) throws WldtCacheException {
        if(this.cache == null)
            throw new WldtCacheException(String.format("Cache %s not found !", cacheIdentifier));

         this.cache.invalidate(key);
    }

    public static void main(String[] args) {

        try {

            String myCacheId = "mycacheid";
            String testKey = "test";

            WldtCache<String, String> wldtCache = new WldtCache<>(myCacheId, 10, TimeUnit.SECONDS, 0);
            wldtCache.initCache();
            wldtCache.putData(testKey, "test");

            for(int i=0; i<11; i++){

                Thread.sleep(1000);

                if(wldtCache.getData("test").isPresent())
                    logger.info("Cached Data ({}): {}", testKey, wldtCache.getData("test").get());
                else
                    logger.error("Cached Data ({}) not found !", testKey);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public long getExpirationTimeValue() {
        return expirationTimeValue;
    }

    public void setExpirationTimeValue(long expirationTimeValue) {
        this.expirationTimeValue = expirationTimeValue;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getMaximumSize() {
        return maximumSize;
    }

    public void setMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
    }

    public String getCacheIdentifier() {
        return cacheIdentifier;
    }

    public void setCacheIdentifier(String cacheIdentifier) {
        this.cacheIdentifier = cacheIdentifier;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("WldtCache{");
        sb.append("expirationTimeValue=").append(expirationTimeValue);
        sb.append(", timeUnit=").append(timeUnit);
        sb.append(", maximumSize=").append(maximumSize);
        sb.append(", cacheIdentifier='").append(cacheIdentifier).append('\'');
        sb.append(", cache=").append(cache);
        sb.append('}');
        return sb.toString();
    }
}
