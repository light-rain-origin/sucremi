package com.sucremi.project;

/**
 * 项目状态枚举。持久化为字符串（见 project.status）。
 */
public enum ProjectStatus {
    ACTIVE,
    PAUSED,
    DONE;

    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        for (ProjectStatus s : values()) {
            if (s.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
