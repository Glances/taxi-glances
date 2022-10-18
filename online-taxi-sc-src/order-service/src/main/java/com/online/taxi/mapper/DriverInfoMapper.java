package com.online.taxi.mapper;

import com.online.taxi.entity.DriverInfo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 */
@Service
public interface DriverInfoMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_driver_info
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_driver_info
     *
     * @mbggenerated
     */
    int insert(DriverInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_driver_info
     *
     * @mbggenerated
     */
    int insertSelective(DriverInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_driver_info
     *
     * @mbggenerated
     */
    DriverInfo selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_driver_info
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(DriverInfo record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_driver_info
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(DriverInfo record);

    List<DriverInfo> selectDriverInfoList();
}
