package com.mashibing.lcnorder.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * tbl_order
 * @author 
 */
@Data
public class TblOrder implements Serializable {
    private Integer id;

    private String orderName;

    private static final long serialVersionUID = 1L;
}