package com.dw.admin.components.auth;

/**
 * token 缓存服务接口
 *
 * @author dawei
 */
public interface TokenCacheHelper {

    /**
     * 是否存在 token
     */
    boolean contains(String tokenId);

    /**
     * 保存 token
     */
    boolean put(String tokenId, String token);

    /**
     * 删除 token
     */
    boolean remove(String tokenId);

}
