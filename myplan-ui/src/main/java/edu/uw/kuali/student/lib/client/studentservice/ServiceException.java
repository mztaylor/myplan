package edu.uw.kuali.student.lib.client.studentservice;

public class ServiceException
        extends Exception {

    private static final long serialVersionUID = 1L;

    public ServiceException(String s) {
        super(s);
    }

    public ServiceException(String s, Exception e) {
        super(s, e);
    }
}