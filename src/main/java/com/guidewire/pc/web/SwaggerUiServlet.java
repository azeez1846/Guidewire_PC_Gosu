package com.guidewire.pc.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class SwaggerUiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\">");
        out.println("  <title>Guidewire PolicyCenter REST API - Swagger UI</title>");
        out.println("  <link rel=\"stylesheet\" href=\"https://unpkg.com/swagger-ui-dist@5/swagger-ui.css\" />");
        out.println("  <style>html { box-sizing: border-box; overflow-y: scroll; } *, *:before, *:after { box-sizing: inherit; } body { margin:0; background: #fafafa; }</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("  <div id=\"swagger-ui\"></div>");
        out.println("  <script src=\"https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js\"></script>");
        out.println("  <script src=\"https://unpkg.com/swagger-ui-dist@5/swagger-ui-standalone-preset.js\"></script>");
        out.println("  <script>");
        out.println("    window.onload = function() {");
        out.println("      window.ui = SwaggerUIBundle({");
        out.println("        url: '/rest/v1/openapi.json',");
        out.println("        dom_id: '#swagger-ui',");
        out.println("        deepLinking: true,");
        out.println("        presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],");
        out.println("        plugins: [SwaggerUIBundle.plugins.DownloadUrl],");
        out.println("        layout: 'StandaloneLayout'");
        out.println("      });");
        out.println("    };");
        out.println("  </script>");
        out.println("</body>");
        out.println("</html>");
    }
}
