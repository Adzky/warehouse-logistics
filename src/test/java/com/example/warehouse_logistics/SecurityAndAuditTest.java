package com.example.warehouse_logistics;

import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityAndAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Test
    void requiresAuthenticationForOperationsAndCsrfForStateChanges() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/api/warehouses")
                        .with(user("manager").roles("WAREHOUSE_MANAGER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"WH-CSRF","name":"CSRF Warehouse","location":"Test"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthEndpointIsAvailableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    void administratorCanCreateAccountsAndTheyCanSignIn() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(user("admin").roles("ADMINISTRATOR"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"New.Receiver","password":"test-password-not-secret",
                                 "role":"RECEIVER"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new.receiver"))
                .andExpect(jsonPath("$.role").value("RECEIVER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "NEW.RECEIVER")
                        .param("password", "test-password-not-secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    void pickerCannotReceiveAndAccountAdministrationIsAdministratorOnly() throws Exception {
        mockMvc.perform(post("/api/inventory/receive")
                        .with(user("picker").roles("PICKER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users")
                        .with(user("manager").roles("WAREHOUSE_MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void stockMovementRecordsAuthenticatedActor() throws Exception {
        String warehouseJson = mockMvc.perform(post("/api/warehouses")
                        .with(user("manager").roles("WAREHOUSE_MANAGER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"WH-AUDIT","name":"Audit Warehouse","location":"Test"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String productJson = mockMvc.perform(post("/api/products")
                        .with(user("manager").roles("WAREHOUSE_MANAGER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-AUDIT","name":"Audit Product","unitOfMeasure":"each"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String warehouseId = objectMapper.readTree(warehouseJson).get("id").asText();
        String productId = objectMapper.readTree(productJson).get("id").asText();
        String locationJson = mockMvc.perform(post("/api/locations")
                        .with(user("manager").roles("WAREHOUSE_MANAGER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"warehouseId":"%s","code":"ZONE-AUDIT","name":"Audit Zone","type":"ZONE"}
                                """.formatted(warehouseId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String locationId = objectMapper.readTree(locationJson).get("id").asText();

        mockMvc.perform(post("/api/inventory/receive")
                        .with(user("receiver-audit").roles("RECEIVER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","locationId":"%s","quantity":3,"reference":"AUDIT-01"}
                                """.formatted(productId, locationId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/inventory/movements")
                        .with(user("receiver-audit").roles("RECEIVER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].performedBy").value("receiver-audit"))
                .andExpect(jsonPath("$[0].reference").value("AUDIT-01"));
    }
}
