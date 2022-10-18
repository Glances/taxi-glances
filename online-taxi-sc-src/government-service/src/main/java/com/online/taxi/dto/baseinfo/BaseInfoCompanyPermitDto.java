package com.online.taxi.dto.baseinfo;

import java.util.Date;

/**
 * 网约车平台公司经营许可证
 *
 */
public class BaseInfoCompanyPermitDto {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_base_info_company_permit.id
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * 网络预约出租汽车经营许可证号
     */
    private String certificate;

    /**
     * 经营区域
     */
    private String operationArea;

    /**
     * 公司名称
     */
    private String ownerName;

    /**
     * 发证机构名称
     */
    private String organization;

    /**
     * 有效期起
     */
    private Date startDate;

    /**
     * 有效期止
     */
    private Date stopDate;

    /**
     * 初次发证日期
     */
    private Date certifyDate;

    /**
     * 证照状态
     */
    private String state;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.id
     *
     * @return the value of tbl_base_info_company_permit.id
     * @mbggenerated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.id
     *
     * @param id the value for tbl_base_info_company_permit.id
     * @mbggenerated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.certificate
     *
     * @return the value of tbl_base_info_company_permit.certificate
     * @mbggenerated
     */
    public String getCertificate() {
        return certificate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.certificate
     *
     * @param certificate the value for tbl_base_info_company_permit.certificate
     * @mbggenerated
     */
    public void setCertificate(String certificate) {
        this.certificate = certificate == null ? null : certificate.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.operation_area
     *
     * @return the value of tbl_base_info_company_permit.operation_area
     * @mbggenerated
     */
    public String getOperationArea() {
        return operationArea;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.operation_area
     *
     * @param operationArea the value for tbl_base_info_company_permit.operation_area
     * @mbggenerated
     */
    public void setOperationArea(String operationArea) {
        this.operationArea = operationArea == null ? null : operationArea.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.owner_name
     *
     * @return the value of tbl_base_info_company_permit.owner_name
     * @mbggenerated
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.owner_name
     *
     * @param ownerName the value for tbl_base_info_company_permit.owner_name
     * @mbggenerated
     */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName == null ? null : ownerName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.organization
     *
     * @return the value of tbl_base_info_company_permit.organization
     * @mbggenerated
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.organization
     *
     * @param organization the value for tbl_base_info_company_permit.organization
     * @mbggenerated
     */
    public void setOrganization(String organization) {
        this.organization = organization == null ? null : organization.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.start_date
     *
     * @return the value of tbl_base_info_company_permit.start_date
     * @mbggenerated
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.start_date
     *
     * @param startDate the value for tbl_base_info_company_permit.start_date
     * @mbggenerated
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.stop_date
     *
     * @return the value of tbl_base_info_company_permit.stop_date
     * @mbggenerated
     */
    public Date getStopDate() {
        return stopDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.stop_date
     *
     * @param stopDate the value for tbl_base_info_company_permit.stop_date
     * @mbggenerated
     */
    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.certify_date
     *
     * @return the value of tbl_base_info_company_permit.certify_date
     * @mbggenerated
     */
    public Date getCertifyDate() {
        return certifyDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.certify_date
     *
     * @param certifyDate the value for tbl_base_info_company_permit.certify_date
     * @mbggenerated
     */
    public void setCertifyDate(Date certifyDate) {
        this.certifyDate = certifyDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.state
     *
     * @return the value of tbl_base_info_company_permit.state
     * @mbggenerated
     */
    public String getState() {
        return state;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.state
     *
     * @param state the value for tbl_base_info_company_permit.state
     * @mbggenerated
     */
    public void setState(String state) {
        this.state = state == null ? null : state.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.create_time
     *
     * @return the value of tbl_base_info_company_permit.create_time
     * @mbggenerated
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.create_time
     *
     * @param createTime the value for tbl_base_info_company_permit.create_time
     * @mbggenerated
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_base_info_company_permit.update_time
     *
     * @return the value of tbl_base_info_company_permit.update_time
     * @mbggenerated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_base_info_company_permit.update_time
     *
     * @param updateTime the value for tbl_base_info_company_permit.update_time
     * @mbggenerated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
