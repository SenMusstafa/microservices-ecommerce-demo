package com.mustafasen.legacysoapservice;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Minimal hand-rolled SOAP 1.1 endpoint: one operation, GetStockLevel.
 * Unknown SKUs produce a SOAP Fault (HTTP 500, as SOAP 1.1 specifies).
 */
@RestController
@RequestMapping("/ws")
public class WarehouseController {

    static final String NS = "http://legacy.example.com/warehouse";

    private record Entry(int quantity, String location) {
    }

    private static final Map<String, Entry> WAREHOUSE = Map.of(
            "product-1", new Entry(120, "Istanbul-A1"),
            "product-2", new Entry(15, "Ankara-B3"),
            "product-3", new Entry(0, "Izmir-C2"));

    @GetMapping(params = "wsdl", produces = MediaType.TEXT_XML_VALUE)
    public String wsdl() throws IOException {
        return new String(new ClassPathResource("warehouse.wsdl").getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
    }

    @PostMapping(consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE, "application/soap+xml"},
            produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<String> getStockLevel(@RequestBody String envelope) {
        String sku;
        try {
            sku = extractSku(envelope);
        } catch (Exception e) {
            return fault("soapenv:Client", "Malformed request: " + e.getMessage());
        }
        Entry entry = WAREHOUSE.get(sku);
        if (entry == null) {
            return fault("soapenv:Client", "Unknown SKU: " + sku);
        }
        String body = "<wh:GetStockLevelResponse>"
                + "<wh:sku>" + sku + "</wh:sku>"
                + "<wh:quantity>" + entry.quantity() + "</wh:quantity>"
                + "<wh:location>" + entry.location() + "</wh:location>"
                + "</wh:GetStockLevelResponse>";
        return ResponseEntity.ok(envelope(body));
    }

    static String extractSku(String envelope) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        var doc = factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(envelope.getBytes(StandardCharsets.UTF_8)));
        var nodes = doc.getElementsByTagNameNS(NS, "sku");
        if (nodes.getLength() == 0 || nodes.item(0).getTextContent().isBlank()) {
            throw new IllegalArgumentException("missing <sku>");
        }
        return nodes.item(0).getTextContent().trim();
    }

    private static ResponseEntity<String> fault(String code, String message) {
        String body = "<soapenv:Fault><faultcode>" + code + "</faultcode><faultstring>"
                + message.replace("&", "&amp;").replace("<", "&lt;") + "</faultstring></soapenv:Fault>";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(envelope(body));
    }

    private static String envelope(String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wh=\"" + NS + "\">"
                + "<soapenv:Body>" + body + "</soapenv:Body></soapenv:Envelope>";
    }
}
