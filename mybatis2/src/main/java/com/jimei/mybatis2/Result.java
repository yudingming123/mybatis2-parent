package com.jimei.mybatis2;

/**
 * @Author yudm
 * @Date 2020/12/19 14:43
 * @Desc
 */
public class Result {
    //状态码
    private int sts;
    //异常类型
    private String exType;
    //抛出的异常对象信息
    private String exInfo;
    //具体的数据
    private Object data;

    public Result(int sts, String exType, String exInfo, Object data) {
        this.sts = sts;
        this.exType = exType;
        this.exInfo = exInfo;
        this.data = data;
    }

    public static Result success(Object data) {
        return new Result(0, null, null, data);
    }

    public static Result faild(String exType, String exInfo) {
        return new Result(1, exType, exInfo, null);
    }

    public int getSts() {
        return sts;
    }

    public void setSts(int sts) {
        this.sts = sts;
    }

    public String getExType() {
        return exType;
    }

    public void setExType(String exType) {
        this.exType = exType;
    }

    public String getExInfo() {
        return exInfo;
    }

    public void setExInfo(String exInfo) {
        this.exInfo = exInfo;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
