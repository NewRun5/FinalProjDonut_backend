package com.donut.common.utils;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class FileUtil {
    public String saveImage(String imagePath) throws IOException, NoSuchAlgorithmException {
        // URL에서 이미지 다운로드
        URL url = new URL(imagePath);
        InputStream in = url.openStream();

        // 이미지 데이터를 바이트 배열로 읽기
        byte[] imageBytes = in.readAllBytes();
        in.close();

        // 파일 이름을 해시값으로 생성
        String hashName = generateHash(imageBytes) + ".png";

        // 저장할 경로 설정
        String saveDirectory = "src/main/resources/static/images/";
        File saveDir = new File(saveDirectory);
        if (!saveDir.exists()) {
            saveDir.mkdirs();  // 디렉토리가 없으면 생성
        }

        // 최종 저장 경로
        String savePath = Paths.get(saveDirectory, hashName).toString();

        // 이미지 파일 저장
        try (FileOutputStream fos = new FileOutputStream(savePath)) {
            fos.write(imageBytes);
        }

        // 저장 경로 반환
        return savePath;
    }

    public String generateHash(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data);

        // 바이트 배열을 16진수 문자열로 변환
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}
