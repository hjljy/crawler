package cn.hjljy.crawler.demo.pojo.jsoupCrawler.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 2020版全国5级信息表
 * </p>
 *
 * @author 海加尔金鹰（www.hjljy.cn）
 * @since 2021-04-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysArea implements Serializable {

    private static final long serialVersionUID=1L;

    private Long id;

    private String name;

    private Long pid;

    private Long provinceCode;

    private Long cityCode;

    private Long areaCode;

    private Long streetCode;

    private Long committeeCode;

    private Long committeeType;

    private Integer sort;

    private Integer level;

}
