package com.sucremi.recovery;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 项目开发检查点实体，对应 {@code project_checkpoint} 表。
 *
 * <p>检查点是用于恢复工作的历史快照，创建后不覆盖旧记录。</p>
 */
@Getter
@Setter
@TableName("project_checkpoint")
public class ProjectCheckpoint {
    /** 检查点主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 所属用户 ID。 */
    private Long userId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 用户记录的开发状态摘要。 */
    private String summary;
    /** 保存检查点时正在处理的事项。 */
    private String currentFocus;
    /** 保存检查点时遇到的阻塞事项。 */
    private String blockers;
    /** 检查点创建时间。 */
    private LocalDateTime createdAt;
}
