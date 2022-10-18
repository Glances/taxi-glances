package com.online.taxi.mapper;

import com.online.taxi.entity.PassengerInfo;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 * @date 2018/8/25
 */
@Service
public interface PassengerInfoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_passenger_info
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_passenger_info
     *
     * @mbggenerated
     */
    int insert(PassengerInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_passenger_info
     *
     * @mbggenerated
     */
    int insertSelective(PassengerInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_passenger_info
     *
     * @mbggenerated
     */
    PassengerInfo selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_passenger_info
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(PassengerInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_passenger_info
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(PassengerInfo record);
}
