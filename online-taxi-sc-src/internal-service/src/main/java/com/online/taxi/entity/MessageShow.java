package com.online.taxi.entity;

import lombok.Data;

import java.util.Date;

@Data
public class MessageShow {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.id
     *
     * @mbggenerated
     */
    private Integer id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.title
     *
     * @mbggenerated
     */
    private String title;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.content
     *
     * @mbggenerated
     */
    private String content;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.yid
     *
     * @mbggenerated
     */
    private String yid;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.show_type
     *
     * @mbggenerated
     */
    private Integer acceptIdentity;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.send_time
     *
     * @mbggenerated
     */
    private Date sendTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.push_type
     *
     * @mbggenerated
     */
    private Integer pushType;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.status
     *
     * @mbggenerated
     */
    private Integer status;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.order_id
     *
     * @mbggenerated
     */
    private Integer orderId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.sms_send_app_id
     *
     * @mbggenerated
     */
    private Integer smsSendAppId;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column tbl_message_show.create_time
     *
     * @mbggenerated
     */
    private Date createTime;

}