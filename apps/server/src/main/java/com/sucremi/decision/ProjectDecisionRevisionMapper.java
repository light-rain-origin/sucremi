package com.sucremi.decision;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 决策历史版本数据访问接口。 */
@Mapper
public interface ProjectDecisionRevisionMapper extends BaseMapper<ProjectDecisionRevision> {
}
