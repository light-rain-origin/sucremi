package com.sucremi.recovery;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 项目下一步行动实体，对应 {@code project_action} 表。
 *
 * <p>每条行动同时记录项目与用户归属；业务查询和更新必须按当前用户过滤。</p>
 */
@Getter
@Setter
@TableName("project_action")
public class ProjectAction {
    /** 行动主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属用户 ID。 */
    private Long userId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 行动标题。 */
    private String title;
    /** 行动补充说明。 */
    private String details;
    /** 行动状态：TODO 或 DONE。 */
    private String status;
    /** 项目内显示顺序，数值越小越靠前。 */
    private Integer sortOrder;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最近更新时间。 */
    private LocalDateTime updatedAt;
    /** 完成时间；未完成时为空。 */
    private LocalDateTime completedAt;
}
