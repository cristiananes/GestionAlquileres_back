package com.example.gestionalquilerback.service;

import com.example.gestionalquilerback.model.entity.Expense;
import com.example.gestionalquilerback.model.entity.Income;
import com.example.gestionalquilerback.repository.ExpenseRepository;
import com.example.gestionalquilerback.repository.IncomeRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public byte[] generateIncomeReport(LocalDate from, LocalDate to) {
        List<Income> incomes;
        BigDecimal total;
        if (from != null && to != null) {
            incomes = incomeRepository.findByIncomeDateBetween(from, to);
            total = incomeRepository.sumByIncomeDateBetween(from, to);
        } else {
            incomes = incomeRepository.findAll();
            total = incomeRepository.sumAll();
        }
        return buildPdf("Reporte de Ingresos", from, to, incomes, total);
    }

    public byte[] generateExpenseReport(LocalDate from, LocalDate to) {
        List<Expense> expenses;
        BigDecimal total;
        if (from != null && to != null) {
            expenses = expenseRepository.findByExpenseDateBetween(from, to);
            total = expenseRepository.sumByExpenseDateBetween(from, to);
        } else {
            expenses = expenseRepository.findAll();
            total = expenseRepository.sumAll();
        }
        return buildPdf("Reporte de Gastos", from, to, expenses, total);
    }

    private byte[] buildPdf(String title, LocalDate from, LocalDate to, List<?> items, BigDecimal total) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(10);
        document.add(titlePara);

        if (from != null && to != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Paragraph dateRange = new Paragraph("Período: " + from.format(fmt) + " - " + to.format(fmt),
                    FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY));
            dateRange.setAlignment(Element.ALIGN_CENTER);
            dateRange.setSpacingAfter(20);
            document.add(dateRange);
        } else {
            Paragraph allTime = new Paragraph("Todos los registros",
                    FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GRAY));
            allTime.setAlignment(Element.ALIGN_CENTER);
            allTime.setSpacingAfter(20);
            document.add(allTime);
        }

        if (items.isEmpty()) {
            document.add(new Paragraph("No hay registros para el período seleccionado."));
        } else {
            Object first = items.getFirst();
            boolean isIncome = first instanceof Income;

            PdfPTable table = new PdfPTable(isIncome ? 5 : 4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(20);

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            PdfPCell headerCell;

            String[] headers = isIncome
                    ? new String[]{"Fecha", "Descripción", "Propiedad", "Tipo", "Monto"}
                    : new String[]{"Fecha", "Descripción", "Propiedad", "Monto"};

            for (String h : headers) {
                headerCell = new PdfPCell(new Phrase(h, headerFont));
                headerCell.setBackgroundColor(new Color(59, 130, 246));
                headerCell.setPadding(6);
                table.addCell(headerCell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            if (isIncome) {
                for (Object obj : items) {
                    Income i = (Income) obj;
                    table.addCell(new PdfPCell(new Phrase(i.getIncomeDate().format(fmt), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(i.getDescription(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(
                            i.getProperty() != null ? i.getProperty().getName() : "-", cellFont)));
                    table.addCell(new PdfPCell(new Phrase(i.getIncomeType().name(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase("$" + i.getAmount().toPlainString(), cellFont)));
                }
            } else {
                for (Object obj : items) {
                    Expense e = (Expense) obj;
                    table.addCell(new PdfPCell(new Phrase(e.getExpenseDate().format(fmt), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(e.getDescription(), cellFont)));
                    table.addCell(new PdfPCell(new Phrase(
                            e.getProperty() != null ? e.getProperty().getName() : "-", cellFont)));
                    table.addCell(new PdfPCell(new Phrase("$" + e.getAmount().toPlainString(), cellFont)));
                }
            }

            document.add(table);

            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(59, 130, 246));
            Paragraph totalPara = new Paragraph("Total: $" + total.toPlainString(), totalFont);
            totalPara.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalPara);
        }

        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.LIGHT_GRAY);
        Paragraph footer = new Paragraph("Generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " - GestionAlquiler", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }
}
