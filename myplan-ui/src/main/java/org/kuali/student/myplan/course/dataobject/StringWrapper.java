package org.kuali.student.myplan.course.dataobject;

/**
 * Wrapper so that String can be used as CollectionGroup value.
 */
public class StringWrapper {

    private String string;

    public StringWrapper() {}

    public StringWrapper(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
