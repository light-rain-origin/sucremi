package com.sucremi.decision;

import com.sucremi.common.api.PageResult;
import com.sucremi.decision.dto.DecisionRequest;
import com.sucremi.decision.dto.DecisionResponse;
import com.sucremi.decision.dto.DecisionRevisionResponse;
import com.sucremi.decision.dto.KeyDecisionResponse;
import com.sucremi.decision.dto.SupersedeDecisionRequest;

import java.util.List;

/** 项目决策管理、版本与替代业务服务。 */
public interface DecisionService {
    /** 分页查询项目决策。 */
    PageResult<DecisionResponse> list(long projectId, int page, int size, DecisionStatus status,
                                      String sortBy, String direction);
    /** 创建项目决策。 */
    DecisionResponse create(long projectId, DecisionRequest request);
    /** 读取项目决策详情。 */
    DecisionResponse get(long projectId, long decisionId);
    /** 编辑项目决策并保留完整历史版本。 */
    DecisionResponse update(long projectId, long decisionId, DecisionRequest request);
    /** 查询项目决策的不可变历史版本。 */
    PageResult<DecisionRevisionResponse> revisions(long projectId, long decisionId, int page, int size);
    /** 原子创建新决策并替代指定旧决策。 */
    DecisionResponse supersede(long projectId, long decisionId, SupersedeDecisionRequest request);
    /** 软删除提议中的误创建决策。 */
    void delete(long projectId, long decisionId);
    /** 返回恢复页使用的最多三条当前有效决策。 */
    List<KeyDecisionResponse> keyDecisions(long projectId);
}
