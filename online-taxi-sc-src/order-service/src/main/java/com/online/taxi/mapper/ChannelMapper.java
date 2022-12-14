package com.online.taxi.mapper;

import com.online.taxi.entity.Channel;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 * @date 2018/8/25
 */
@Service
public interface ChannelMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_channel
     *
     * @mbggenerated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_channel
     *
     * @mbggenerated
     */
    int insert(Channel record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_channel
     *
     * @mbggenerated
     */
    int insertSelective(Channel record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_channel
     *
     * @mbggenerated
     */
    Channel selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_channel
     *
     * @mbggenerated
     */
    int updateByPrimaryKeySelective(Channel record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tbl_channel
     *
     * @mbggenerated
     */
    int updateByPrimaryKey(Channel record);
}
