package com.online.taxi.mapper;

import com.online.taxi.entity.OrderRulePriceTag;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderRulePriceTagMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_rule_price_tag
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_rule_price_tag
     *
     * @mbggenerated
     */
    int insert(OrderRulePriceTag record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_rule_price_tag
     *
     * @mbggenerated
     */
    int insertSelective(OrderRulePriceTag record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_rule_price_tag
     *
     * @mbggenerated
     */
    OrderRulePriceTag selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_rule_price_tag
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(OrderRulePriceTag record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_order_rule_price_tag
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(OrderRulePriceTag record);

    /**
     * This method corresponds to the database table tbl_order_rule_price_tag
     */
    int insertList(List<OrderRulePriceTag> records);

    /**
     * This method corresponds to the database table tbl_order_rule_price_tag
     */
    int deleteByOrderIdAndCategory(@Param("orderId") int orderId, @Param("category") Integer category);
}