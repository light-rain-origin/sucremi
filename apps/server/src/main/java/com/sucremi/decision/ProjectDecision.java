package com.sucremi.decision;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 项目决策当前状态实体。 */
@Getter
@Setter
@TableName("project_decision")
public class ProjectDecision {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long projectId;
    private String title;
    private String context;
    private String optionsJson;
    private String decision;
    private String rationale;
    private String consequences;
    private String revisitCondition;
    private String status;
    private Long supersededById;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
