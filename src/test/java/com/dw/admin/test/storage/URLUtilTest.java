package com.dw.admin.test.storage;

import com.dw.admin.components.storage.StorageUrlUtil;


import java.util.Objects;

/**
 * @author dawei
 */
public class URLUtilTest {


    public static void main(String[] args) {
        // 获取腾讯云COS预签名URL中提取过期时间
        String url = "https://abc-test-1384986123.cos.ap-beijing.myqcloud.com/%E9%A6%99%E8%95%89banana.png?q-sign-algorithm=sha1&q-ak=AKIDIU8K0ucGP&q-sign-time=1763384904;1763388504&q-key-time=1763384904;1763388504&q-header-list=host&q-url-param-list=ci-process&q-signature=404b023a&x-cos-security-token=nAzXGnporj7&ci-process=originImage";
        Integer expirationTime = StorageUrlUtil.getExpiresValue(url);
        System.out.println("腾讯云过期时间: " + expirationTime);
        if (Objects.equals(1763388504, expirationTime)) {
            System.out.println("提取正确");
        }

        // 获取阿里云OSS预签名URL中提取过期时间
        String url2 = "http://abc-oss-bucket.oss-cn-beijing.aliyuncs.com/dwa/1952770676166279098/dog.png?Expires=1755016584&OSSAccessKeyId=LTAI5tAx1rNrvfzX7du91kGq&Signature=xP6j36U5XaiZk9n9qwxWzlCUuaY%3D";
        Integer expirationTime2 = StorageUrlUtil.getExpiresValue(url2);
        System.out.println("阿里云过期时间: " + expirationTime2);
        if (Objects.equals(1755016584, expirationTime2)) {
            System.out.println("提取正确");
        }
    }
}
