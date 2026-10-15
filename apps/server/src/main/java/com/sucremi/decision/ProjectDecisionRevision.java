package com.sucremi.decision;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 决策的不可变历史快照实体。 */
@Getter
@Setter
@TableName("project_decision_revision")
public class ProjectDecisionRevision {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;
    private Long decisionId;
    private Integer revisionNumber;
    private String snapshotJson;
    private String changeSummary;
    private LocalDateTime createdAt;
}
