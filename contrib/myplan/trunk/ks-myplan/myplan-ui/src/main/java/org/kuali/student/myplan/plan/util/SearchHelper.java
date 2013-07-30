package org.kuali.student.myplan.plan.util;


import org.kuali.student.common.search.dto.SearchResultCell;
import org.kuali.student.common.search.dto.SearchResultRow;

public class SearchHelper {
  /**
     * Returns the value for the SearchResultCell in the SearchResultRow comparing with given key
     *
     * @param row
     * @param key
     * @return
     */
    public static String getCellValue(SearchResultRow row, String key) {
        for (SearchResultCell cell : row.getCells()) {
            if (key.equals(cell.getKey())) {
                return cell.getValue();
            }
        }
        throw new RuntimeException("cell result '" + key + "' not found");
    }
}