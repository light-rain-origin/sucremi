package com.sucremi.project.dto;

/**
 * 项目列表查询参数。
 *
 * <ul>
 *   <li>{@code archived}：false=仅未归档（默认），true=仅归档。</li>
 *   <li>{@code q}：按名称/简介模糊搜索。</li>
 *   <li>{@code status}：按状态过滤。</li>
 *   <li>{@code sort}：形如 {@code updatedAt,desc}；字段受白名单限制。</li>
 * </ul>
 */
public class ProjectQuery {

    private String q;
    private String status;
    private boolean archived = false;
    private int page = 1;
    private int size = 20;
    private String sort = "updatedAt,desc";

    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
