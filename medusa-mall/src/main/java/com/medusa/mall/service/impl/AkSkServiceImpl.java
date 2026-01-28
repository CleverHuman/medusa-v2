package com.medusa.mall.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medusa.common.constant.HttpStatus;
import com.medusa.common.exception.ServiceException;
import com.medusa.common.utils.StringUtils;
import com.medusa.common.utils.http.RequestBodyUtils;
import com.medusa.common.utils.sign.HmacSHA256Signer;
import com.medusa.common.utils.sign.SignUtils;
import com.medusa.mall.domain.MallAkSk;
import com.medusa.mall.mapper.AkSkMapper;
import com.medusa.mall.service.IAkSkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class AkSkServiceImpl implements IAkSkService {
    @Autowired
    private AkSkMapper akSkMapper;

    @Override
    public String getSecretKeyByAccessKey(String accessKey) {
        MallAkSk akSk = akSkMapper.selectByAccessKey(accessKey);
        if (akSk != null && akSk.getStatus() == 1) {
            return akSk.getSecretKey();
        }
        return null;
    }

    @Override
    public Boolean checkAkSkAuth(HttpServletRequest request) {
        String accessKey = request.getHeader("X-Access-Key");
        String signature = request.getHeader("X-Signature");
        String timestampStr = request.getHeader("X-Timestamp");

        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(signature)) {
            throw new ServiceException("Missing AK/SK headers", HttpStatus.UNAUTHORIZED);
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new ServiceException("Invalid Timestamp format", HttpStatus.UNAUTHORIZED);
        }

//        long now = System.currentTimeMillis();
//        if (Math.abs(now - timestamp) > 2 * 60 * 1000) {
//            throw new ServiceException("Request expired", HttpStatus.UNAUTHORIZED);
//        }

        String secretKey = getSecretKeyByAccessKey(accessKey);
        if (secretKey == null) {
            throw new ServiceException("Invalid AccessKey", HttpStatus.UNAUTHORIZED);
        }

        String body;
        String normalizedBody;
        String signContent;
        try{
            body = RequestBodyUtils.readJsonBody(request);
            normalizedBody = SignUtils.normalizeJson(body); // 排序
            signContent = timestampStr + normalizedBody;
        } catch (IOException e) {
            throw new ServiceException("Invalid JSON body", HttpStatus.UNAUTHORIZED);
        }

        String serverSignature = HmacSHA256Signer.signBase64(signContent, secretKey);

        if (!serverSignature.equals(signature)) {
            throw new ServiceException("Signature mismatch", HttpStatus.UNAUTHORIZED);
        }
        return true;
    }

}
