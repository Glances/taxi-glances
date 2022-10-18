package com.online.taxi.entity;

import java.util.Date;

public class LargeScreenPassenger {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_large_screen_passenger.id
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_large_screen_passenger.passenger_info_id
     *
     * @mbggenerated
     */
    private Integer passengerInfoId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_large_screen_passenger.device_code
     *
     * @mbggenerated
     */
    private String deviceCode;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_large_screen_passenger.login_time
     *
     * @mbggenerated
     */
    private Date loginTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_large_screen_passenger.logout_time
     *
     * @mbggenerated
     */
    private Date logoutTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_large_screen_passenger.login_status
     *
     * @mbggenerated
     */
    private Integer loginStatus;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_large_screen_passenger.repair_time
     *
     * @mbggenerated
     */
    private Date repairTime;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_large_screen_passenger.id
     *
     * @return the value of tbl_large_screen_passenger.id
     *
     * @mbggenerated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_large_screen_passenger.id
     *
     * @param id the value for tbl_large_screen_passenger.id
     *
     * @mbggenerated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_large_screen_passenger.passenger_info_id
     *
     * @return the value of tbl_large_screen_passenger.passenger_info_id
     *
     * @mbggenerated
     */
    public Integer getPassengerInfoId() {
        return passengerInfoId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_large_screen_passenger.passenger_info_id
     *
     * @param passengerInfoId the value for tbl_large_screen_passenger.passenger_info_id
     *
     * @mbggenerated
     */
    public void setPassengerInfoId(Integer passengerInfoId) {
        this.passengerInfoId = passengerInfoId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_large_screen_passenger.device_code
     *
     * @return the value of tbl_large_screen_passenger.device_code
     *
     * @mbggenerated
     */
    public String getDeviceCode() {
        return deviceCode;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_large_screen_passenger.device_code
     *
     * @param deviceCode the value for tbl_large_screen_passenger.device_code
     *
     * @mbggenerated
     */
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode == null ? null : deviceCode.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_large_screen_passenger.login_time
     *
     * @return the value of tbl_large_screen_passenger.login_time
     *
     * @mbggenerated
     */
    public Date getLoginTime() {
        return loginTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_large_screen_passenger.login_time
     *
     * @param loginTime the value for tbl_large_screen_passenger.login_time
     *
     * @mbggenerated
     */
    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_large_screen_passenger.logout_time
     *
     * @return the value of tbl_large_screen_passenger.logout_time
     *
     * @mbggenerated
     */
    public Date getLogoutTime() {
        return logoutTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_large_screen_passenger.logout_time
     *
     * @param logoutTime the value for tbl_large_screen_passenger.logout_time
     *
     * @mbggenerated
     */
    public void setLogoutTime(Date logoutTime) {
        this.logoutTime = logoutTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_large_screen_passenger.login_status
     *
     * @return the value of tbl_large_screen_passenger.login_status
     *
     * @mbggenerated
     */
    public Integer getLoginStatus() {
        return loginStatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_large_screen_passenger.login_status
     *
     * @param loginStatus the value for tbl_large_screen_passenger.login_status
     *
     * @mbggenerated
     */
    public void setLoginStatus(Integer loginStatus) {
        this.loginStatus = loginStatus;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column tbl_large_screen_passenger.repair_time
     *
     * @return the value of tbl_large_screen_passenger.repair_time
     *
     * @mbggenerated
     */
    public Date getRepairTime() {
        return repairTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column tbl_large_screen_passenger.repair_time
     *
     * @param repairTime the value for tbl_large_screen_passenger.repair_time
     *
     * @mbggenerated
     */
    public void setRepairTime(Date repairTime) {
        this.repairTime = repairTime;
    }
}