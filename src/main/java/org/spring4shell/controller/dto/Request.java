package org.spring4shell.controller.dto;

import org.spring4shell.util.StackUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

public class Request {
    private String ivName;
    private List<UUID> ivList = new ArrayList<>();
    private ComplexObject ivComplexObject = new ComplexObject();

    public Request() {
        StackUtils.logCallerMethod();
    }

    public String getName() {
        StackUtils.logCallerMethod();
        return ivName;
    }

    public void setName(final String ivName) {
        StackUtils.logCallerMethod();
        this.ivName = ivName;
    }

    public List<UUID> getList() {
        StackUtils.logCallerMethod();
        return ivList;
    }

    public ComplexObject getComplexObject() {
        StackUtils.logCallerMethod();
        return ivComplexObject;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Request.class.getSimpleName() + "[", "]")
            .add("name='" + ivName + "'")
            .add("list=" + ivList)
            .add("complexObject=" + ivComplexObject)
            .toString();
    }
}
