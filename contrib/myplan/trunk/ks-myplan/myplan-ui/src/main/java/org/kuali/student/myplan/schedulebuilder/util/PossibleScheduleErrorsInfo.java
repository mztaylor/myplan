package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleErrors;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 3/27/2014.
 */
public class PossibleScheduleErrorsInfo implements PossibleScheduleErrors {

    private String errorType;

    private Map<String, Map<String, List<String>>> invalidOptions;


    @Override
    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    @Override

    public Map<String, Map<String, List<String>>> getInvalidOptions() {
        return invalidOptions;
    }

    public void setInvalidOptions(Map<String, Map<String, List<String>>> invalidOptions) {
        this.invalidOptions = invalidOptions;
    }
}
