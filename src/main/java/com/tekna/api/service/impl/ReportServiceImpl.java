package com.tekna.api.service.impl;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.tekna.api.common.exception.InternalServerException;
import com.tekna.api.dto.InventoryMovementFilterInput;
import com.tekna.api.dto.ProductFilterInput;
import com.tekna.api.entity.InventoryMovement;
import com.tekna.api.entity.Product;
import com.tekna.api.repository.InventoryMovementRepository;
import com.tekna.api.repository.ProductRepository;
import com.tekna.api.service.ReportService;
import com.tekna.api.specification.InventoryMovementSpecification;
import com.tekna.api.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;

    @Override
    public byte[] exportProductsToExcel(ProductFilterInput filter) {
        log.info("Generating Excel report for products");
        Specification<Product> spec = ProductSpecification.withFilter(filter);
        List<Product> products = productRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "name"));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Inventario de productos");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle alertStyle = createAlertStyle(workbook);

            String[] columns = {"SKU", "Nombre", "Categoría", "Precio (S/)", "Stock actual", "Stock mínimo", "Estado"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowIdx++);

                createCell(row, 0, product.getSku(), dataStyle);
                createCell(row, 1, product.getName(), dataStyle);
                createCell(row, 2, product.getCategory() != null ? product.getCategory().getName() : "-", dataStyle);

                Cell priceCell = row.createCell(3);
                priceCell.setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
                priceCell.setCellStyle(currencyStyle);

                boolean isLow = product.isLowStock();
                CellStyle currentStockStyle = isLow ? alertStyle : dataStyle;

                Cell stockCell = row.createCell(4);
                stockCell.setCellValue(product.getStock() != null ? product.getStock() : 0);
                stockCell.setCellStyle(currentStockStyle);

                Cell minStockCell = row.createCell(5);
                minStockCell.setCellValue(product.getMinStock() != null ? product.getMinStock() : 0);
                minStockCell.setCellStyle(dataStyle);

                Cell statusCell = row.createCell(6);
                statusCell.setCellValue(isLow ? "Bajo stock" : "Normal");
                statusCell.setCellStyle(currentStockStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error creating products Excel report", e);
            throw new InternalServerException("Failed to generate products Excel report: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportProductsToPdf(ProductFilterInput filter) {
        log.info("Generating PDF report for products");
        Specification<Product> spec = ProductSpecification.withFilter(filter);
        List<Product> products = productRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "name"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(31, 41, 55));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(107, 114, 128));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font alertFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(185, 28, 28));

            Paragraph title = new Paragraph("TEKNA GAMER - REPORTE DE INVENTARIO Y STOCK", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Generado el: " + DATE_FORMATTER.format(Instant.now()) + " | Total items: " + products.size(), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.0f, 4.0f, 2.5f, 1.8f, 1.5f, 1.5f, 2.0f});

            String[] headers = {"SKU", "Nombre del producto", "Categoría", "Precio", "Stock", "Mínimo", "Estado"};
            Color headerBg = new Color(31, 41, 55);

            for (String headerText : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(headerText, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Color altBg = new Color(249, 250, 251);
            boolean isAlt = false;

            for (Product product : products) {
                Color rowBg = isAlt ? altBg : Color.WHITE;
                boolean isLow = product.isLowStock();

                addPdfCell(table, product.getSku(), cellFont, rowBg, Element.ALIGN_LEFT);
                addPdfCell(table, product.getName(), cellFont, rowBg, Element.ALIGN_LEFT);
                addPdfCell(table, product.getCategory() != null ? product.getCategory().getName() : "-", cellFont, rowBg, Element.ALIGN_LEFT);
                addPdfCell(table, product.getPrice() != null ? "S/ " + product.getPrice().toPlainString() : "-", cellFont, rowBg, Element.ALIGN_RIGHT);
                addPdfCell(table, String.valueOf(product.getStock()), isLow ? alertFont : cellFont, isLow ? new Color(254, 242, 242) : rowBg, Element.ALIGN_CENTER);
                addPdfCell(table, String.valueOf(product.getMinStock()), cellFont, rowBg, Element.ALIGN_CENTER);
                addPdfCell(table, isLow ? "ALERTA: BAJO STOCK" : "NORMAL", isLow ? alertFont : cellFont, isLow ? new Color(254, 242, 242) : rowBg, Element.ALIGN_CENTER);

                isAlt = !isAlt;
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error creating products PDF report", e);
            throw new InternalServerException("Failed to generate products PDF report: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportMovementsToExcel(InventoryMovementFilterInput filter) {
        log.info("Generating Excel report for inventory movements");
        Specification<InventoryMovement> spec = InventoryMovementSpecification.withFilter(filter);
        List<InventoryMovement> movements = movementRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Movimientos de inventario");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            String[] columns = {"Fecha", "Tipo", "SKU", "Producto", "Cantidad", "Stock ant.", "Stock nuevo", "Precio unit.", "Documento", "Motivo"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (InventoryMovement movement : movements) {
                Row row = sheet.createRow(rowIdx++);

                createCell(row, 0, movement.getCreatedAt() != null ? DATE_FORMATTER.format(movement.getCreatedAt()) : "-", dataStyle);
                createCell(row, 1, movement.getMovementType() != null ? movement.getMovementType().name() : "-", dataStyle);
                createCell(row, 2, movement.getProduct() != null ? movement.getProduct().getSku() : "-", dataStyle);
                createCell(row, 3, movement.getProduct() != null ? movement.getProduct().getName() : "-", dataStyle);

                Cell qtyCell = row.createCell(4);
                qtyCell.setCellValue(movement.getQuantity() != null ? movement.getQuantity() : 0);
                qtyCell.setCellStyle(dataStyle);

                Cell prevCell = row.createCell(5);
                prevCell.setCellValue(movement.getPreviousStock() != null ? movement.getPreviousStock() : 0);
                prevCell.setCellStyle(dataStyle);

                Cell newCell = row.createCell(6);
                newCell.setCellValue(movement.getNewStock() != null ? movement.getNewStock() : 0);
                newCell.setCellStyle(dataStyle);

                Cell priceCell = row.createCell(7);
                priceCell.setCellValue(movement.getUnitPrice() != null ? movement.getUnitPrice().doubleValue() : 0.0);
                priceCell.setCellStyle(currencyStyle);

                createCell(row, 8, movement.getDocumentNumber() != null ? movement.getDocumentNumber() : "-", dataStyle);
                createCell(row, 9, movement.getReason() != null ? movement.getReason() : "-", dataStyle);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Error creating movements Excel report", e);
            throw new InternalServerException("Failed to generate movements Excel report: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportMovementsToPdf(InventoryMovementFilterInput filter) {
        log.info("Generating PDF report for inventory movements");
        Specification<InventoryMovement> spec = InventoryMovementSpecification.withFilter(filter);
        List<InventoryMovement> movements = movementRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(31, 41, 55));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(107, 114, 128));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);

            Paragraph title = new Paragraph("TEKNA GAMER - HISTORIAL DE MOVIMIENTOS DE INVENTARIO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Generado el: " + DATE_FORMATTER.format(Instant.now()) + " | Total registros: " + movements.size(), subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.0f, 1.3f, 2.0f, 3.2f, 1.2f, 1.2f, 1.8f, 2.5f});

            String[] headers = {"Fecha", "Tipo", "SKU", "Producto", "Cant.", "Nuevo", "Documento", "Motivo"};
            Color headerBg = new Color(31, 41, 55);

            for (String headerText : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(headerText, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            Color altBg = new Color(249, 250, 251);
            boolean isAlt = false;

            for (InventoryMovement movement : movements) {
                Color rowBg = isAlt ? altBg : Color.WHITE;

                addPdfCell(table, movement.getCreatedAt() != null ? DATE_FORMATTER.format(movement.getCreatedAt()) : "-", cellFont, rowBg, Element.ALIGN_CENTER);
                addPdfCell(table, movement.getMovementType() != null ? movement.getMovementType().name() : "-", cellFont, rowBg, Element.ALIGN_CENTER);
                addPdfCell(table, movement.getProduct() != null ? movement.getProduct().getSku() : "-", cellFont, rowBg, Element.ALIGN_LEFT);
                addPdfCell(table, movement.getProduct() != null ? movement.getProduct().getName() : "-", cellFont, rowBg, Element.ALIGN_LEFT);
                addPdfCell(table, String.valueOf(movement.getQuantity()), cellFont, rowBg, Element.ALIGN_CENTER);
                addPdfCell(table, String.valueOf(movement.getNewStock()), cellFont, rowBg, Element.ALIGN_CENTER);
                addPdfCell(table, movement.getDocumentNumber() != null ? movement.getDocumentNumber() : "-", cellFont, rowBg, Element.ALIGN_LEFT);
                addPdfCell(table, movement.getReason() != null ? movement.getReason() : "-", cellFont, rowBg, Element.ALIGN_LEFT);

                isAlt = !isAlt;
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error creating movements PDF report", e);
            throw new InternalServerException("Failed to generate movements PDF report: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        setBorders(style);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("\"S/\" #,##0.00"));
        setBorders(style);
        return style;
    }

    private CellStyle createAlertStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void createCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void addPdfCell(PdfPTable table, String text, Font font, Color background, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(background);
        cell.setPadding(5);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}
