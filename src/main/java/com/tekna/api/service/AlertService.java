package com.tekna.api.service;

import com.tekna.api.common.pagination.PageOutput;
import com.tekna.api.common.pagination.SearchInput;
import com.tekna.api.dto.AlertFilterInput;
import com.tekna.api.dto.AlertOutput;
import com.tekna.api.entity.Product;

import java.util.UUID;

/**
 * Service contract for managing low stock alerts and periodic inventory evaluations.
 */
public interface AlertService {

    /**
     * Retrieves an alert record by its unique identifier.
     *
     * @param id the alert UUID
     * @return the alert output details
     * @throws com.tekna.api.common.exception.NotFoundException if the alert does not exist
     */
    AlertOutput getById(UUID id);

    /**
     * Marks a pending alert as attended.
     *
     * @param id the alert UUID
     * @return the updated alert output
     * @throws com.tekna.api.common.exception.NotFoundException if the alert does not exist
     */
    AlertOutput markAsAttended(UUID id);

    /**
     * Performs a paginated search of alerts using dynamic filter criteria.
     *
     * @param input the search parameters including page, size, sorting, and filter criteria
     * @return a paginated page of alert outputs
     */
    PageOutput<AlertOutput> search(SearchInput<AlertFilterInput> input);

    /**
     * Evaluates a product's current stock and triggers a new pending alert if stock is at or below minimum level.
     *
     * @param product      the product being evaluated
     * @param currentStock the verified stock level
     */
    void checkAndGenerateAlert(Product product, int currentStock);

    /**
     * Periodically inspects all active products in the system to identify low stock items and generate pending alerts.
     *
     * @return the number of new alerts generated during this run
     */
    int checkAllProductsStockScheduled();
}
