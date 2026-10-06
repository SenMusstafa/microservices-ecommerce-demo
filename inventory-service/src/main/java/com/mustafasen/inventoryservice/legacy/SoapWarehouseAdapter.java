package com.mustafasen.inventoryservice.legacy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.NoSuchElementException;

/**
 * Adapter: speaks SOAP/XML to the legacy warehouse system and exposes it as
 * a plain Java call (and, through the controller, as JSON over REST).
 */
@Component
public class SoapWarehouseAdapter implements WarehouseGateway {

    static final String NS = "http://legacy.example.com/warehouse";

    private final RestTemplate restTemplate;
    private final String url;

    public SoapWarehouseAdapter(RestTemplateBuilder builder,
                                @Value("${legacy.warehouse.url:http://localhost:8090/ws}") String url) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
        this.url = url;
    }

    @Override
    public WarehouseStockResponse getWarehouseStock(String sku) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.add("SOAPAction", "GetStockLevel");
        String body;
        try {
            body = restTemplate.postForObject(url, new HttpEntity<>(buildRequest(sku), headers), String.class);
        } catch (HttpStatusCodeException e) {
            // SOAP 1.1 reports faults with HTTP 500 and a Fault element in the body.
            String fault = faultString(e.getResponseBodyAsString());
            if (fault != null) {
                throw new NoSuchElementException(fault);
            }
            throw new LegacyUnavailableException("Legacy warehouse returned " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            throw new LegacyUnavailableException("Legacy warehouse is unreachable", e);
        }
        return parseResponse(body);
    }

    static String buildRequest(String sku) {
        String escaped = sku.replace("&", "&amp;").replace("<", "&lt;");
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wh=\"" + NS + "\">"
                + "<soapenv:Body><wh:GetStockLevelRequest><wh:sku>" + escaped + "</wh:sku></wh:GetStockLevelRequest>"
                + "</soapenv:Body></soapenv:Envelope>";
    }

    static WarehouseStockResponse parseResponse(String xml) {
        try {
            var doc = parse(xml);
            return new WarehouseStockResponse(
                    text(doc, "sku"),
                    Integer.parseInt(text(doc, "quantity")),
                    text(doc, "location"));
        } catch (Exception e) {
            throw new LegacyUnavailableException("Unreadable response from legacy warehouse", e);
        }
    }

    static String faultString(String xml) {
        try {
            var nodes = parse(xml).getElementsByTagName("faultstring");
            return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent();
        } catch (Exception e) {
            return null;
        }
    }

    private static org.w3c.dom.Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String text(org.w3c.dom.Document doc, String name) {
        var nodes = doc.getElementsByTagNameNS(NS, name);
        if (nodes.getLength() == 0) {
            throw new IllegalArgumentException("missing <" + name + ">");
        }
        return nodes.item(0).getTextContent().trim();
    }
}
