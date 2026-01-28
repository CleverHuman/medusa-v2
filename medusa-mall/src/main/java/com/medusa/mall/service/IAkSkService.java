package com.medusa.mall.service;

import jakarta.servlet.http.HttpServletRequest;

public interface IAkSkService {
    Boolean checkAkSkAuth(HttpServletRequest request);

    String getSecretKeyByAccessKey(String accessKey);
}
