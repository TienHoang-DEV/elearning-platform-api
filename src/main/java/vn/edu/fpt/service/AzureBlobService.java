package vn.edu.fpt.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.util.AppConstants;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AzureBlobService {

    private final BlobServiceClient blobServiceClient;

    public AzureBlobService() {
        String connectionString = System.getProperty("AZURE_STORAGE_CONNECTION_STRING");
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
            return blobClient.getBlobUrl();
        } catch (Exception e) {
            throw new RuntimeException("Error");
        }
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
        String decodedBlobName = extractBlobName(blobName);
        return blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(decodedBlobName);
    }

    public static String extractBlobName(String blobUrlOrName) {
        if (blobUrlOrName == null || blobUrlOrName.isBlank()) {
            return blobUrlOrName;
        }

        String blobName = blobUrlOrName.trim();
        if (blobName.startsWith("http://") || blobName.startsWith("https://")) {
            try {
                String path = URI.create(blobName).getPath();
                int containerSeparator = path.indexOf('/', 1);
                blobName = containerSeparator >= 0 ? path.substring(containerSeparator + 1) : path;
            } catch (IllegalArgumentException ignored) {
                int queryIndex = blobName.indexOf('?');
                blobName = queryIndex >= 0 ? blobName.substring(0, queryIndex) : blobName;
                int slashIndex = blobName.lastIndexOf('/');
                blobName = slashIndex >= 0 ? blobName.substring(slashIndex + 1) : blobName;
            }
        } else {
            int queryIndex = blobName.indexOf('?');
            blobName = queryIndex >= 0 ? blobName.substring(0, queryIndex) : blobName;
        }

        return URLDecoder.decode(blobName, StandardCharsets.UTF_8);
    }

}
