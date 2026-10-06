package com.mustafasen.legacysoapservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarehouseController.class)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static String request(String sku) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:wh=\"http://legacy.example.com/warehouse\"><soapenv:Body>"
                + "<wh:GetStockLevelRequest><wh:sku>" + sku + "</wh:sku></wh:GetStockLevelRequest>"
                + "</soapenv:Body></soapenv:Envelope>";
    }

    @Test
    void knownSku_returnsStockLevelEnvelope() throws Exception {
        mockMvc.perform(post("/ws").contentType(MediaType.TEXT_XML).content(request("product-1")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<wh:quantity>120</wh:quantity>")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Istanbul-A1")));
    }

    @Test
    void unknownSku_returnsSoapFault() throws Exception {
        mockMvc.perform(post("/ws").contentType(MediaType.TEXT_XML).content(request("nope")))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unknown SKU: nope")));
    }

    @Test
    void malformedXml_returnsSoapFault() throws Exception {
        mockMvc.perform(post("/ws").contentType(MediaType.TEXT_XML).content("<not-closed>"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Fault")));
    }

    @Test
    void wsdl_isServed() throws Exception {
        mockMvc.perform(get("/ws").param("wsdl", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("GetStockLevel")));
    }
}
