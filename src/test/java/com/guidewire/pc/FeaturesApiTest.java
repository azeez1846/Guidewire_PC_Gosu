package com.guidewire.pc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guidewire.pc.web.GuidewireRestServlet;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FeaturesApiTest {
    private GuidewireRestServlet servlet;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        servlet = new GuidewireRestServlet();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testAll29FeatureEndpoints() throws Exception {
        String[] endpoints = new String[]{
            "/ai-referral/evaluate",
            "/esignature/create",
            "/geospatial/risk",
            "/payment/process",
            "/vin/decode",
            "/telematics/evaluate",
            "/tria/evaluate",
            "/pollution/assess",
            "/cyber/evaluate",
            "/flood/rate",
            "/coinsurance/evaluate",
            "/deductible/buyback",
            "/uw/escalation",
            "/dividend/calculate",
            "/rate-cap/apply",
            "/siu-fraud/evaluate",
            "/fraud/evaluate",
            "/reinsurance/calculate",
            "/cat/evaluate",
            "/audit/calculate",
            "/emod/calculate",
            "/inland-marine/rate",
            "/cancellation/refund",
            "/multinational/ledger",
            "/commission/split",
            "/oos/merge",
            "/renewal/eligibility",
            "/uw-override/audit",
            "/coi/generate",
            "/ig/vehicle-details",
            "/ig/address-standardize"
        };

        for (String endpoint : endpoints) {
            String jsonInput = objectMapper.writeValueAsString(Map.of("jobNumber", "S0001001"));
            byte[] bytes = jsonInput.getBytes(StandardCharsets.UTF_8);

            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            InvocationHandler reqHandler = (proxy, method, args) -> {
                if (method == null) return null;
                String name = method.getName();
                if ("getMethod".equals(name)) return "POST";
                if ("getPathInfo".equals(name)) return endpoint;
                if ("getHeader".equals(name) && args != null && args.length > 0 && "Referer".equals(args[0])) return "http://localhost:8085/?page=features";
                if ("getInputStream".equals(name)) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                    return new ServletInputStream() {
                        @Override public boolean isFinished() { return bais.available() == 0; }
                        @Override public boolean isReady() { return true; }
                        @Override public void setReadListener(ReadListener readListener) {}
                        @Override public int read() { return bais.read(); }
                    };
                }
                if ("getCharacterEncoding".equals(name)) return "UTF-8";
                if ("getContentType".equals(name)) return "application/json";
                if ("toString".equals(name)) return "MockRequest";
                if ("hashCode".equals(name)) return 1;
                return null;
            };

            InvocationHandler respHandler = (proxy, method, args) -> {
                if (method == null) return null;
                String name = method.getName();
                if ("getWriter".equals(name)) return printWriter;
                if ("getCharacterEncoding".equals(name)) return "UTF-8";
                if ("toString".equals(name)) return "MockResponse";
                if ("hashCode".equals(name)) return 1;
                return null;
            };

            HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                reqHandler
            );

            HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                respHandler
            );

            servlet.service(req, resp);
            printWriter.flush();
            String output = stringWriter.toString();
            assertNotNull(output, "Output null for " + endpoint);
            assertTrue(output.contains("{") || output.contains("["), "Non-JSON response for " + endpoint + ": " + output);
        }
    }
}
