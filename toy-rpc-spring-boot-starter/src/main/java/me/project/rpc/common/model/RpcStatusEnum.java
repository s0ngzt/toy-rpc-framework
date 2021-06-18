package me.project.rpc.common.model;

public enum RpcStatusEnum {
    SUCCESS(200, "SUCCESS"),
    ERROR(500, "ERROR"),
    NOT_FOUND(404, "NOT FOUND");

    private final int code;
    private final String description;

    RpcStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
