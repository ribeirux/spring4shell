package org.spring4shell.controller.dto;

import org.spring4shell.util.StackUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

public class ComplexObject {

    private Map<String, UUID> ivMap = new HashMap<>();

    public Map<String, UUID> getMap() {
        StackUtils.logCallerMethod();
        return ivMap;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ComplexObject.class.getSimpleName() + "[", "]")
            .add("map=" + ivMap)
            .toString();
    }
}
