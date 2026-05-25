package com.shelfflow.exception;

/**
 * 组合包启用失败异常
 */
public class BundleEnableFailedException extends BaseException {

    public BundleEnableFailedException(){}

    public BundleEnableFailedException(String msg){
        super(msg);
    }
}
