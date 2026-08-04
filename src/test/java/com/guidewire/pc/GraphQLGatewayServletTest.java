package com.guidewire.pc;

import com.guidewire.pc.web.GraphQLGatewayServlet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class GraphQLGatewayServletTest {

    @Test
    @DisplayName("Test GraphQL GET schema response")
    public void testGraphQLGetSchema() throws Exception {
        GraphQLGatewayServlet servlet = new GraphQLGatewayServlet();
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        DummyRequest req = new DummyRequest("");
        DummyResponse resp = new DummyResponse(writer);

        servlet.service(req, resp);

        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("GraphQL Gateway Active"));
        assertTrue(output.contains("/graphql"));
    }

    @Test
    @DisplayName("Test GraphQL POST Query execution")
    public void testGraphQLPostQuery() throws Exception {
        GraphQLGatewayServlet servlet = new GraphQLGatewayServlet();
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        String jsonQuery = "{\"query\": \"query { policy(id: \\\"POL-849102\\\") { policyNumber, status } }\"}";
        DummyRequest req = new DummyRequest(jsonQuery);
        req.setMethod("POST");
        DummyResponse resp = new DummyResponse(writer);

        servlet.service(req, resp);

        writer.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("POL-849102"));
        assertTrue(output.contains("In Force"));
    }

    // Dummy Servlet classes for testing without external Mockito dependency
    private static class DummyRequest extends jakarta.servlet.http.HttpServletRequestWrapper {
        private final String body;
        private String method = "GET";

        public DummyRequest(String body) {
            super(new org.eclipse.jetty.server.Request(null, null));
            this.body = body;
        }

        public void setMethod(String m) { this.method = m; }

        @Override
        public String getMethod() { return method; }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new StringReader(body));
        }
    }

    private static class DummyResponse extends jakarta.servlet.http.HttpServletResponseWrapper {
        private final PrintWriter writer;

        public DummyResponse(PrintWriter writer) {
            super(new org.eclipse.jetty.server.Response(null, null));
            this.writer = writer;
        }

        @Override
        public PrintWriter getWriter() { return writer; }

        @Override
        public void setContentType(String type) {}

        @Override
        public void setStatus(int sc) {}
    }
}
