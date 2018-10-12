package com.coolweather.android.db;

import org.litepal.crud.DataSupport;
import org.litepal.crud.LitePalSupport;

/**
 * 省份表的model,省份实体类
 * @author Administrator
 */
public class Province extends LitePalSupport{
    private int id;

    private String provinceName;

    private int provinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String name) {
        this.provinceName = name;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int code) {
        this.provinceCode = code;
    }
}
