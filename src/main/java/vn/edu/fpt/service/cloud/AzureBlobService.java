package vn.edu.fpt.service.cloud;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.util.AppConstants;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
        //Encode vì vì tên file có thể chứa các ký tự mà HTTP Header không xử lý tốt, đặc biệt là:
        //Dấu cách ( )
        //Tiếng Việt có dấu (à, á, đ, ...)
        //Ký tự đặc biệt (#, &, %, ?, ...)
        //URLEncoder được thiết kế cho dữ liệu form HTML: Space -> +
        //Trong URL hoặc filename*=UTF-8''... của HTTP Header: Space -> %20
    }

    public String generateSasUrl(String containerName, String blobName) {
        // 1. Kết nối tới file cụ thể (Blob Client) trên Azure Container
        BlobClient blobClient = getBlobClient(containerName, blobName);
        
        // 2. Thiết lập cấu hình mã Token truy cập tạm thời (SAS - Shared Access Signature)
        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                // Thời gian hết hạn của link (link xem video tự động vô hiệu hóa sau số giờ định nghĩa trước)
                OffsetDateTime.now().plusHours(AppConstants.SAS_EXPIRATION_HOURS),
                // Chỉ cấp quyền ĐỌC (Read) video
                new BlobSasPermission().setReadPermission(true)
        );
        
        // 3. Ghép URL gốc của file video với chuỗi ký tên SAS Token bảo mật và trả về
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

    public String getPublicUrl(String containerName, String blobName) {
        return getBlobClient(containerName, blobName).getBlobUrl();
    }
}
