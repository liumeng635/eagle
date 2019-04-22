package com.eagle.entity;

import com.eagle.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 功能描述:
 * @author: 刘猛
 * @date: 2019/4/22 10:32
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class BaseUniversity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 学校名称
     */
    private String schoolName;
    /**
     * 所属区域，查询用
     */
    private String areaId;
    /**
     * 区域名称，显示用
     */
    private String areaName;
    /**
     * 是否为热点数据,0-否,1-是
     */
    private Integer hotspot;


    public static final String SCHOOL_NAME = "school_name";

    public static final String AREA_ID = "area_id";

    public static final String AREA_NAME = "area_name";

    public static final String HOTSPOT = "hotspot";

}
