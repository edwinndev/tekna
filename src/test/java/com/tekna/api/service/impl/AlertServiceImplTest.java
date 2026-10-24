package com.tekna.api.service.impl;

import com.tekna.api.common.exception.NotFoundException;
import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.AlertFilterInput;
import com.tekna.api.dto.AlertOutput;
import com.tekna.api.entity.Alert;
import com.tekna.api.entity.AlertStatus;
import com.tekna.api.entity.Product;
import com.tekna.api.mapper.AlertMapper;
import com.tekna.api.repository.AlertRepository;
import com.tekna.api.repository.ProductRepository;
import com.tekna.api.security.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AlertMapper alertMapper;

    @InjectMocks
    private AlertServiceImpl alertService;

    private UUID alertId;
    private UUID productId;
    private Product product;
    private Alert alert;
    private AlertOutput alertOutput;

    @BeforeEach
    void setUp() {
        alertId = UUID.randomUUID();
        productId = UUID.randomUUID();
        UserContext.setUserId(UUID.randomUUID());

        product = new Product();
        product.setId(productId);
        product.setSku("KB-HYPERX-ALLOY");
        product.setName("HyperX Alloy Origins");
        product.setStock(3);
        product.setMinStock(5);

        alert = new Alert();
        alert.setId(alertId);
        alert.setProduct(product);
        alert.setStockRecorded(3);
        alert.setMinStockSnapshot(5);
        alert.setStatus(AlertStatus.PENDING);

        alertOutput = new AlertOutput();
        alertOutput.setId(alertId);
        alertOutput.setProductId(productId);
        alertOutput.setProductSku("KB-HYPERX-ALLOY");
        alertOutput.setProductName("HyperX Alloy Origins");
        alertOutput.setStockRecorded(3);
        alertOutput.setMinStockSnapshot(5);
        alertOutput.setStatus(AlertStatus.PENDING);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("Should successfully retrieve alert by ID")
    void getById_Success() {
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
        when(alertMapper.toOutput(alert)).thenReturn(alertOutput);

        AlertOutput result = alertService.getById(alertId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(alertId);
        assertThat(result.getStatus()).isEqualTo(AlertStatus.PENDING);
    }

    @Test
    @DisplayName("Should throw NotFoundException when alert ID does not exist")
    void getById_NotFound() {
        when(alertRepository.findById(alertId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.getById(alertId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Alert not found with id: " + alertId);
    }

    @Test
    @DisplayName("Should mark alert as attended successfully")
    void markAsAttended_Success() {
        when(alertRepository.findById(alertId)).thenReturn(Optional.of(alert));
        when(alertRepository.save(alert)).thenReturn(alert);
        alertOutput.setStatus(AlertStatus.ATTENDED);
        when(alertMapper.toOutput(alert)).thenReturn(alertOutput);

        AlertOutput result = alertService.markAsAttended(alertId);

        assertThat(result.getStatus()).isEqualTo(AlertStatus.ATTENDED);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.ATTENDED);
        verify(alertRepository).save(alert);
    }

    @Test
    @DisplayName("Should generate low stock alert when current stock is equal or less than minStock")
    void checkAndGenerateAlert_GeneratesAlert() {
        when(alertRepository.existsByProductIdAndStatus(productId, AlertStatus.PENDING)).thenReturn(false);

        alertService.checkAndGenerateAlert(product, 2);

        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should not duplicate alert if already pending for product")
    void checkAndGenerateAlert_AlreadyPending_DoesNotDuplicate() {
        when(alertRepository.existsByProductIdAndStatus(productId, AlertStatus.PENDING)).thenReturn(true);

        alertService.checkAndGenerateAlert(product, 2);

        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should check all products and generate alerts in scheduled execution")
    void checkAllProductsStockScheduled_Success() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(alertRepository.existsByProductIdAndStatus(productId, AlertStatus.PENDING)).thenReturn(false);

        int count = alertService.checkAllProductsStockScheduled();

        assertThat(count).isEqualTo(1);
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    @DisplayName("Should search alerts with pagination and filters")
    void search_Success() {
        SearchInput<AlertFilterInput> input = new SearchInput<>();
        input.setPageNumber(0);
        input.setPageSize(10);
        input.setSortBy("createdAt");
        input.setSortDirection("DESC");

        Page<Alert> page = new PageImpl<>(List.of(alert));
        when(alertRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(alertMapper.toOutputList(any())).thenReturn(List.of(alertOutput));

        PageOutput<AlertOutput> result = alertService.search(input);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
    }
}
