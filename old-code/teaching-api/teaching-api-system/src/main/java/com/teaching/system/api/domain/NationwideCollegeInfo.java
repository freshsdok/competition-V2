package com.teaching.system.api.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 全国院校信息对象 nationwide_college_info
 *
 * @author teaching
 * @date 2025-12-03
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NationwideCollegeInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 序号
     */
    private String id;

    /**
     * 学校名称
     */
    @Excel(name = "学校名称")
    private String schoolName;

    /**
     * 学校标识码
     */
    @Excel(name = "学校标识码")
    private String schoolIdentificationCode;

    /**
     * 主管部门
     */
    @Excel(name = "主管部门")
    private String competentDepartment;

    /**
     * 省
     */
    @Excel(name = "省")
    private String province;

    /**
     * 市
     */
    @Excel(name = "市")
    private String city;

    /**
     * 省份code
     */
    @Excel(name = "省份code")
    private String provinceCode;

    /**
     * 市编码code
     */
    @Excel(name = "市编码code")
    private String cityCode;

    /**
     * 办学层次
     */
    @Excel(name = "办学层次")
    private String educationalLevel;

    /**
     * 备注
     */
    @Excel(name = "备注")
    private String remak;

    /**
     * 官网网址
     */
    @Excel(name = "官网网址")
    private String officialWebsiteAddress;

    /**
     * 是否有国际学院
     */
    @Excel(name = "是否有国际学院")
    private String internationalAcademy;

    /**
     * 国际学院网址
     */
    @Excel(name = "国际学院网址")
    private String internationalCollegeWebsite;

    /**
     * 985
     */
    @Excel(name = "985")
    private String nineEightFive;

    /**
     * 211
     */
    @Excel(name = "211")
    private String twoOneOne;

    /**
     * 双一流
     */
    @Excel(name = "双一流")
    private String doubleFirstClassUniversityPlan;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolIdentificationCode(String schoolIdentificationCode) {
        this.schoolIdentificationCode = schoolIdentificationCode;
    }

    public String getSchoolIdentificationCode() {
        return schoolIdentificationCode;
    }

    public void setCompetentDepartment(String competentDepartment) {
        this.competentDepartment = competentDepartment;
    }

    public String getCompetentDepartment() {
        return competentDepartment;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvince() {
        return province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setEducationalLevel(String educationalLevel) {
        this.educationalLevel = educationalLevel;
    }

    public String getEducationalLevel() {
        return educationalLevel;
    }

    public void setRemak(String remak) {
        this.remak = remak;
    }

    public String getRemak() {
        return remak;
    }

    public void setOfficialWebsiteAddress(String officialWebsiteAddress) {
        this.officialWebsiteAddress = officialWebsiteAddress;
    }

    public String getOfficialWebsiteAddress() {
        return officialWebsiteAddress;
    }

    public void setInternationalAcademy(String internationalAcademy) {
        this.internationalAcademy = internationalAcademy;
    }

    public String getInternationalAcademy() {
        return internationalAcademy;
    }

    public void setInternationalCollegeWebsite(String internationalCollegeWebsite) {
        this.internationalCollegeWebsite = internationalCollegeWebsite;
    }

    public String getInternationalCollegeWebsite() {
        return internationalCollegeWebsite;
    }

    public void setNineEightFive(String nineEightFive) {
        this.nineEightFive = nineEightFive;
    }

    public String getNineEightFive() {
        return nineEightFive;
    }

    public void setTwoOneOne(String twoOneOne) {
        this.twoOneOne = twoOneOne;
    }

    public String getTwoOneOne() {
        return twoOneOne;
    }

    public void setDoubleFirstClassUniversityPlan(String doubleFirstClassUniversityPlan) {
        this.doubleFirstClassUniversityPlan = doubleFirstClassUniversityPlan;
    }

    public String getDoubleFirstClassUniversityPlan() {
        return doubleFirstClassUniversityPlan;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("schoolName", getSchoolName())
                .append("schoolIdentificationCode", getSchoolIdentificationCode())
                .append("competentDepartment", getCompetentDepartment())
                .append("province", getProvince())
                .append("city", getCity())
                .append("provinceCode", getProvinceCode())
                .append("cityCode", getCityCode())
                .append("educationalLevel", getEducationalLevel())
                .append("remak", getRemak())
                .append("officialWebsiteAddress", getOfficialWebsiteAddress())
                .append("internationalAcademy", getInternationalAcademy())
                .append("internationalCollegeWebsite", getInternationalCollegeWebsite())
                .append("nineEightFive", getNineEightFive())
                .append("twoOneOne", getTwoOneOne())
                .append("doubleFirstClassUniversityPlan", getDoubleFirstClassUniversityPlan())
                .toString();
    }
}
