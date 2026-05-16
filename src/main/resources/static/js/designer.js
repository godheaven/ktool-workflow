var id = document.getElementById("drawflow");
const editor = new Drawflow(id);

// Config
editor.reroute = true;
editor.start();

// State
let selectedNodeId = null;

// Initialize Lucide
lucide.createIcons();

/* --- Node Templates --- */
function getNodeHtml(type, label) {
    const icons = {
        'START': 'play-circle',
        'END': 'stop-circle',
        'TASK': 'clipboard-list',
        'FORM': 'file-input',
        'APPROVAL': 'user-check',
        'RULE': 'git-merge',
        'AUTOMATION': 'cpu',
        'NOTIFICATION': 'bell',
        'GATEWAY': 'share-2'
    };
    
    const icon = icons[type] || 'settings';
    const headerClass = 'header-' + type.toLowerCase();
    
    return `
        <div class="drawflow-node-content">
            <div class="node-header ${headerClass}">
                <div class="node-icon" data-lucide="${icon}"></div>
                <span>${type}</span>
            </div>
            <div class="node-body">
                <div class="node-label">${label}</div>
            </div>
        </div>
    `;
}

/* --- Drag and Drop --- */
function allowDrop(ev) {
    ev.preventDefault();
}

function drag(ev) {
    const item = ev.target.closest(".toolbox-item");
    if (!item) return;
    const nodeType = item.getAttribute('data-node');
    console.log("Dragging node type:", nodeType);
    ev.dataTransfer.setData("node", nodeType);
}

function drop(ev) {
    ev.preventDefault();
    const data = ev.dataTransfer.getData("node");
    console.log("Dropped node type:", data, "at", ev.clientX, ev.clientY);
    if (data) {
        addNodeToDrawflow(data, ev.clientX, ev.clientY);
    }
}

function addNodeToDrawflow(type, pos_x, pos_y) {
    console.log("Adding node to Drawflow:", type);
    if (editor.editor_mode === 'view') {
        console.warn("Editor is in view mode, cannot add node");
        return;
    }
    
    // Coordinate calculation
    const rect = editor.precanvas.getBoundingClientRect();
    pos_x = (pos_x - rect.left) / (rect.width / editor.precanvas.clientWidth) / editor.zoom;
    pos_y = (pos_y - rect.top) / (rect.height / editor.precanvas.clientHeight) / editor.zoom;

    const label = type.charAt(0) + type.slice(1).toLowerCase();
    const html = getNodeHtml(type, label);
    
    // Inputs/Outputs based on type
    let inputs = 1;
    let outputs = 1;
    
    if (type === 'START') inputs = 0;
    if (type === 'END') outputs = 0;
    if (type === 'RULE') outputs = 2; // Default 2 outcomes

    const nodeData = { 
        type: type, 
        config: { fields: [] }, 
        slaHours: 24 
    };
    
    const nodeId = editor.addNode(type, inputs, outputs, pos_x, pos_y, type.toLowerCase(), nodeData, html);
    
    setTimeout(() => lucide.createIcons(), 0);
    return nodeId;
}

/* --- Events --- */
editor.on('nodeSelected', function(id) {
    selectedNodeId = id;
    showProperties(id);
});

editor.on('nodeUnselected', function() {
    selectedNodeId = null;
    hideProperties();
});

editor.on('nodeRemoved', function(id) {
    if (selectedNodeId == id) {
        selectedNodeId = null;
        hideProperties();
    }
});

/* --- Properties Panel --- */
function showProperties(nodeId) {
    const node = editor.getNodeFromId(nodeId);
    document.getElementById('no-node-selected').style.display = 'none';
    document.getElementById('node-properties').style.display = 'block';
    
    document.getElementById('prop-id').value = nodeId;
    document.getElementById('prop-label').value = node.data.label || node.name;
    document.getElementById('prop-type-label').innerText = node.name + ' Properties';
    document.getElementById('prop-sla').value = node.data.slaHours || 0;
    
    // Show/Hide sections
    document.querySelectorAll('.type-section').forEach(s => s.style.display = 'none');
    const section = document.getElementById('section-' + node.name.toLowerCase());
    if (section) section.style.display = 'block';
    
    if (node.name === 'FORM') {
        renderFormFields(node.data.config.fields || []);
    }
    
    if (node.name === 'APPROVAL') {
        document.getElementById('prop-approval-mode').value = node.data.config.approvalMode || 'SINGLE_APPROVER';
    }
    
    lucide.createIcons();
}

function hideProperties() {
    document.getElementById('no-node-selected').style.display = 'block';
    document.getElementById('node-properties').style.display = 'none';
}

function updateNodeLabel(val) {
    const node = editor.getNodeFromId(selectedNodeId);
    node.data.label = val;
    const labelEl = document.querySelector(`#node-${selectedNodeId} .node-label`);
    if (labelEl) labelEl.innerText = val;
}

function updateNodeSla(val) {
    const node = editor.getNodeFromId(selectedNodeId);
    node.data.slaHours = parseInt(val);
}

function updateNodeConfig(key, val) {
    const node = editor.getNodeFromId(selectedNodeId);
    node.data.config[key] = val;
}

/* --- Form Builder Logic --- */
function renderFormFields(fields) {
    const list = document.getElementById('fields-list');
    list.innerHTML = '';
    
    fields.forEach((field, index) => {
        const div = document.createElement('div');
        div.className = 'field-item';
        div.innerHTML = `
            <div class="field-header">
                <span>${field.label || 'Unnamed Field'}</span>
                <div class="field-actions">
                    <button class="btn-icon" onclick="removeField(${index})"><i data-lucide="trash-2"></i></button>
                </div>
            </div>
            <div style="font-size: 0.7rem; color: var(--text-muted);">${field.type}</div>
        `;
        list.appendChild(div);
    });
    lucide.createIcons();
}

function addField() {
    const node = editor.getNodeFromId(selectedNodeId);
    if (!node.data.config.fields) node.data.config.fields = [];
    
    const newField = {
        id: 'field_' + Date.now(),
        label: 'New Field',
        type: 'TEXT',
        required: false
    };
    
    node.data.config.fields.push(newField);
    renderFormFields(node.data.config.fields);
}

function removeField(index) {
    const node = editor.getNodeFromId(selectedNodeId);
    node.data.config.fields.splice(index, 1);
    renderFormFields(node.data.config.fields);
}

/* --- API Integration --- */
async function loadWorkflow() {
    try {
        const response = await fetch(`${apiBase}/${workflowId}/model`);
        const data = await response.json();
        console.log("Workflow data loaded from API:", data);
        
        if (data.nodes && data.nodes.length > 0) {
            // Transform our DTO to Drawflow format
            const drawflowData = {
                drawflow: {
                    Home: {
                        data: {}
                    }
                }
            };
            
            data.nodes.forEach(node => {
                const html = getNodeHtml(node.type, node.label);
                
                // Estimate inputs/outputs based on type
                let inputs = 1;
                let outputs = 1;
                if (node.type === 'START') inputs = 0;
                if (node.type === 'END') outputs = 0;
                if (node.type === 'RULE') outputs = 2;

                drawflowData.drawflow.Home.data[node.id] = {
                    id: parseInt(node.id),
                    name: node.type,
                    data: { 
                        type: node.type, 
                        label: node.label, 
                        config: node.config || {}, 
                        slaHours: node.slaHours 
                    },
                    class: node.type.toLowerCase(),
                    html: html,
                    typenode: false,
                    inputs: {},
                    outputs: {},
                    pos_x: node.x,
                    pos_y: node.y
                };
                
                // We'll fill inputs/outputs later based on edges
                for(let i=1; i<=inputs; i++) drawflowData.drawflow.Home.data[node.id].inputs[`input_${i}`] = { connections: [] };
                for(let i=1; i<=outputs; i++) drawflowData.drawflow.Home.data[node.id].outputs[`output_${i}`] = { connections: [] };
            });
            
            // Add edges
            data.edges.forEach(edge => {
                const sourceNode = drawflowData.drawflow.Home.data[edge.source];
                const targetNode = drawflowData.drawflow.Home.data[edge.target];
                
                if (sourceNode && targetNode) {
                    const outputName = edge.sourceOutput || 'output_1';
                    const inputName = edge.targetInput || 'input_1';
                    
                    // Add to source output
                    if (!sourceNode.outputs[outputName]) sourceNode.outputs[outputName] = { connections: [] };
                    sourceNode.outputs[outputName].connections.push({
                        node: edge.target.toString(),
                        input: inputName
                    });
                    
                    // Add to target input (Crucial for Drawflow rendering)
                    if (!targetNode.inputs[inputName]) targetNode.inputs[inputName] = { connections: [] };
                    targetNode.inputs[inputName].connections.push({
                        node: edge.source.toString(),
                        input: outputName // In Drawflow's input connections, 'input' field actually refers to the source output name
                    });
                }
            });
            
            // Drawflow rendering is tricky with Lucide. We'll try multiple times.
            const initIcons = () => {
                try {
                    lucide.createIcons();
                    console.log("Lucide icons triggered");
                } catch (e) {
                    console.error("Lucide error:", e);
                }
            };
            
            editor.import(drawflowData);
            setTimeout(initIcons, 100);
            setTimeout(initIcons, 500);
            setTimeout(initIcons, 1500);
        }
    } catch (error) {
        console.error("Error loading workflow", error);
    }
}

async function saveWorkflow() {
    const exportData = editor.export();
    const nodes = [];
    const edges = [];
    
    const drawflowNodes = exportData.drawflow.Home.data;
    Object.keys(drawflowNodes).forEach(key => {
        const n = drawflowNodes[key];
        nodes.push({
            id: n.id.toString(),
            type: n.data.type || n.name,
            label: n.data.label || n.name,
            x: n.pos_x,
            y: n.pos_y,
            config: n.data.config,
            slaHours: n.data.slaHours
        });
        
        Object.keys(n.outputs).forEach(outKey => {
            n.outputs[outKey].connections.forEach(conn => {
                edges.push({
                    source: n.id.toString(),
                    target: conn.node,
                    sourceOutput: outKey,
                    targetInput: conn.input || conn.output
                });
            });
        });
    });
    
    const payload = { nodes, edges };
    
    try {
        const response = await fetch(`${apiBase}/${workflowId}/model`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        if (response.ok) {
            showToast("Workflow saved successfully", "success");
        } else {
            showToast("Error saving workflow", "error");
        }
    } catch (error) {
        console.error("Error saving workflow", error);
        showToast("Network error while saving", "error");
    }
}

async function validateWorkflow() {
    try {
        const response = await fetch(`${apiBase}/${workflowId}/validate`);
        const errors = await response.json();
        
        if (errors.length === 0) {
            showToast("Workflow is valid!", "success");
        } else {
            alert("Validation Errors:\n- " + errors.join("\n- "));
        }
    } catch (error) {
        showToast("Error validating workflow", "error");
    }
}

function previewWorkflow() {
    const exportData = editor.export();
    console.log("Current Workflow State:", exportData);
    alert("Preview feature: Check console for workflow JSON structure.");
}

function showToast(message, type) {
    // Simple alert for now, could be a pretty toast
    console.log(`[${type}] ${message}`);
    // I'll add a simple toast UI if time permits, for now alert is fine but less "premium"
    const toast = document.createElement('div');
    toast.style.position = 'fixed';
    toast.style.bottom = '20px';
    toast.style.left = '50%';
    toast.style.transform = 'translateX(-50%)';
    toast.style.padding = '12px 24px';
    toast.style.borderRadius = '8px';
    toast.style.color = 'white';
    toast.style.zIndex = '1000';
    toast.style.background = type === 'success' ? '#22c55e' : '#ef4444';
    toast.innerText = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Initial Load
document.addEventListener('DOMContentLoaded', loadWorkflow);
