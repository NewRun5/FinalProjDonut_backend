package com.donut.common;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileController {
    /**
     * 파일 저장 테스트용 메소드입니다.
     * 클라이언트에서는 Base64 문자열 처리하여 보내면 됩니다.
     * 확장자, 원본 이름 등의 메타데이터 있으면 좀 더 정교하게 저장 가능합니다.
     * @param file : 클라이언트 (Base64 문자열) -> 서버 File 객체
     * @return : 완료시 true 반환
     * @throws IOException : 파일 저장 오류
     */
    @MutationMapping
    public boolean inputFileTest(@Argument File file) throws IOException {
        final String uploadDir = ""; // 저장할 경로

        Path destinationPath = Paths.get(uploadDir).resolve(file.getName()).normalize();

        // 파일을 지정한 경로에 복사
        Files.copy(file.toPath(), destinationPath);

        System.out.println("파일 저장 완료: " + destinationPath);
        return true;
    }
}
