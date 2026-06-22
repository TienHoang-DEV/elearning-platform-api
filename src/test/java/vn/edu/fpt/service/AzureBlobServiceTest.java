package vn.edu.fpt.service;

import org.junit.jupiter.api.Test;
import vn.edu.fpt.service.cloud.AzureBlobService;

import static org.assertj.core.api.Assertions.assertThat;

class AzureBlobServiceTest {

    @Test
    void extractBlobNameKeepsEncodedSeedValue() {
        String blobName = AzureBlobService.extractBlobName("%5B28Tech%5D.%20BUOI%201.pdf");

        assertThat(blobName).isEqualTo("[28Tech]. BUOI 1.pdf");
    }

    @Test
    void extractBlobNameFromFullAzureBlobUrl() {
        String blobName = AzureBlobService.extractBlobName(
                "https://elearningstorageswp391.blob.core.windows.net/materials/%5B28Tech%5D.%20BUOI%201.pdf"
        );

        assertThat(blobName).isEqualTo("[28Tech]. BUOI 1.pdf");
    }
}
