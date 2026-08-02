package com.guidewire.pc.web;

import com.guidewire.pc.pcf.PcfValidationEngine;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PcfStudioServlet extends HttpServlet {

    private final File rootDir;

    public PcfStudioServlet(File rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/index.html")) {
            serveStudioUI(resp);
            return;
        }

        if (pathInfo.equals("/api/list")) {
            listPcfFiles(resp);
            return;
        }

        if (pathInfo.equals("/api/read")) {
            readPcfFile(req, resp);
            return;
        }

        if (pathInfo.equals("/api/validate")) {
            validateDrop(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "PCF Studio endpoint not found: " + pathInfo);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/api/validate")) {
            validateDrop(req, resp);
            return;
        }

        if (pathInfo != null && pathInfo.equals("/api/save")) {
            savePcfFile(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void listPcfFiles(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        File pcfDir = new File(rootDir, "config/web/pcf");
        List<String> fileList = new ArrayList<>();

        if (pcfDir.exists()) {
            try (var stream = Files.walk(pcfDir.toPath())) {
                fileList = stream.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".pcf"))
                        .map(p -> pcfDir.toPath().relativize(p).toString())
                        .collect(Collectors.toList());
            }
        }

        PrintWriter out = resp.getWriter();
        out.print("[");
        for (int i = 0; i < fileList.size(); i++) {
            out.print("\"" + fileList.get(i).replace("\\", "/") + "\"");
            if (i < fileList.size() - 1) out.print(",");
        }
        out.print("]");
    }

    private void readPcfFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String relativePath = req.getParameter("path");
        if (relativePath == null || relativePath.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing path parameter");
            return;
        }

        File file = new File(rootDir, "config/web/pcf/" + relativePath);
        if (!file.exists() || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "PCF File not found: " + relativePath);
            return;
        }

        resp.setContentType("application/xml;charset=UTF-8");
        Files.copy(file.toPath(), resp.getOutputStream());
    }

    private void validateDrop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String childType = req.getParameter("childType");
        String parentType = req.getParameter("parentType");
        String widgetId = req.getParameter("widgetId");

        List<PcfValidationEngine.ValidationError> errors = PcfValidationEngine.validateWidgetPlacement(widgetId, childType, parentType);
        PrintWriter out = resp.getWriter();

        if (errors.isEmpty()) {
            out.print("{\"valid\": true, \"message\": \"Valid drop target inside <" + parentType + ">\"}");
        } else {
            PcfValidationEngine.ValidationError err = errors.get(0);
            String escapedMsg = err.getMessage().replace("\"", "\\\"");
            out.print("{\"valid\": false, \"error\": \"" + escapedMsg + "\", \"childType\": \"" + childType + "\", \"parentType\": \"" + parentType + "\"}");
        }
    }

    private void savePcfFile(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String relativePath = req.getParameter("path");
        String content = req.getReader().lines().collect(Collectors.joining("\n"));

        if (relativePath == null || relativePath.trim().isEmpty() || content.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing path or content");
            return;
        }

        File file = new File(rootDir, "config/web/pcf/" + relativePath);
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), content);

        PrintWriter out = resp.getWriter();
        out.print("{\"success\": true, \"message\": \"PCF saved successfully to " + file.getAbsolutePath() + "\"}");
    }

    private void serveStudioUI(HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\">");
        out.println("  <title>Guidewire PolicyCenter Visual PCF Studio</title>");
        out.println("  <style>");
        out.println("    :root { --bg-dark: #0f172a; --panel-bg: #1e293b; --accent: #3b82f6; --text-main: #f8fafc; --text-sub: #94a3b8; --border: #334155; --danger: #ef4444; --success: #22c55e; }");
        out.println("    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', system-ui, sans-serif; }");
        out.println("    body { background: var(--bg-dark); color: var(--text-main); display: flex; flex-direction: column; height: 100vh; overflow: hidden; }");
        out.println("    header { background: #090d16; border-bottom: 1px solid var(--border); padding: 12px 24px; display: flex; justify-content: space-between; align-items: center; }");
        out.println("    .title-badge { display: flex; align-items: center; gap: 12px; }");
        out.println("    .title-badge h1 { font-size: 1.1rem; font-weight: 600; color: #60a5fa; }");
        out.println("    .badge { background: #1e1b4b; color: #818cf8; border: 1px solid #3730a3; padding: 2px 8px; border-radius: 4px; font-size: 0.75rem; }");
        out.println("    .actions { display: flex; gap: 10px; }");
        out.println("    button { background: var(--accent); color: white; border: none; padding: 6px 14px; border-radius: 6px; font-size: 0.85rem; font-weight: 500; cursor: pointer; transition: 0.2s; }");
        out.println("    button:hover { filter: brightness(1.1); }");
        out.println("    button.secondary { background: #334155; }");
        out.println("    button.success { background: #16a34a; }");
        out.println("    .studio-container { display: flex; flex: 1; overflow: hidden; }");
        out.println("    /* Palette */");
        out.println("    .palette { width: 260px; background: var(--panel-bg); border-right: 1px solid var(--border); padding: 16px; overflow-y: auto; }");
        out.println("    .palette h3 { font-size: 0.8rem; text-transform: uppercase; color: var(--text-sub); margin-bottom: 12px; letter-spacing: 0.5px; }");
        out.println("    .widget-item { background: #0f172a; border: 1px solid var(--border); padding: 10px 12px; border-radius: 6px; margin-bottom: 8px; font-size: 0.85rem; cursor: grab; display: flex; align-items: center; justify-content: space-between; transition: all 0.2s; }");
        out.println("    .widget-item:hover { border-color: var(--accent); background: #1e1b4b; }");
        out.println("    .widget-type { color: #93c5fd; font-family: monospace; font-size: 0.75rem; }");
        out.println("    /* Canvas */");
        out.println("    .workspace { flex: 1; display: flex; flex-direction: column; background: #020617; }");
        out.println("    .canvas-bar { background: #0f172a; border-bottom: 1px solid var(--border); padding: 8px 16px; display: flex; justify-content: space-between; align-items: center; font-size: 0.85rem; }");
        out.println("    .canvas { flex: 1; padding: 24px; overflow-y: auto; }");
        out.println("    .pcf-container { background: var(--panel-bg); border: 2px dashed var(--border); border-radius: 8px; padding: 16px; margin-bottom: 16px; transition: border-color 0.2s, background 0.2s; min-height: 80px; }");
        out.println("    .pcf-container.drag-over-valid { border-color: var(--success) !important; background: rgba(34, 197, 94, 0.08) !important; }");
        out.println("    .pcf-container.drag-over-invalid { border-color: var(--danger) !important; background: rgba(239, 68, 68, 0.1) !important; }");
        out.println("    .container-header { font-size: 0.8rem; font-weight: 600; color: #a7f3d0; margin-bottom: 12px; display: flex; justify-content: space-between; font-family: monospace; }");
        out.println("    .widget-node { background: #0f172a; border: 1px solid var(--border); padding: 10px 14px; border-radius: 6px; margin-bottom: 8px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; transition: 0.2s; }");
        out.println("    .widget-node.selected { border-color: var(--accent); background: #1e1b4b; }");
        out.println("    .widget-node:hover { border-color: #60a5fa; }");
        out.println("    .widget-title { font-weight: 500; font-size: 0.85rem; }");
        out.println("    .widget-meta { font-family: monospace; font-size: 0.75rem; color: var(--text-sub); }");
        out.println("    /* Inspector */");
        out.println("    .inspector { width: 300px; background: var(--panel-bg); border-left: 1px solid var(--border); padding: 16px; overflow-y: auto; }");
        out.println("    .inspector h3 { font-size: 0.85rem; color: var(--text-sub); margin-bottom: 16px; text-transform: uppercase; }");
        out.println("    .prop-group { margin-bottom: 14px; }");
        out.println("    .prop-group label { display: block; font-size: 0.75rem; color: var(--text-sub); margin-bottom: 4px; }");
        out.println("    .prop-group input, .prop-group select { width: 100%; background: #0f172a; border: 1px solid var(--border); padding: 8px; border-radius: 6px; color: var(--text-main); font-size: 0.85rem; }");
        out.println("    /* Toast & Error Alert */");
        out.println("    .toast { position: fixed; bottom: 20px; right: 20px; background: var(--danger); color: white; padding: 12px 20px; border-radius: 8px; font-size: 0.85rem; box-shadow: 0 10px 25px rgba(0,0,0,0.5); display: none; z-index: 999; animation: slideIn 0.3s forwards; }");
        out.println("    @keyframes slideIn { from { transform: translateY(100%); opacity: 0; } to { transform: translateY(0); opacity: 1; } }");
        out.println("    .xml-preview { height: 180px; background: #020617; border-top: 1px solid var(--border); padding: 12px; font-family: monospace; font-size: 0.8rem; color: #a5f3fc; overflow: auto; white-space: pre; }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("  <header>");
        out.println("    <div class=\"title-badge\">");
        out.println("      <h1>Guidewire PolicyCenter Visual PCF Studio</h1>");
        out.println("      <span class=\"badge\">IntelliJ Drag &amp; Drop Guard Active</span>");
        out.println("    </div>");
        out.println("    <div class=\"actions\">");
        out.println("      <select id=\"pcfSelector\" onchange=\"loadSelectedPcf()\" style=\"background:#1e293b; color:white; border:1px solid #334155; padding:6px 12px; border-radius:6px;\"><option value=\"\">-- Select Existing PCF --</option></select>");
        out.println("      <button class=\"secondary\" onclick=\"createNewPcf()\">+ New PCF</button>");
        out.println("      <button class=\"success\" onclick=\"savePcf()\">💾 Save &amp; Export XML</button>");
        out.println("    </div>");
        out.println("  </header>");
        out.println("  <div class=\"studio-container\">");
        out.println("    <!-- Palette -->");
        out.println("    <div class=\"palette\">");
        out.println("      <h3>Layout Containers</h3>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'DetailViewPanel')\"><span>DetailViewPanel</span><span class=\"widget-type\">&lt;DetailView&gt;</span></div>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'InputColumn')\"><span>InputColumn</span><span class=\"widget-type\">&lt;Column&gt;</span></div>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'ListViewTile')\"><span>ListViewTile</span><span class=\"widget-type\">&lt;ListView&gt;</span></div>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'RowIterator')\"><span>RowIterator</span><span class=\"widget-type\">&lt;Iterator&gt;</span></div>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'Row')\"><span>Row</span><span class=\"widget-type\">&lt;Row&gt;</span></div>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'Toolbar')\"><span>Toolbar</span><span class=\"widget-type\">&lt;Toolbar&gt;</span></div>");
        out.println("      <h3 style=\"margin-top:20px;\">Form Inputs</h3>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'TextInput')\"><span>TextInput</span><span class=\"widget-type\">&lt;Input&gt;</span></div>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'SelectInput')\"><span>SelectInput</span><span class=\"widget-type\">&lt;Select&gt;</span></div>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'DateInput')\"><span>DateInput</span><span class=\"widget-type\">&lt;Date&gt;</span></div>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'CheckBoxInput')\"><span>CheckBoxInput</span><span class=\"widget-type\">&lt;Check&gt;</span></div>");
        out.println("      <h3 style=\"margin-top:20px;\">Table Cells</h3>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'TextCell')\"><span>TextCell</span><span class=\"widget-type\">&lt;Cell&gt;</span></div>");
        out.println("      <div class=\"widget-item\" draggable=\"true\" ondragstart=\"onDragStart(event, 'ToolbarButton')\"><span>ToolbarButton</span><span class=\"widget-type\">&lt;Button&gt;</span></div>");
        out.println("    </div>");
        out.println("    <!-- Workspace Canvas -->");
        out.println("    <div class=\"workspace\">");
        out.println("      <div class=\"canvas-bar\">");
        out.println("        <span id=\"canvasPcfName\">Editing: NewPolicyScreen.pcf</span>");
        out.println("        <span>Schema Validation Guard: <strong style=\"color:#22c55e;\">ENFORCED</strong></span>");
        out.println("      </div>");
        out.println("      <div class=\"canvas\" id=\"pcfCanvas\">");
        out.println("        <!-- Rendered PCF Tree -->");
        out.println("      </div>");
        out.println("      <div class=\"xml-preview\" id=\"xmlPreview\">&lt;!-- Live Formatted Guidewire XML Preview --&gt;</div>");
        out.println("    </div>");
        out.println("    <!-- Inspector -->");
        out.println("    <div class=\"inspector\">");
        out.println("      <h3>Widget Inspector</h3>");
        out.println("      <div id=\"inspectorForm\"><p style=\"color:var(--text-sub); font-size:0.85rem;\">Select a component on the canvas to configure properties.</p></div>");
        out.println("    </div>");
        out.println("  </div>");
        out.println("  <div class=\"toast\" id=\"errorToast\"></div>");
        out.println("  <script>");
        out.println("    let currentPcf = {");
        out.println("      id: 'WorkersCompLineDVTile',");
        out.println("      title: 'Workers Compensation Policy Line',");
        out.println("      children: [");
        out.println("        { type: 'DetailViewPanel', id: 'WCLineDV', children: [");
        out.println("            { type: 'InputColumn', id: 'Col1', title: 'Payroll Exposure', children: [");
        out.println("                { type: 'TextInput', id: 'TotalPayroll', label: 'Estimated Payroll', value: 'WCPolicyLine.TotalPayrollExposure', required: 'true' },");
        out.println("                { type: 'TextInput', id: 'ExperienceMod', label: 'Experience Mod', value: 'WCPolicyLine.ExperienceMod', required: 'true' }");
        out.println("            ]}");
        out.println("          ]}");
        out.println("      ]");
        out.println("    };");
        out.println("    let selectedNode = null;");
        out.println("    window.onload = function() { loadPcfFileList(); renderCanvas(); };");
        out.println("    function loadPcfFileList() {");
        out.println("      fetch('/pcf-studio/api/list').then(r => r.json()).then(files => {");
        out.println("        const sel = document.getElementById('pcfSelector');");
        out.println("        files.forEach(f => { const opt = document.createElement('option'); opt.value = f; opt.innerText = f; sel.appendChild(opt); });");
        out.println("      });");
        out.println("    }");
        out.println("    function onDragStart(ev, widgetType) { ev.dataTransfer.setData('text/plain', widgetType); }");
        out.println("    function renderCanvas() {");
        out.println("      const canvas = document.getElementById('pcfCanvas');");
        out.println("      canvas.innerHTML = '';");
        out.println("      currentPcf.children.forEach(c => canvas.appendChild(createNodeElem(c, currentPcf)));");
        out.println("      updateXmlPreview();");
        out.println("    }");
        out.println("    function createNodeElem(node, parent) {");
        out.println("      const isContainer = ['DetailViewPanel', 'DetailViewTile', 'InputColumn', 'ListViewTile', 'ListViewPanel', 'RowIterator', 'Row', 'Toolbar'].includes(node.type);");
        out.println("      if (isContainer) {");
        out.println("        const div = document.createElement('div');");
        out.println("        div.className = 'pcf-container';");
        out.println("        div.setAttribute('data-type', node.type);");
        out.println("        div.innerHTML = `<div class=\"container-header\"><span>&lt;${node.type} id=\"${node.id}\"&gt;</span><span>Container</span></div>`;");
        out.println("        div.ondragover = (e) => onDragOverContainer(e, node.type, div);");
        out.println("        div.ondragleave = () => { div.classList.remove('drag-over-valid', 'drag-over-invalid'); };");
        out.println("        div.ondrop = (e) => onDropOnContainer(e, node);");
        out.println("        if (node.children) { node.children.forEach(ch => div.appendChild(createNodeElem(ch, node))); }");
        out.println("        return div;");
        out.println("      } else {");
        out.println("        const div = document.createElement('div');");
        out.println("        div.className = 'widget-node' + (selectedNode === node ? ' selected' : '');");
        out.println("        div.innerHTML = `<div><div class=\"widget-title\">${node.label || node.id}</div><div class=\"widget-meta\">${node.type} (${node.value || ''})</div></div><button style=\"background:#ef4444; padding:2px 8px;\" onclick=\"deleteNode('${node.id}', event)\">✕</button>`;");
        out.println("        div.onclick = (e) => { e.stopPropagation(); selectWidget(node); };");
        out.println("        return div;");
        out.println("      }");
        out.println("    }");
        out.println("    function onDragOverContainer(ev, parentType, elem) {");
        out.println("      ev.preventDefault();");
        out.println("      const childType = ev.dataTransfer.types.includes('text/plain') ? 'TextInput' : 'TextInput';");
        out.println("      // Visual drop target validation");
        out.println("      elem.classList.add('drag-over-valid');");
        out.println("    }");
        out.println("    function onDropOnContainer(ev, parentNode) {");
        out.println("      ev.preventDefault();");
        out.println("      ev.stopPropagation();");
        out.println("      const childType = ev.dataTransfer.getData('text/plain');");
        out.println("      if (!childType) return;");
        out.println("      // Validate with backend IntelliJ drop guard engine");
        out.println("      fetch(`/pcf-studio/api/validate?childType=${childType}&parentType=${parentNode.type}&widgetId=new`).then(r => r.json()).then(res => {");
        out.println("        if (!res.valid) {");
        out.println("          showToast(res.error);");
        out.println("        } else {");
        out.println("          if (!parentNode.children) parentNode.children = [];");
        out.println("          parentNode.children.push({ type: childType, id: childType + '_' + Math.floor(Math.random()*1000), label: 'New ' + childType, value: 'PolicyPeriod.Value' });");
        out.println("          renderCanvas();");
        out.println("        }");
        out.println("      });");
        out.println("    }");
        out.println("    function showToast(msg) {");
        out.println("      const t = document.getElementById('errorToast');");
        out.println("      t.innerText = msg; t.style.display = 'block';");
        out.println("      setTimeout(() => { t.style.display = 'none'; }, 4000);");
        out.println("    }");
        out.println("    function selectWidget(node) {");
        out.println("      selectedNode = node; renderCanvas();");
        out.println("      const form = document.getElementById('inspectorForm');");
        out.println("      form.innerHTML = `");
        out.println("        <div class=\"prop-group\"><label>Widget Type</label><input type=\"text\" value=\"${node.type}\" readonly style=\"opacity:0.6;\"></div>");
        out.println("        <div class=\"prop-group\"><label>Widget ID</label><input type=\"text\" id=\"inp_id\" value=\"${node.id}\" onchange=\"updateSelectedProp('id', this.value)\"></div>");
        out.println("        <div class=\"prop-group\"><label>Label</label><input type=\"text\" id=\"inp_label\" value=\"${node.label || ''}\" onchange=\"updateSelectedProp('label', this.value)\"></div>");
        out.println("        <div class=\"prop-group\"><label>Value (Gosu Binding)</label><input type=\"text\" id=\"inp_val\" value=\"${node.value || ''}\" onchange=\"updateSelectedProp('value', this.value)\"></div>");
        out.println("      `;");
        out.println("    }");
        out.println("    function updateSelectedProp(prop, val) { if (selectedNode) { selectedNode[prop] = val; renderCanvas(); } }");
        out.println("    function updateXmlPreview() {");
        out.println("      const xml = generateXml(currentPcf);");
        out.println("      document.getElementById('xmlPreview').innerText = xml;");
        out.println("    }");
        out.println("    function generateXml(pcf) {");
        out.println("      let xml = '<?xml version=\"1.0\" encoding=\"UTF-8\"?>\\n<PCF xmlns=\"http://guidewire.com/pcf\" id=\"' + pcf.id + '\" title=\"' + pcf.title + '\">\\n';");
        out.println("      xml += renderChildrenXml(pcf.children, '  ');");
        out.println("      xml += '</PCF>'; return xml;");
        out.println("    }");
        out.println("    function renderChildrenXml(children, indent) {");
        out.println("      if (!children) return '';");
        out.println("      let res = '';");
        out.println("      children.forEach(c => {");
        out.println("        if (c.children) {");
        out.println("          res += `${indent}<${c.type} id=\"${c.id}\"${c.title ? ' title=\"'+c.title+'\"' : ''}>\\n`;");
        out.println("          res += renderChildrenXml(c.children, indent + '  ');");
        out.println("          res += `${indent}</${c.type}>\\n`;");
        out.println("        } else {");
        out.println("          res += `${indent}<${c.type} id=\"${c.id}\" label=\"${c.label || ''}\" value=\"${c.value || ''}\"/>\\n`;");
        out.println("        }");
        out.println("      });");
        out.println("      return res;");
        out.println("    }");
        out.println("    function savePcf() {");
        out.println("      const xml = generateXml(currentPcf);");
        out.println("      const path = 'submission/' + currentPcf.id + '.pcf';");
        out.println("      fetch('/pcf-studio/api/save?path=' + encodeURIComponent(path), { method: 'POST', body: xml }).then(r => r.json()).then(res => {");
        out.println("        alert(res.message);");
        out.println("      });");
        out.println("    }");
        out.println("    function loadSelectedPcf() {");
        out.println("      const path = document.getElementById('pcfSelector').value;");
        out.println("      if (!path) return;");
        out.println("      fetch('/pcf-studio/api/read?path=' + encodeURIComponent(path)).then(r => r.text()).then(xmlText => {");
        out.println("        document.getElementById('xmlPreview').innerText = xmlText;");
        out.println("        document.getElementById('canvasPcfName').innerText = 'Editing: ' + path;");
        out.println("      });");
        out.println("    }");
        out.println("    function createNewPcf() { currentPcf = { id: 'NewCustomDVTile', title: 'New Custom PCF Tile', children: [{ type: 'DetailViewPanel', id: 'DV1', children: [{ type: 'InputColumn', id: 'Col1', children: [] }] }] }; renderCanvas(); }");
        out.println("  </script>");
        out.println("</body>");
        out.println("</html>");
    }
}
