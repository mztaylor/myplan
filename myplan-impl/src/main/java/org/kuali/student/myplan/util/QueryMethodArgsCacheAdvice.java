package org.kuali.student.myplan.util;

import org.apache.commons.collections.keyvalue.MultiKey;
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

    private MultiKey getCacheKey(ProceedingJoinPoint pjp) {
		final StringBuffer cacheKey = new StringBuffer(pjp.getSignature().getName());
		cacheKey.append("(");
		for (int i = 0; i < pjp.getArgs().length; i++) {

			if(null == pjp.getArgs()[i]) {
				cacheKey.append("<null>");
			} else {

                // Parse through the QueryByCriteria to establish same key for same criteria values
                if( pjp.getArgs()[i] instanceof QueryByCriteria) {

                    QueryByCriteria qbc = (QueryByCriteria) pjp.getArgs()[i];
                    cacheKey.append(qbc.getPredicate().toString());

                }   else {

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
}
