package com.teaching.system.domain;

import com.teaching.common.core.web.domain.BaseEntity;

import java.util.List;

// 专家用户对象
public class SpecialistSysUser extends BaseEntity{

    private static final long serialVersionUID = 1L;

    private Long userId;

    private String userName;

    private String nickName;

    //学校id
    private String school;

    //学校名称
    private String schoolName;

    /**
     * 所在学院
     */
    private String institute;

    /**
     * 职务
     */
    private String position;

    /** 省份名称 */
    private String province;

    /** 省份编码 */
    private String provinceCode;

    /** 头像 */
    private String avatar;

    /** 专家姓名及学院搜索 */
    private String keyWords;

    /**
     * 分配状态
     */
    private String distributeStatus;

    private List<ReviewTaskInfo> reviewTaskInfoList;

    public List<ReviewTaskInfo> getReviewTaskInfoList() {
        return reviewTaskInfoList;
    }

    public void setReviewTaskInfoList(List<ReviewTaskInfo> reviewTaskInfoList) {
        this.reviewTaskInfoList = reviewTaskInfoList;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public String getDistributeStatus() {
        return distributeStatus;
    }

    public void setDistributeStatus(String distributeStatus) {
        this.distributeStatus = distributeStatus;
    }
}
