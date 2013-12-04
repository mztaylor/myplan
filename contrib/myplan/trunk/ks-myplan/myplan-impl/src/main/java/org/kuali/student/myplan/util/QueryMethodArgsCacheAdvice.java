package org.kuali.student.myplan.util;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.ObjectExistsException;
import org.aopalliance.aop.Advice;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.student.common.util.spring.MethodArgsToObjectEhcacheAdvice;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends the methodArgsCacheAdvice to be able to cache searchRequests based on same QueryByCriteria objects
 * <p/>
 * TODO: This class should be removed when https://jira.kuali.org/browse/KULRICE-6988 is fixed
 *
 * @Author kmuthu
 * Date: 3/22/12
 */
public class QueryMethodArgsCacheAdvice extends MethodArgsToObjectEhcacheAdvice {
    final Logger LOG = Logger.getLogger(getClass());

    private CacheManager cacheManager;
    private String cacheName;
    private boolean enabled;

    @Override
    public Object getFromCache(ProceedingJoinPoint pjp) throws Throwable {
        if (!enabled) {
            return pjp.proceed();
        }

        if (cacheManager == null) {
            cacheManager = CacheManager.getInstance();
            try {
                cacheManager.addCache(cacheName);
            } catch (ObjectExistsException e) {

            }
        }
        MultiKey cacheKey = getCacheKey(pjp);

        Element cachedResult = cacheManager.getCache(cacheName).get(cacheKey);
        Object result = null;
        if (cachedResult == null) {
            result = pjp.proceed();
            LOG.info("Storing to Cache: " + cacheName);
            cacheManager.getCache(cacheName).put(new Element(cacheKey, result));
        } else {
            LOG.info("Found in Cache: " + cacheName);
            result = cachedResult.getValue();
        }

        return result;
    }

    private MultiKey getCacheKey(ProceedingJoinPoint pjp) {
        final StringBuffer cacheKey = new StringBuffer(pjp.getSignature().getName());
        cacheKey.append("(");
        for (int i = 0; i < pjp.getArgs().length; i++) {

            if (null == pjp.getArgs()[i]) {
                cacheKey.append("<null>");
            } else {

                // Parse through the QueryByCriteria to establish same key for same criteria values
                if (pjp.getArgs()[i] instanceof QueryByCriteria) {

                    QueryByCriteria qbc = (QueryByCriteria) pjp.getArgs()[i];
                    cacheKey.append(qbc.getPredicate().toString());

                } else {

                    cacheKey.append(pjp.getArgs()[i].toString());

                }
            }

            if (i + 1 != pjp.getArgs().length) {
                cacheKey.append(",");
            }

        }

        cacheKey.append(")");
        List<String> keyList = new ArrayList<String>();
        keyList.add(cacheKey.toString());
        return new MultiKey(keyList.toArray());
    }

    /**
     * @return the cacheName
     */
    public String getCacheName() {
        return cacheName;
    }

    /**
     * @param cacheName the cacheName to set
     */
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}