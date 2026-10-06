package com.mustafasen.inventoryservice.legacy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SoapWarehouseAdapterTest {

    private static final String URL = "http://legacy/ws";
    private static final String NS = "http://legacy.example.com/warehouse";

    private MockRestServiceServer server;
    private SoapWarehouseAdapter adapter;

    @BeforeEach
    void setUp() throws Exception {
        RestTemplateBuilder builder = new RestTemplateBuilder();
        adapter = new SoapWarehouseAdapter(builder, URL);
        // swap the adapter's RestTemplate for one wired to the mock server
        var field = SoapWarehouseAdapter.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        RestTemplate restTemplate = (RestTemplate) field.get(adapter);
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    private static String envelope(String body) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wh=\"" + NS
                + "\"><soapenv:Body>" + body + "</soapenv:Body></soapenv:Envelope>";
    }

    @Test
    void sendsSoapRequestAndMapsResponseToDto() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("SOAPAction", "GetStockLevel"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<wh:sku>product-1</wh:sku>")))
                .andRespond(withSuccess(envelope("<wh:GetStockLevelResponse><wh:sku>product-1</wh:sku>"
                        + "<wh:quantity>120</wh:quantity><wh:location>Istanbul-A1</wh:location>"
                        + "</wh:GetStockLevelResponse>"), MediaType.TEXT_XML));

        WarehouseStockResponse response = adapter.getWarehouseStock("product-1");

        assertThat(response.getSku()).isEqualTo("product-1");
        assertThat(response.getQuantity()).isEqualTo(120);
        assertThat(response.getLocation()).isEqualTo("Istanbul-A1");
        server.verify();
    }

    @Test
    void soapFault_becomesNoSuchElement() {
        server.expect(requestTo(URL)).andRespond(withServerError().contentType(MediaType.TEXT_XML)
                .body(envelope("<soapenv:Fault><faultcode>soapenv:Client</faultcode>"
                        + "<faultstring>Unknown SKU: nope</faultstring></soapenv:Fault>")));

        assertThatThrownBy(() -> adapter.getWarehouseStock("nope"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Unknown SKU: nope");
    }

    @Test
    void connectionFailure_becomesLegacyUnavailable() {
        server.expect(requestTo(URL)).andRespond(request -> {
            throw new IOException("Connection refused");
        });

        assertThatThrownBy(() -> adapter.getWarehouseStock("product-1"))
                .isInstanceOf(LegacyUnavailableException.class);
    }

    @Test
    void non500ErrorWithoutFault_becomesLegacyUnavailable() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> adapter.getWarehouseStock("product-1"))
                .isInstanceOf(LegacyUnavailableException.class);
    }

    @Test
    void garbageResponse_becomesLegacyUnavailable() {
        server.expect(requestTo(URL)).andRespond(withSuccess("not xml", MediaType.TEXT_XML));

        assertThatThrownBy(() -> adapter.getWarehouseStock("product-1"))
                .isInstanceOf(LegacyUnavailableException.class);
    }
}
