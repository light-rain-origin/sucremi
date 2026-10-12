package com.sucremi.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 项目状态白名单校验（纯 POJO）。防止非法状态写库。
 */
class ProjectStatusTest {

    @Test
    void acceptsKnownStatuses() {
        assertTrue(ProjectStatus.isValid("ACTIVE"));
        assertTrue(ProjectStatus.isValid("PAUSED"));
        assertTrue(ProjectStatus.isValid("DONE"));
    }

    @Test
    void rejectsNullOrUnknown() {
        assertFalse(ProjectStatus.isValid(null));
        assertFalse(ProjectStatus.isValid(""));
        assertFalse(ProjectStatus.isValid("NOPE"));
        assertFalse(ProjectStatus.isValid("active"));
    }
}
