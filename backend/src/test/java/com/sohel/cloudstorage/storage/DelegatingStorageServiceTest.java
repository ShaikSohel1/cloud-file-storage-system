package com.sohel.cloudstorage.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class DelegatingStorageServiceTest {

    private AzureBlobStorageService azureBlobStorageService;
    private SupabaseStorageService supabaseStorageService;
    private LocalStorageService localStorageService;

    @BeforeEach
    void setUp() {
        azureBlobStorageService = mock(AzureBlobStorageService.class);
        supabaseStorageService = mock(SupabaseStorageService.class);
        localStorageService = mock(LocalStorageService.class);
    }

    @Test
    @DisplayName("Should route upload to AzureBlobStorageService when STORAGE_TYPE is azure")
    void shouldRouteToAzureWhenConfigured() throws IOException {
        DelegatingStorageService delegatingService = new DelegatingStorageService(
                azureBlobStorageService,
                supabaseStorageService,
                localStorageService,
                "azure"
        );

        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy data".getBytes());
        when(azureBlobStorageService.uploadFile(file, "test.pdf")).thenReturn("azure://cloudstorage-files/test.pdf");

        String result = delegatingService.uploadFile(file, "test.pdf");

        assertEquals("azure://cloudstorage-files/test.pdf", result);
        verify(azureBlobStorageService).uploadFile(file, "test.pdf");
    }

    @Test
    @DisplayName("Should route upload to LocalStorageService when STORAGE_TYPE is local")
    void shouldRouteToLocalWhenConfigured() throws IOException {
        DelegatingStorageService delegatingService = new DelegatingStorageService(
                azureBlobStorageService,
                supabaseStorageService,
                localStorageService,
                "local"
        );

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());
        when(localStorageService.uploadFile(file, "test.txt")).thenReturn("/uploads/test.txt");

        String result = delegatingService.uploadFile(file, "test.txt");

        assertEquals("/uploads/test.txt", result);
        verify(localStorageService).uploadFile(file, "test.txt");
    }

    @Test
    @DisplayName("Should route download to AzureBlobStorageService when STORAGE_TYPE is azure")
    void shouldRouteDownloadToAzure() throws IOException {
        DelegatingStorageService delegatingService = new DelegatingStorageService(
                azureBlobStorageService,
                supabaseStorageService,
                localStorageService,
                "azure"
        );

        byte[] expected = "azure file content".getBytes();
        when(azureBlobStorageService.downloadFile("test.pdf")).thenReturn(expected);

        byte[] result = delegatingService.downloadFile("test.pdf");

        assertNotNull(result);
        assertEquals(expected.length, result.length);
        verify(azureBlobStorageService).downloadFile("test.pdf");
    }
}
