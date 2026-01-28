package com.medusa.mall.controller;

import com.google.zxing.WriterException;
import com.medusa.mall.util.QrCodeUtil;
import com.medusa.common.annotation.Anonymous;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Anonymous
@RestController
public class QrCodeController {
    @GetMapping("/api/qr")
    public void getQrCode(@RequestParam("content") String content, HttpServletResponse response) throws IOException {
        try {
            BufferedImage qrImage = QrCodeUtil.generateQrCodeImage(content, 300, 300);
            response.setContentType("image/png");
            ImageIO.write(qrImage, "PNG", response.getOutputStream());
        } catch (WriterException e) {
            response.sendError(500, "Failed to generate QR code");
        }
    }
} 