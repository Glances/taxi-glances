package com.online.taxi.mapper;

import com.online.taxi.entity.ChargeRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 功能描述
 * @date 2018/8/25
 */
@Service
public interface ChargeRuleMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_charge_rule
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_charge_rule
     *
     * @mbggenerated
     */
    int insert(ChargeRule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_charge_rule
     *
     * @mbggenerated
     */
    int insertSelective(ChargeRule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_charge_rule
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(ChargeRule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_charge_rule
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(ChargeRule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_charge_rule
     *
     * @mbggenerated
     */
    List<ChargeRule> selectByPrimaryKey(ChargeRule chargeRule);
}
