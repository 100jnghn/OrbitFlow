package com.finalproj.orbitflow.global.file.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : S3FileStorage
 * @since : 26. 1. 3. 토요일
 **/


@Service
@RequiredArgsConstructor
public class S3FileStorage implements FileStorage {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public Resource loadAsResource(String objectKey) {

        ResponseInputStream<GetObjectResponse> s3Object =
                s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(objectKey)
                                .build()
                );

        return new InputStreamResource(s3Object);
    }
}
