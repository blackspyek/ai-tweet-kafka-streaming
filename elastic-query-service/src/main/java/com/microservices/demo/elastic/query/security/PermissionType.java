package com.microservices.demo.elastic.query.security;

public enum PermissionType {
    READ("READ"), WRITE("WRITE"), ADMIN("ADMIN");
    private String type;
    PermissionType(String value) {
        this.type = value;
    }
    public String getType() {
        return type;
    }
}
