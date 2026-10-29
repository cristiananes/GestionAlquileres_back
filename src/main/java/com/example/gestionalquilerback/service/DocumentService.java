package com.example.gestionalquilerback.service;
import com.example.gestionalquilerback.dto.DocumentResponse;
import com.example.gestionalquilerback.model.entity.*;
import com.example.gestionalquilerback.repository.*;
import com.example.gestionalquilerback.security.SecurityUtil;
import com.example.gestionalquilerback.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional
public class DocumentService {
    private final PropertyDocumentRepository documents;
    private final PropertyService properties;
    private final IncomeService incomes;
    private final ExpenseService expenses;
    private final SecurityUtil security;
    public List<DocumentResponse> list(Long propertyId, boolean archived) {
        properties.findEntity(propertyId);
        return documents.findByPropertyId(propertyId).stream().filter(d -> d.isArchived() == archived).map(this::response).toList();
    }
    public DocumentResponse upload(Long propertyId, Long incomeId, Long expenseId, MultipartFile file) throws IOException {
        var property = properties.findEntity(propertyId);
        if (file.isEmpty() || file.getSize() > 5L * 1024 * 1024) throw new IllegalArgumentException("El archivo debe tener entre 1 byte y 5 MB");
        if (incomeId != null && expenseId != null) throw new IllegalArgumentException("Vincula el archivo a un solo movimiento");
        var bytes = file.getBytes();
        var contentType = detectType(bytes);
        String filename = file.getOriginalFilename() == null ? "documento" : file.getOriginalFilename().replaceAll("[\\/\r\n]", "_");
        if (filename.length() > 200) filename = filename.substring(filename.length() - 200);
        var document = PropertyDocument.builder().property(property).user(security.getCurrentUser()).filename(filename).contentType(contentType).size(bytes.length).data(bytes).build();
        if (incomeId != null) { var income = incomes.findEntity(incomeId); if (income.getProperty() == null || !propertyId.equals(income.getProperty().getId())) throw new IllegalArgumentException("El ingreso no pertenece a esta propiedad"); document.setIncome(income); }
        if (expenseId != null) { var expense = expenses.findEntity(expenseId); if (expense.getProperty() == null || !propertyId.equals(expense.getProperty().getId())) throw new IllegalArgumentException("El gasto no pertenece a esta propiedad"); document.setExpense(expense); }
        return response(documents.save(document));
    }
    static String detectType(byte[] b) {
        if (b.length >= 5 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F' && b[4] == '-') return "application/pdf";
        if (b.length >= 8 && (b[0] & 255) == 137 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G' && b[4] == 13 && b[5] == 10 && b[6] == 26 && b[7] == 10) return "image/png";
        if (b.length >= 3 && (b[0] & 255) == 255 && (b[1] & 255) == 216 && (b[2] & 255) == 255) return "image/jpeg";
        throw new IllegalArgumentException("Solo se admiten PDF, PNG y JPEG");
    }
    public PropertyDocument entity(Long id) {
        var d = documents.findById(id).orElseThrow(() -> new ResourceNotFoundException("Document", id));
        properties.findEntity(d.getProperty().getId());
        return d;
    }
    public void archive(Long id, boolean archived) { var d = entity(id); d.setArchived(archived); documents.save(d); }
    private DocumentResponse response(PropertyDocument d) { return new DocumentResponse(d.getId(), d.getProperty().getId(), d.getIncome() == null ? null : d.getIncome().getId(), d.getExpense() == null ? null : d.getExpense().getId(), d.getFilename(), d.getContentType(), d.getSize(), d.isArchived(), d.getCreatedAt()); }
}
