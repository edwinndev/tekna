package com.tekna.api.service;

import com.tekna.api.dto.InventoryMovementFilterInput;
import com.tekna.api.dto.ProductFilterInput;

/**
 * Service contract for generating and exporting tabular reports in Excel and PDF formats.
 */
public interface ReportService {

    /**
     * Generates an Excel spreadsheet (.xlsx) containing filtered or full product inventory data.
     *
     * @param filter optional dynamic filtering parameters for products
     * @return raw byte array of the generated Excel document
     */
    byte[] exportProductsToExcel(ProductFilterInput filter);

    /**
     * Generates a PDF report containing filtered or full product inventory data.
     *
     * @param filter optional dynamic filtering parameters for products
     * @return raw byte array of the generated PDF document
     */
    byte[] exportProductsToPdf(ProductFilterInput filter);

    /**
     * Generates an Excel spreadsheet (.xlsx) containing inventory movement history.
     *
     * @param filter optional dynamic filtering parameters for movements
     * @return raw byte array of the generated Excel document
     */
    byte[] exportMovementsToExcel(InventoryMovementFilterInput filter);

    /**
     * Generates a PDF report containing inventory movement history.
     *
     * @param filter optional dynamic filtering parameters for movements
     * @return raw byte array of the generated PDF document
     */
    byte[] exportMovementsToPdf(InventoryMovementFilterInput filter);
}
