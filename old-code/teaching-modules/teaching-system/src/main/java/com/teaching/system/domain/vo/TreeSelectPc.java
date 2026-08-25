package com.teaching.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.constant.UserConstants;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.system.api.domain.SysOrg;
import com.teaching.system.domain.SysMenu;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Treeselect树结构实体类
 *
 * @author teaching
 */
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TreeSelectPc implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    private Long id;

    /**
     * 节点名称
     */
    private String label;

    /**
     * 节点类型
     */
    private String menuType;

    private String dataScope;

    /**
     * 节点禁用
     */
    private boolean disabled = false;

    private String path;

    /**
     * 子节点
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelectPc> children;


    private String columnId;
    private String columnName;
    private String columnCode;
    private String columnType;
    private String fileName;
    private String fileUrl;
    private String fileType;

    public TreeSelectPc() {

    }

    public TreeSelectPc(SysOrg org) {
        this.id = org.getOrgId();
        this.label = org.getOrgName();
        this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, org.getStatus());
        this.children = org.getChildren().stream().map(TreeSelectPc::new).collect(Collectors.toList());
    }

    public TreeSelectPc(SysMenu menu) {
        this.id = menu.getMenuId();
        this.label = menu.getMenuName();
        this.menuType = menu.getMenuType();
        this.dataScope = menu.getDataScope();
        this.path = menu.getPath();
        this.columnId = menu.getColumnId();
        this.columnName = menu.getColumnName();
        this.columnCode = menu.getColumnCode();
        this.columnType = menu.getColumnType();
        this.fileName = menu.getFileName();
        this.fileUrl = menu.getFileUrl();
        this.fileType = menu.getFileType();
        this.children = menu.getChildren().stream().map(TreeSelectPc::new).collect(Collectors.toList());
    }


    public String getColumnId() {
        return columnId;
    }

    public void setColumnId(String columnId) {
        this.columnId = columnId;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnCode() {
        return columnCode;
    }

    public void setColumnCode(String columnCode) {
        this.columnCode = columnCode;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<TreeSelectPc> getChildren() {
        return children;
    }

    public void setChildren(List<TreeSelectPc> children) {
        this.children = children;
    }

    public String getMenuType() {
        return menuType;
    }

    public void setMenuType(String menuType) {
        this.menuType = menuType;
    }

    public String getDataScope() {
        return dataScope;
    }

    public void setDataScope(String dataScope) {
        this.dataScope = dataScope;
    }
}
