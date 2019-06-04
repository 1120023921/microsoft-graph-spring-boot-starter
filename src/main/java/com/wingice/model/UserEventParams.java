package com.wingice.model;

import java.time.ZoneId;

/**
 * @author 胡昊
 * Description: 查询用户事件参数
 * Date: 2019/5/23
 * Time: 19:57
 * Create: DoubleH
 */
public class UserEventParams {

    /**
     * 用户名
     */
    private String userPrincipalName;
    /**
     * 开始时间
     */
    private Long start;
    /**
     * 结束时间
     */
    private Long end;
    /**
     * 当前页
     */
    private Integer pageNum;
    /**
     * 页大小
     */
    private Integer pageSize;
    /**
     * 时区
     */
    private String timezone = ZoneId.systemDefault().getId();
    /**
     * 内容的类型。 可能的值为 text 和 HTML
     */
    private String contentType = "HTML";

    /**
     * 是否是组织者
     */
    private String isOrganizer;

    /**
     * 是否取消
     */
    private String isCancelled;

    /**
     * 排序
     */
    private String orderBy;

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getIsOrganizer() {
        return isOrganizer;
    }

    public void setIsOrganizer(String isOrganizer) {
        this.isOrganizer = isOrganizer;
    }

    public String getIsCancelled() {
        return isCancelled;
    }

    public void setIsCancelled(String isCancelled) {
        this.isCancelled = isCancelled;
    }
}
