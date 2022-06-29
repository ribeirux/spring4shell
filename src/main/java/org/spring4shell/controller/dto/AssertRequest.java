package org.spring4shell.controller.dto;

import org.spring4shell.util.StackUtils;

import java.util.StringJoiner;

public class AssertRequest {

    private String ivName;

    public AssertRequest() {
        StackUtils.logCallerMethod();
    }

    public String getName() {
        StackUtils.logCallerMethod();
        return ivName;
    }

    public void setName(final String name) {
        StackUtils.logCallerMethod();
        this.ivName = name;
    }

    public void assertName(final String name) {
        StackUtils.logCallerMethod();
        assert this.ivName.equals(name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AssertRequest.class.getSimpleName() + "[", "]")
            .add("name='" + ivName + "'")
            .toString();
    }
}
