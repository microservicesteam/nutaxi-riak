package com.msteam.aws.model;

public enum InstanceStateCode {

    PENDING(0, "Pending"),
    RUNNING(16, "Running"),
    SHUTTING_DOWN(32, "Shutting down"),
    TERMINATED(48, "Terminated"),
    STOPPING(64, "Stopping"),
    STOPPED(80, "Stopped");

    public final int statusCode;
    public final String name;

    InstanceStateCode(int statusCode, String name) {
        this.statusCode = statusCode;
        this.name = name;
    }
}
