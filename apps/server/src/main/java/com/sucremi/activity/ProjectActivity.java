package com.sucremi.activity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 项目活动实体，仅保存版本化的轻量事件元数据。
 */
@Getter
@Setter
@TableName("project_activity")
public class ProjectActivity {
    private static final int CURRENT_METADATA_VERSION = 1;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;
    private String eventType;
    private Integer metadataVersion;
    private String metadata;
    private LocalDateTime createdAt;

    /** 返回当前元数据结构版本。 */
    public static int currentMetadataVersion() {
        return CURRENT_METADATA_VERSION;
    }
}
