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
import com.tekna.api.service.AlertService;
import com.tekna.api.specification.AlertSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {

    private static final UUID SYSTEM_ALERT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final AlertRepository alertRepository;
    private final ProductRepository productRepository;
    private final AlertMapper alertMapper;

    @Override
    public AlertOutput getById(UUID id) {
        return alertRepository.findById(id)
                .map(alertMapper::toOutput)
                .orElseThrow(() -> new NotFoundException("Alert not found with id: " + id));
    }

    @Override
    @Transactional
    public AlertOutput markAsAttended(UUID id) {
        log.info("Marking alert with id: {} as attended", id);

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert not found with id: " + id));

        alert.setStatus(AlertStatus.ATTENDED);
        UUID currentUserId = UserContext.getUserId();
        alert.setUpdatedBy(currentUserId != null ? currentUserId : SYSTEM_ALERT_USER_ID);

        Alert updatedAlert = alertRepository.save(alert);
        return alertMapper.toOutput(updatedAlert);
    }

    @Override
    public PageOutput<AlertOutput> search(SearchInput<AlertFilterInput> input) {
        Sort sort = Sort.by(Sort.Direction.fromString(input.getSortDirection()), input.getSortBy());
        Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);
        Specification<Alert> spec = AlertSpecification.withFilter(input.getFilter());

        Page<Alert> page = alertRepository.findAll(spec, pageable);
        return new PageOutput<>(
                alertMapper.toOutputList(page.getContent()),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber()
        );
    }

    @Override
    @Transactional
    public void checkAndGenerateAlert(Product product, int currentStock) {
        if (currentStock <= product.getMinStock()) {
            boolean alreadyPending = alertRepository.existsByProductIdAndStatus(product.getId(), AlertStatus.PENDING);
            if (!alreadyPending) {
                log.warn("Generating LOW STOCK ALERT for product '{}' (SKU: {}). Current: {}, Min: {}",
                        product.getName(), product.getSku(), currentStock, product.getMinStock());

                Alert alert = new Alert();
                alert.setProduct(product);
                alert.setStockRecorded(currentStock);
                alert.setMinStockSnapshot(product.getMinStock());
                alert.setStatus(AlertStatus.PENDING);

                UUID currentUserId = UserContext.getUserId();
                alert.setCreatedBy(currentUserId != null ? currentUserId : SYSTEM_ALERT_USER_ID);

                alertRepository.save(alert);
            }
        }
    }

    @Override
    @Scheduled(fixedRate = 600000L)
    @Transactional
    public int checkAllProductsStockScheduled() {
        log.info("Executing scheduled periodic inventory stock verification");

        List<Product> products = productRepository.findAll();
        int createdAlertsCount = 0;

        for (Product product : products) {
            if (product.getStock() <= product.getMinStock()) {
                boolean alreadyPending = alertRepository.existsByProductIdAndStatus(product.getId(), AlertStatus.PENDING);
                if (!alreadyPending) {
                    Alert alert = new Alert();
                    alert.setProduct(product);
                    alert.setStockRecorded(product.getStock());
                    alert.setMinStockSnapshot(product.getMinStock());
                    alert.setStatus(AlertStatus.PENDING);
                    alert.setCreatedBy(SYSTEM_ALERT_USER_ID);

                    alertRepository.save(alert);
                    createdAlertsCount++;
                }
            }
        }

        log.info("Scheduled stock check finished. Generated {} new pending low-stock alerts", createdAlertsCount);
        return createdAlertsCount;
    }
}
