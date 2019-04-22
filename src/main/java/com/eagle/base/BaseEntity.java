package com.eagle.base;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 公共的entity
 *
 * @author yu_chen
 * @date 2017-12-01 17:15
 **/
@Data
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 创建人
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 修改人
     */
    private Long updateUser;

    /**
     * 修改时间
     */
    private Timestamp updateTime;

    public final static String ID = "id";
    public final static String CREATE_USER = "create_user";
    public final static String CREATE_TIME = "create_time";
    public final static String UPDATE_USER = "update_user";
    public final static String UPDATE_TIME = "update_time";
}
