package org.linlinjava.litemall.db.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.linlinjava.litemall.db.domain.LitemallShopOrder;
import org.linlinjava.litemall.db.domain.LitemallShopOrderExample;

public interface LitemallShopOrderMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    long countByExample(LitemallShopOrderExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    int deleteByExample(LitemallShopOrderExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    int insert(LitemallShopOrder record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    int insertSelective(LitemallShopOrder record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    LitemallShopOrder selectOneByExample(LitemallShopOrderExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    LitemallShopOrder selectOneByExampleSelective(@Param("example") LitemallShopOrderExample example, @Param("selective") LitemallShopOrder.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    List<LitemallShopOrder> selectByExampleSelective(@Param("example") LitemallShopOrderExample example, @Param("selective") LitemallShopOrder.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    List<LitemallShopOrder> selectByExample(LitemallShopOrderExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    LitemallShopOrder selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") LitemallShopOrder.Column ... selective);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    LitemallShopOrder selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    LitemallShopOrder selectByPrimaryKeyWithLogicalDelete(@Param("id") Integer id, @Param("andLogicalDeleted") boolean andLogicalDeleted);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") LitemallShopOrder record, @Param("example") LitemallShopOrderExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") LitemallShopOrder record, @Param("example") LitemallShopOrderExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(LitemallShopOrder record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     */
    int updateByPrimaryKey(LitemallShopOrder record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int logicalDeleteByExample(@Param("example") LitemallShopOrderExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table litemall_shop_order
     *
     * @mbg.generated
     * @project https://github.com/itfsw/mybatis-generator-plugin
     */
    int logicalDeleteByPrimaryKey(Integer id);
}