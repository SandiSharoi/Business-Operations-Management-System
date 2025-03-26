package com.DAT.capstone_project.controller;

import com.DAT.capstone_project.dto.FormApplyDTO;
import com.DAT.capstone_project.service.ApproverService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/download")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class DownloadController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    @Autowired
    private ApproverService approverService;

    // This endpoint generates and downloads the Excel file
    @GetMapping("/excel/{approverId}")
    public ResponseEntity<byte[]> downloadExcel(@PathVariable Long approverId) throws IOException {
        logger.info("Received request to download Excel for approverId: {}", approverId);

        // Fetch the past decisions for the approver from the service
        List<FormApplyDTO> pastDecisions = approverService.getPastDecisionsForApprover(approverId);

        // If no past decisions are found, return a 404 response
        if (pastDecisions == null || pastDecisions.isEmpty()) {
            logger.warn("No past decisions found for approverId: {}", approverId);
            return ResponseEntity.notFound().build();
        }

        // Create a new workbook and a sheet within it
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Past Decisions");

        // Create a cell style for center alignment (for data rows)
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Create a bold font for the header row
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        // Create a cell style for the header row and apply the bold font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);  // Ensure header is also center-aligned

        // Create the header row for the Excel file
        Row headerRow = sheet.createRow(0);
        String[] columns = {
                "Form ID", "Staff ID", "Staff Name", "Project ID", "Project Name", "Planned Date",
                "Planned Start Hour", "Planned End Hour", "Actual Date", "Actual Start Hour",
                "Actual End Hour", "Duration", "Overtime Date", "Work Type", "Description", "Document", "Status"
        };

        // Add column headers to the first row with the bold style and center alignment
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);  // Set the bold and center-aligned style for header cells
        }

        // Adjust column widths
        //sheet.setColumnWidth(0, 30 * 256);  // Form ID
        //sheet.setColumnWidth(1, 30 * 256);  // Staff ID
        sheet.setColumnWidth(2, 20 * 256);  // Staff Name
        //sheet.setColumnWidth(3, 20 * 256);  // Project ID
        sheet.setColumnWidth(4, 20 * 256);  // Project Name
        sheet.setColumnWidth(5, 20 * 256);  // Planned Date
        sheet.setColumnWidth(6, 17 * 256);  // Planned Start Hour
        sheet.setColumnWidth(7, 17 * 256);  // Planned End Hour
        sheet.setColumnWidth(8, 20 * 256);  // Actual Date
        sheet.setColumnWidth(9, 17 * 256);  // Actual Start Hour
        sheet.setColumnWidth(10, 17 * 256); // Actual End Hour
        sheet.setColumnWidth(11, 20 * 256); // Duration
        sheet.setColumnWidth(12, 15 * 256); // Overtime Date
        sheet.setColumnWidth(13, 20 * 256); // Work Type
        sheet.setColumnWidth(14, 20 * 256); // Description
        sheet.setColumnWidth(15, 20 * 256); // Document
        sheet.setColumnWidth(16, 15 * 256); // Status

        // Populate the rows with past decision data
        int rowNum = 1;
        for (FormApplyDTO form : pastDecisions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(form.getFormApplyId());
            row.createCell(1).setCellValue(form.getEmployee().getStaffId());
            row.createCell(2).setCellValue(form.getEmployee().getName());
            row.createCell(3).setCellValue(form.getProjectId());
            row.createCell(4).setCellValue(form.getProjectName());
            row.createCell(5).setCellValue(form.getActualDate().toString());
            row.createCell(6).setCellValue(form.getActualStartHour().toString());
            row.createCell(7).setCellValue(form.getActualEndHour().toString());
            row.createCell(8).setCellValue(form.getActualDate().toString());
            row.createCell(9).setCellValue(form.getActualStartHour().toString());
            row.createCell(10).setCellValue(form.getActualEndHour().toString());
            row.createCell(11).setCellValue(form.getDuration() != null ? form.getDuration().toString() : "N/A");
            row.createCell(12).setCellValue(form.getOvertimeDate() != null ? form.getOvertimeDate() : "N/A");
            row.createCell(13).setCellValue(form.getWorkType() != null ? form.getWorkType() : "N/A");
            row.createCell(14).setCellValue(form.getDescription() != null ? form.getDescription() : "N/A");
            row.createCell(15).setCellValue(form.getFileName() != null ? form.getFileName() : "N/A");
            row.createCell(16).setCellValue(form.getFormStatus());

            // Apply the center alignment style to each cell (Changed)
            for (int i = 0; i < 17; i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        // Write the workbook data to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        byte[] excelFile = baos.toByteArray();

        // Set up the headers for the HTTP response
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=formList.xlsx");

        // Return the file as a downloadable response
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excelFile);

    }

    // This endpoint generates and downloads the PDF file
    @GetMapping("/pdf/{approverId}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long approverId) throws Exception {
        logger.info("Received request to download PDF for approverId: {}", approverId);

        // Fetch the past decisions for the approver from the service
        List<FormApplyDTO> pastDecisions = approverService.getPastDecisionsForApprover(approverId);

        // If no past decisions are found, return a 404 response
        if (pastDecisions == null || pastDecisions.isEmpty()) {
            logger.warn("No past decisions found for approverId: {}", approverId);
            return ResponseEntity.notFound().build();
        }

        // Create a Document instance for the PDF
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Create PdfWriter instance
        PdfWriter.getInstance(document, baos);
        document.open();

        // Add title to the PDF
        Paragraph title = new Paragraph("Past Form Decisions", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Create a table with 16 columns
        PdfPTable table = new PdfPTable(16);
        table.setWidthPercentage(100);

        // Add column headers to the table
        String[] columns = {
                "Form ID", "Staff ID", "Staff Name", "Project ID", "Project Name", "Planned Date",
                "Planned Start Hour", "Planned End Hour", "Actual Date", "Actual Start Hour",
                "Actual End Hour", "Duration", "Overtime Date", "Work Type", "Description", "Document", "Status"
        };

        for (String column : columns) {
            table.addCell(new Phrase(column));
        }

        // Add data rows to the table
        for (FormApplyDTO form : pastDecisions) {
            table.addCell(String.valueOf(form.getFormApplyId()));
            table.addCell(form.getEmployee().getStaffId());
            table.addCell(form.getEmployee().getName());
            table.addCell(String.valueOf(form.getProjectId()));
            table.addCell(form.getProjectName());
            table.addCell(form.getActualDate().toString());
            table.addCell(form.getActualStartHour().toString());
            table.addCell(form.getActualEndHour().toString());
            table.addCell(form.getActualDate().toString());
            table.addCell(form.getActualStartHour().toString());
            table.addCell(form.getActualEndHour().toString());
            table.addCell(form.getDuration() != null ? form.getDuration().toString() : "N/A");
            table.addCell(form.getOvertimeDate() != null ? form.getOvertimeDate() : "N/A");
            table.addCell(form.getWorkType() != null ? form.getWorkType() : "N/A");
            table.addCell(form.getDescription() != null ? form.getDescription() : "N/A");
            table.addCell(form.getFileName() != null ? form.getFileName() : "N/A");
            table.addCell(form.getFormStatus());
        }

        // Add the table to the document
        document.add(table);
        document.close();

        byte[] pdfFile = baos.toByteArray();

        // Set up the headers for the HTTP response
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=formList.pdf");

        // Return the file as a downloadable response
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfFile);
    }

    @GetMapping("/csv/{approverId}")
    public ResponseEntity<byte[]> downloadCsv(@PathVariable Long approverId) throws IOException {
        logger.info("Received request to download CSV for approverId: {}", approverId);

        // Fetch the past decisions for the approver from the service
        List<FormApplyDTO> pastDecisions = approverService.getPastDecisionsForApprover(approverId);

        // If no past decisions are found, return a 404 response
        if (pastDecisions == null || pastDecisions.isEmpty()) {
            logger.warn("No past decisions found for approverId: {}", approverId);
            return ResponseEntity.notFound().build();
        }

        // Create CSV content
        StringBuilder csvContent = new StringBuilder();
        String[] columns = {
                "Form ID", "Staff ID", "Staff Name", "Project ID", "Project Name", "Planned Date",
                "Planned Start Hour", "Planned End Hour", "Actual Date", "Actual Start Hour",
                "Actual End Hour", "Duration", "Overtime Date", "Work Type", "Description", "Document", "Status"
        };

        // Add header row
        csvContent.append(String.join(",", columns));
        csvContent.append("\n");

        // Add data rows
        for (FormApplyDTO form : pastDecisions) {
            csvContent.append(form.getFormApplyId()).append(",")
                    .append(form.getEmployee().getStaffId()).append(",")
                    .append(form.getEmployee().getName()).append(",")
                    .append(form.getProjectId()).append(",")
                    .append(form.getProjectName()).append(",")
                    .append(form.getActualDate()).append(",")
                    .append(form.getActualStartHour()).append(",")
                    .append(form.getActualEndHour()).append(",")
                    .append(form.getActualDate()).append(",")
                    .append(form.getActualStartHour()).append(",")
                    .append(form.getActualEndHour()).append(",")
                    .append(form.getDuration() != null ? form.getDuration().toString() : "N/A").append(",")
                    .append(form.getOvertimeDate() != null ? form.getOvertimeDate() : "N/A").append(",")
                    .append(form.getWorkType() != null ? form.getWorkType() : "N/A").append(",")
                    .append(form.getDescription() != null ? form.getDescription() : "N/A").append(",")
                    .append(form.getFileName() != null ? form.getFileName() : "N/A").append(",")
                    .append(form.getFormStatus()).append("\n");
        }

        // Convert to byte array
        byte[] csvFile = csvContent.toString().getBytes();

        // Set up the headers for the HTTP response
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=formList.csv");

        // Return the file as a downloadable response
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvFile);
    }

}
