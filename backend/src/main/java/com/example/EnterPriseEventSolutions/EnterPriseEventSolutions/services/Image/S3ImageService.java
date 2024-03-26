package com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.services.Image;

import com.example.EnterPriseEventSolutions.EnterPriseEventSolutions.services.AmazonS3Service;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service("storageService")
@Profile("production")
public class S3ImageService {


    private final AmazonS3Service amazonS3Service;

    public S3ImageService(AmazonS3Service amazonS3Service) {
        this.amazonS3Service = amazonS3Service;
    }

    public void uploadImage(File imageFile, String imageName, String userName) throws IOException {
        String key = userName + "/" + imageName;
        amazonS3Service.uploadFile(key, imageFile);
        // Eliminar el archivo temporal después de cargarlo a S3

    }

    //  private static File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
    //     File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
    //     FileCopyUtils.copy(multipartFile.getBytes(), file);
    //      return file;
    //   }

    public byte[] downloadImage(String imageName) throws IOException {
        return amazonS3Service.downloadFile(imageName);
    }

    public void deleteImage(String imageName) {
        amazonS3Service.deleteFile(imageName);
    }

    // Otros métodos para manejar imágenes
}

