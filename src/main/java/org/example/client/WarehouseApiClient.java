package org.example.client;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.request.AddCategoryRequest;
import org.example.request.AddProductRequest;
import org.example.request.ChangePasswordRequest;
import org.example.request.CreateInvoiceRequest;
import org.example.request.CreateUserRequest;
import org.example.request.LoginRequest;
import org.example.request.MonthlyReportRequest;
import org.example.request.StockInRequest;
import org.example.request.StockOutRequest;
import org.example.request.UpdateProductRequest;
import org.example.response.CategoryResponse;
import org.example.response.InvoiceResponse;
import org.example.response.LoginResponse;
import org.example.response.MonthlySalesReportResponse;
import org.example.response.ProductResponse;
import org.example.response.StockTransactionResponse;
import org.example.response.StoreResponse;
import org.example.response.UserResponse;

import java.time.YearMonth;
import java.util.List;

/**
 * Typed facade over the Spring Boot REST API. This is what the JavaFX screens talk
 * to; it hides the HTTP/JSON details behind plain Java method calls.
 */
public class WarehouseApiClient {

    private final ApiClient api;
    private String currentRole;

    public WarehouseApiClient() {
        this.api = new ApiClient();
    }

    public LoginResponse login(String username, String password) {
        LoginResponse response = api.post("/auth/login", new LoginRequest(username, password), new TypeReference<>() {});
        api.setAuthToken(response.getToken());
        this.currentRole = response.getRole();
        return response;
    }

    // Role of the currently logged-in user (ADMIN/MANAGER/STAFF), or null before login.

    public String getCurrentRole() {
        return currentRole;
    }

    public List<UserResponse> listUsers() {
        return api.get("/users", new TypeReference<>() {});
    }

    public void createUser(String username, String password, String role) {
        api.post("/users", new CreateUserRequest(username, password, role));
    }

    public void changePassword(String currentPassword, String newPassword) {
        api.put("/auth/password", new ChangePasswordRequest(currentPassword, newPassword));
    }

    public List<ProductResponse> listProducts() {
        return api.get("/products", new TypeReference<>() {});
    }

    public List<ProductResponse> listLowStock() {
        return api.get("/products/low-stock", new TypeReference<>() {});
    }

    public void addProduct(AddProductRequest request) {
        api.post("/products", request);
    }

    public void updateProduct(UpdateProductRequest request) {
        api.put("/products/" + request.getId(), request);
    }

    public void deleteProduct(Long id) {
        api.delete("/products/" + id);
    }

    public void stockIn(StockInRequest request) {
        api.post("/stock-transactions/in", request);
    }

    public void stockOut(StockOutRequest request) {
        api.post("/stock-transactions/out", request);
    }

    public List<StockTransactionResponse> history(Long productId) {
        return api.get("/stock-transactions/product/" + productId, new TypeReference<>() {});
    }

    public List<CategoryResponse> listCategories() {
        return api.get("/categories", new TypeReference<>() {});
    }

    public void addCategory(String name) {
        api.post("/categories", new AddCategoryRequest(name));
    }

    public StoreResponse getDefaultStore() {
        return api.get("/stores/default", new TypeReference<>() {});
    }

    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        return api.post("/invoices", request, new TypeReference<>() {});
    }

    public List<InvoiceResponse> listInvoices(Long storeId, YearMonth month) {
        String path = "/invoices?storeId=" + storeId + "&year=" + month.getYear() + "&month=" + month.getMonthValue();
        return api.get(path, new TypeReference<>() {});
    }

    public MonthlySalesReportResponse monthlySummary(MonthlyReportRequest request) {
        YearMonth month = request.getMonth();
        String path = "/reports/monthly?storeId=" + request.getStoreId()
                + "&year=" + month.getYear() + "&month=" + month.getMonthValue();
        return api.get(path, new TypeReference<>() {});
    }
}
