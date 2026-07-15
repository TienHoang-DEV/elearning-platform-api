package vn.edu.fpt.service.cloud;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.util.AppConstants;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AzureBlobService {

    private final BlobServiceClient blobServiceClient;

    public AzureBlobService(@Value("${AZURE_STORAGE_CONNECTION_STRING}") String connectionString) {
        if (connectionString == null) {
            throw new RuntimeException("AZURE_STORAGE_CONNECTION_STRING is null");
        }
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    public String saveFile(MultipartFile file, String containerName) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            BlobClient blobClient = blobServiceClient.getBlobContainerClient(containerName).getBlobClient(filename);

            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(file.getContentType()));
            return blobClient.getBlobName();
        } catch (Exception e) {
            throw new RuntimeException("Error");
        }
    }

    public ResponseEntity<InputStreamResource> dowloadFile(BlobClient blobClient) {
        BlobProperties properties = blobClient.getProperties();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (properties.getContentType() != null && !properties.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(properties.getContentType());
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(properties.getBlobSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + URLEncoder.encode(blobClient.getBlobName(), StandardCharsets.UTF_8).replace("+", "%20"))
                .body(new InputStreamResource(blobClient.openInputStream()));
    }

    public String generateSasUrl(String containerName, String blobName) {
        BlobClient blobClient = getBlobClient(containerName, blobName);
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plusHours(AppConstants.SAS_EXPIRATION_HOURS),
                new BlobSasPermission().setReadPermission(true)
        );
        return blobClient.getBlobUrl() + "?" + blobClient.generateSas(sasValues);
    }

    public BlobClient getBlobClient(String containerName, String blobName) {
        return blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobName);
    }

    public String getPublicUrl(String containerName, String blobName) {
        return getBlobClient(containerName, blobName).getBlobUrl();
    }
    public void deleteFile(String azureStorageContainerMaterials, String fileUrl) {

    }
}
