package org.kuali.student.myplan.course.util;

/**
 * Defines the type of list that should be rendered in a CollectionListFormatter.
 */
public enum CollectionListFormatterHtmlListType {

    /** Output an unordered list. */
    UL("ul", "li"),
    /** Output an ordered list. */
    OL("ol","li"),
    /** Output a data definition list. */
    DD("dl", "dd");

    private String listElementName;
    private String itemElementName;

    CollectionListFormatterHtmlListType(String listElementName, String itemElementName) {
        this.listElementName = listElementName;
        this.itemElementName = itemElementName;
    }

    public String getListElementName() {
        return this.listElementName;
    }

    public String getListItemElementName() {
        return this.itemElementName;
    }
}