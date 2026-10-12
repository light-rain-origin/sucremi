package com.sucremi.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 项目实体，映射 project 表。
 *
 * <p>{@code userId} 为数据归属字段，所有读写都应按当前用户过滤（见 ProjectServiceImpl）。
 * {@code createdAt}/{@code updatedAt} 由数据库默认值与 ON UPDATE 维护，不在应用侧写入。</p>
 */
@Getter
@Setter
@TableName("project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String summary;

    private String goal;

    private String status;

    private Integer progress;

    private LocalDateTime archivedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
