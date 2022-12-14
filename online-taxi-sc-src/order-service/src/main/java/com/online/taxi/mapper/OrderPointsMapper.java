package com.online.taxi.mapper;

import com.online.taxi.entity.OrderPoints;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @date 2018/9/14
 */
@Service
public interface OrderPointsMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_points
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_points
     *
     * @mbggenerated
     */
    int insert(OrderPoints record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_points
     *
     * @mbggenerated
     */
    int insertSelective(OrderPoints record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_points
     *
     * @mbggenerated
     */
    OrderPoints selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_points
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(OrderPoints record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_points
     *
     * @mbggenerated
     */
    int updateByPrimaryKeyWithBLOBs(OrderPoints record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_points
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(OrderPoints record);
}
