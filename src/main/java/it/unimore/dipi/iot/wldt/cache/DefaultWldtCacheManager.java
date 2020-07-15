package it.unimore.dipi.iot.wldt.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimore.dipi.iot.wldt.exception.WldtCacheException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 09/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class DefaultWldtCacheManager implements IWldtCacheManager<String,Object> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWldtCacheManager.class);

    private static DefaultWldtCacheManager instance = null;

    private ConcurrentHashMap<String, Cache<String, Object>> cacheMap = null;

    private DefaultWldtCacheManager() {
        this.cacheMap = new ConcurrentHashMap<>();
    }

    public static DefaultWldtCacheManager getInstance(){

        if(instance == null)
            instance = new DefaultWldtCacheManager();

        return instance;
    }

    @Override
    public void initCache(String cacheIdentifier, long expirationTimeValue, TimeUnit timeUnit,  int maximumSize) throws WldtCacheException {

        try{

            if(this.cacheMap.get(cacheIdentifier) != null)
                throw new WldtCacheException("Cache already initialized !");

            @NonNull Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();

            if(expirationTimeValue > 0)
                cacheBuilder.expireAfterWrite(expirationTimeValue, timeUnit);

            if(maximumSize > 0)
                cacheBuilder.maximumSize(maximumSize);

            this.cacheMap.put(cacheIdentifier, cacheBuilder.build());

        }catch (Exception e){
            logger.error("Error initializing cache ! Error: {}", e.getLocalizedMessage());
            throw new WldtCacheException(String.format("Error initializing cache ! Error: %s", e.getLocalizedMessage()));
        }
    }

    @Override
    public void deleteCache(String cacheIdentifier) throws WldtCacheException {

        if(this.cacheMap.get(cacheIdentifier) == null)
            throw new WldtCacheException(String.format("Cache %s not found !", cacheIdentifier));

        this.cacheMap.remove(cacheIdentifier);
    }

    @Override
    public void addData(String cacheIdentifier, String key, Object value) throws WldtCacheException {

        if(this.cacheMap.get(cacheIdentifier) == null)
            throw new WldtCacheException(String.format("Cache %s not found !", cacheIdentifier));

        this.cacheMap.get(cacheIdentifier).put(key, value);
    }

    @Override
    public Optional<Object> getData(String cacheIdentifier, String key) throws WldtCacheException {

        if(this.cacheMap.get(cacheIdentifier) == null)
            throw new WldtCacheException(String.format("Cache %s not found !", cacheIdentifier));

        return Optional.ofNullable(this.cacheMap.get(cacheIdentifier).getIfPresent(key));
    }

    public static void main(String[] args) {

        try {

            String myCacheId = "mycacheid";
            String testKey = "test";

            /*
            DefaultCacheManager.getInstance().initCache(myCacheId, 0, TimeUnit.SECONDS, 0);
            DefaultCacheManager.getInstance().addData(myCacheId, testKey, "test");

            if(DefaultCacheManager.getInstance().getData(myCacheId, "test").isPresent())
                logger.info("Cached Data ({}): {}", testKey, DefaultCacheManager.getInstance().getData(myCacheId, "test").get());
            else
                logger.error("Cached Data ({}) not found !", testKey);
            */

            DefaultWldtCacheManager.getInstance().initCache(myCacheId, 10, TimeUnit.SECONDS, 0);
            DefaultWldtCacheManager.getInstance().addData(myCacheId, testKey, "test");

            for(int i=0; i<11; i++){

                Thread.sleep(1000);

                if(DefaultWldtCacheManager.getInstance().getData(myCacheId, "test").isPresent())
                    logger.info("Cached Data ({}): {}", testKey, DefaultWldtCacheManager.getInstance().getData(myCacheId, "test").get());
                else
                    logger.error("Cached Data ({}) not found !", testKey);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
