// Global state
let editor;
let selectedNodeId = null;
let editingNodeId = null; // ID of the node currently being edited in the modal
let currentEditingFieldIndex = null;
let currentActiveQuestionTab = 'general';

function switchQuestionTab(tabName, event) {
    if (event) event.stopPropagation();
    currentActiveQuestionTab = tabName;
    renderEnterpriseQuestions();
}

// Helper to get live node data from Drawflow instead of a deep clone
function getNodeById(id) {
    if (!editor || !editor.drawflow || !editor.drawflow.drawflow) return null;
    const module = editor.module || 'Home';
    return editor.drawflow.drawflow[module].data[parseInt(id)];
}

/* --- Constants --- */
const SUPPORTED_FIELD_TYPES = [
    'TEXT', 'TEXTAREA', 'EMAIL', 'PHONE', 'URL', 'PASSWORD', 'COLOR',
    'NUMBER', 'DECIMAL', 'DATE', 'DATETIME', 'BOOLEAN', 'RADIO', 
    'CHECKBOX', 'SELECT', 'MULTISELECT', 'FILE', 'IMAGE', 'SIGNATURE',
    'USER_PICKER', 'ROLE_PICKER', 'ORG_PICKER', 'TABLE', 'REPEATER', 
    'RICH_TEXT', 'HIDDEN', 'CALCULATED', 'DISPLAY_TEXT'
];

/* --- Initialization --- */
document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById("drawflow");
    if (!container) return;

    editor = new Drawflow(container);
    editor.reroute = true;
    editor.start();

    // Attach events
    editor.on('nodeSelected', (id) => {
        selectedNodeId = id;
        showProperties(id);
    });

    editor.on('nodeUnselected', () => {
        selectedNodeId = null;
        hideProperties();
    });

    editor.on('nodeRemoved', (id) => {
        if (selectedNodeId == id) {
            selectedNodeId = null;
            hideProperties();
        }
    });

    // Initial load
    loadWorkflow();
    lucide.createIcons();
});

/* --- Node Templates --- */
function getNodeHtml(type, label) {
    const icons = {
        'START': 'play-circle', 'END': 'stop-circle', 'TASK': 'clipboard-list',
        'FORM': 'file-input', 'APPROVAL': 'user-check', 'RULE': 'git-merge',
        'AUTOMATION': 'cpu', 'NOTIFICATION': 'bell', 'GATEWAY': 'share-2'
    };
    const icon = icons[type] || 'settings';
    return `
        <div class="drawflow-node-content">
            <div class="node-header header-${type.toLowerCase()}">
                <div class="node-icon" data-lucide="${icon}"></div>
                <span>${type}</span>
            </div>
            <div class="node-body"><div class="node-label">${label}</div></div>
        </div>`;
}

/* --- Drag and Drop --- */
function allowDrop(ev) { ev.preventDefault(); }
function drag(ev) {
    const item = ev.target.closest(".toolbox-item");
    if (item) ev.dataTransfer.setData("node", item.getAttribute('data-node'));
}
function drop(ev) {
    ev.preventDefault();
    const data = ev.dataTransfer.getData("node");
    if (data) addNodeToDrawflow(data, ev.clientX, ev.clientY);
}

function addNodeToDrawflow(type, pos_x, pos_y) {
    const rect = editor.precanvas.getBoundingClientRect();
    pos_x = (pos_x - rect.left) / (rect.width / editor.precanvas.clientWidth) / editor.zoom;
    pos_y = (pos_y - rect.top) / (rect.height / editor.precanvas.clientHeight) / editor.zoom;
    const label = type.charAt(0) + type.slice(1).toLowerCase();
    const html = getNodeHtml(type, label);
    const nodeData = { type: type, label: label, config: { fields: [] }, slaHours: 24 };
    editor.addNode(type, type==='START'?0:1, type==='END'?0:(type==='RULE'?2:1), pos_x, pos_y, type.toLowerCase(), nodeData, html);
    setTimeout(() => lucide.createIcons(), 0);
}

/* --- Properties Panel --- */
function showProperties(nodeId) {
    const node = getNodeById(nodeId);
    document.getElementById('no-node-selected').style.display = 'none';
    document.getElementById('node-properties').style.display = 'block';
    document.getElementById('prop-label').value = node.data.label || node.name;
    document.getElementById('prop-sla').value = node.data.slaHours || 0;
    document.querySelectorAll('.type-section').forEach(s => s.style.display = 'none');
    const section = document.getElementById('section-' + node.name.toLowerCase());
    if (section) section.style.display = 'block';
    if (node.name === 'FORM') updateFormStats(node);
    lucide.createIcons();
}

function updateFormStats(node) {
    const fields = node.data.config.fields || [];
    document.getElementById('side-stat-total').innerText = fields.length;
    document.getElementById('side-stat-mandatory').innerText = fields.filter(f => f.required).length;
}

function hideProperties() {
    document.getElementById('no-node-selected').style.display = 'block';
    document.getElementById('node-properties').style.display = 'none';
}

function updateNodeLabel(val) {
    const node = getNodeById(selectedNodeId);
    node.data.label = val;
    const el = document.querySelector(`#node-${selectedNodeId} .node-label`);
    if (el) el.innerText = val;
}

function updateNodeSla(val) {
    const node = getNodeById(selectedNodeId);
    if (node) node.data.slaHours = parseInt(val);
}

/* --- Form Builder Logic --- */
function generateKey(label) {
    if (!label) return 'QST_QUESTION';
    
    // 1. Remove accents & diacritics
    let normalized = label.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
    
    // 2. Convert to uppercase and tokenize by spaces, underscores, hyphens
    let tokens = normalized.toUpperCase().split(/[\s_\-]+/);
    
    // 3. Filter stop words
    const stopWords = new Set(['THE', 'OF', 'AND', 'OR', 'DE', 'DEL', 'LA', 'EL', 'LOS', 'LAS', 'Y']);
    let filteredTokens = tokens.filter(t => t.trim() && !stopWords.has(t));
    
    if (filteredTokens.length === 0) {
        filteredTokens = ["QUESTION"];
    }
    
    // 4. Join with underscores and clean special characters
    let cleaned = filteredTokens.join("_").replace(/[^A-Z0-9_]/g, "");
    
    // 5. Deduplicate underscores and strip leading/trailing ones
    cleaned = cleaned.replace(/_+/g, "_").replace(/^_+|_+$/g, "");
    
    // 6. Protect numeric starts
    if (/^[0-9]/.test(cleaned)) {
        cleaned = "X_" + cleaned;
    }
    
    // 7. Ensure QST_ prefix is present, without duplicating
    let result = cleaned.startsWith("QST_") ? cleaned : "QST_" + cleaned;
    
    // 8. Limit to a maximum length of 60 characters and strip any trailing underscores
    return result.substring(0, 60).replace(/_+$/g, "");
}

function addField() {
    const id = parseInt(editingNodeId || selectedNodeId);
    if (!id) {
        console.error("No node ID for addField", {editingNodeId, selectedNodeId});
        return;
    }
    const node = getNodeById(id);
    if (!node) {
        console.error("Node not found for ID", id);
        return;
    }
    if (!node.data.config) node.data.config = {};
    if (!node.data.config.fields) node.data.config.fields = [];
    
    const idx = node.data.config.fields.length;
    const label = 'New Question ' + (idx + 1);
    node.data.config.fields.push({
        id: 'f_' + Date.now(), 
        key: generateKey(label), 
        label: label,
        type: 'TEXT', 
        required: false, 
        config: {}, 
        rules: { visibleIf: [] }
    });
    currentEditingFieldIndex = idx;
    renderEnterpriseQuestions();
}

function removeField(index) {
    if (!confirm('Remove this question?')) return;
    const id = parseInt(editingNodeId || selectedNodeId);
    const node = getNodeById(id);
    if (node && node.data.config && node.data.config.fields) {
        node.data.config.fields.splice(index, 1);
        currentEditingFieldIndex = null;
        renderEnterpriseQuestions();
    }
}

function openFormBuilder() {
    editingNodeId = parseInt(selectedNodeId);
    if (!editingNodeId) {
        console.error("Attempted to open Form Builder without a selected node");
        return;
    }
    const node = getNodeById(editingNodeId);
    if (!node) {
        console.error("Node not found for Form Builder", editingNodeId);
        return;
    }
    document.getElementById('builder-modal-title').innerText = node.data.label || node.name;
    document.getElementById('form-builder-modal').style.display = 'flex';
    renderEnterpriseQuestions();
}

function closeFormBuilder() {
    document.getElementById('form-builder-modal').style.display = 'none';
    if (editingNodeId) {
        showProperties(editingNodeId);
        editingNodeId = null;
    }
}

function renderEnterpriseQuestions() {
    const id = parseInt(editingNodeId || selectedNodeId);
    if (!id) return;
    const node = getNodeById(id);
    if (!node) return;
    if (!node.data.config) node.data.config = { fields: [] };
    const fields = node.data.config.fields || [];
    const list = document.getElementById('builder-questions-list');
    if (!list) return;
    list.innerHTML = '';

    fields.forEach((field, index) => {
        // Initialize missing key and id for existing fields
        if (!field.id) field.id = 'f_' + (Date.now() + index);
        if (!field.key) field.key = generateKey(field.label || 'question_' + (index + 1));

        const isExpanded = currentEditingFieldIndex === index;
        const typeConfigHtml = renderTypeSpecificConfig(field, index);
        const div = document.createElement('div');
        div.className = 'question-card overflow-hidden';
        div.innerHTML = `
            <div class="question-item" onclick="toggleQuestionExpand(${index})" style="cursor: pointer; ${isExpanded ? 'border-left: 4px solid #3b82f6;' : ''}">
                <div class="question-number">${index + 1}</div>
                <div class="question-content">
                    <div class="question-label" id="label-sum-${index}">${field.label || 'Unnamed'} ${field.required ? '<span class="text-danger">*</span>' : ''}</div>
                    <div class="question-meta">
                        <span class="meta-tag" id="key-sum-${index}">ID: ${field.key}</span>
                        <span class="meta-tag">${field.type}</span>
                    </div>
                </div>
                <div style="margin-left: auto;" id="icon-sum-${index}"><i data-lucide="${getIconForType(field.type)}"></i></div>
            </div>
            ${isExpanded ? `
            <div class="question-editor" style="padding: 24px 30px; background: #fff;">
                <!-- Navigation Tabs -->
                <ul class="nav custom-designer-tabs mb-4" id="question-tabs-${index}">
                    <li class="nav-item">
                        <button type="button" class="nav-link ${currentActiveQuestionTab === 'general' ? 'active' : ''}" onclick="switchQuestionTab('general', event)">General</button>
                    </li>
                    <li class="nav-item">
                        <button type="button" class="nav-link ${currentActiveQuestionTab === 'behavior' ? 'active' : ''}" onclick="switchQuestionTab('behavior', event)">Behavior</button>
                    </li>
                    <li class="nav-item">
                        <button type="button" class="nav-link ${currentActiveQuestionTab === 'rules' ? 'active' : ''}" onclick="switchQuestionTab('rules', event)">Rules</button>
                    </li>
                    <li class="nav-item">
                        <button type="button" class="nav-link ${currentActiveQuestionTab === 'security' ? 'active' : ''}" onclick="switchQuestionTab('security', event)">Security</button>
                    </li>
                    <li class="nav-item">
                        <button type="button" class="nav-link ${currentActiveQuestionTab === 'advanced' ? 'active' : ''}" onclick="switchQuestionTab('advanced', event)">Advanced</button>
                    </li>
                    ${typeConfigHtml ? `
                    <li class="nav-item">
                        <button type="button" class="nav-link ${currentActiveQuestionTab === 'config' ? 'active' : ''}" onclick="switchQuestionTab('config', event)">Type Config</button>
                    </li>` : ''}
                </ul>

                <!-- Tab Contents -->
                <div class="tab-content" onclick="event.stopPropagation()">
                    <!-- 1. GENERAL TAB -->
                    <div class="tab-pane ${currentActiveQuestionTab === 'general' ? 'd-block' : 'd-none'}">
                        <div class="horizontal-field">
                            <label class="fw-semibold text-secondary">Question Label</label>
                            <input type="text" class="form-control" value="${field.label || ''}" oninput="syncFieldLabel(this.value, ${index})">
                        </div>
                        <div class="horizontal-field">
                            <label class="fw-semibold text-secondary">Input Type</label>
                            <select class="form-select" onchange="syncFieldType(this.value, ${index})">
                                ${SUPPORTED_FIELD_TYPES.map(t => `<option value="${t}" ${field.type === t ? 'selected' : ''}>${t}</option>`).join('')}
                            </select>
                        </div>
                        <div class="horizontal-field" style="align-items: flex-start;">
                            <label class="fw-semibold text-secondary" style="margin-top: 8px;">Description / Help</label>
                            <textarea class="form-control" rows="2" oninput="syncFieldDescription(this.value, ${index})">${field.description || ''}</textarea>
                        </div>
                        <div class="horizontal-field">
                            <label class="fw-semibold text-secondary">Default Value</label>
                            <input type="text" class="form-control" placeholder="Optional default value" value="${field.config?.defaultValue || ''}" oninput="syncConfig('defaultValue', this.value, ${index})">
                        </div>
                        <div class="horizontal-field">
                            <label class="fw-semibold text-secondary">Required Field</label>
                            <div style="flex: 1; display: flex; align-items: center;">
                                <div class="form-switch-standalone">
                                    <input class="form-check-input" type="checkbox" role="switch" ${field.required ? 'checked' : ''} onchange="syncFieldProperty('required', this.checked, ${index})">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 2. BEHAVIOR TAB -->
                    <div class="tab-pane ${currentActiveQuestionTab === 'behavior' ? 'd-block' : 'd-none'}">
                        <div class="row">
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-readonly-${index}">Readonly</label>
                                    <input class="form-check-input ms-0" id="switch-readonly-${index}" type="checkbox" role="switch" ${field.readonly ? 'checked' : ''} onchange="syncFieldProperty('readonly', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-hidden-${index}">Hidden</label>
                                    <input class="form-check-input ms-0" id="switch-hidden-${index}" type="checkbox" role="switch" ${field.hidden ? 'checked' : ''} onchange="syncFieldProperty('hidden', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-disabled-${index}">Disabled</label>
                                    <input class="form-check-input ms-0" id="switch-disabled-${index}" type="checkbox" role="switch" ${field.disabled ? 'checked' : ''} onchange="syncFieldProperty('disabled', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-searchable-${index}">Searchable</label>
                                    <input class="form-check-input ms-0" id="switch-searchable-${index}" type="checkbox" role="switch" ${field.searchable ? 'checked' : ''} onchange="syncFieldProperty('searchable', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-sensitive-behavior-${index}">Sensitive</label>
                                    <input class="form-check-input ms-0" id="switch-sensitive-behavior-${index}" type="checkbox" role="switch" ${field.sensitive ? 'checked' : ''} onchange="syncFieldProperty('sensitive', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-auditable-${index}">Auditable</label>
                                    <input class="form-check-input ms-0" id="switch-auditable-${index}" type="checkbox" role="switch" ${field.auditable ? 'checked' : ''} onchange="syncFieldProperty('auditable', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-multiple-${index}">Multiple (Array Value)</label>
                                    <input class="form-check-input ms-0" id="switch-multiple-${index}" type="checkbox" role="switch" ${field.config?.multiple ? 'checked' : ''} onchange="syncConfig('multiple', this.checked, ${index})">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 3. RULES TAB -->
                    <div class="tab-pane ${currentActiveQuestionTab === 'rules' ? 'd-block' : 'd-none'}">
                        <div class="rules-container">
                            ${(() => {
                                const getFriendlyRuleTitle = (ruleName) => {
                                    switch (ruleName) {
                                        case 'visibleIf': return 'Visibility Rules (visibleIf)';
                                        case 'requiredIf': return 'Mandatory Rules (requiredIf)';
                                        case 'readonlyIf': return 'Read-Only Rules (readonlyIf)';
                                        case 'enabledIf': return 'Enablement Rules (enabledIf)';
                                        case 'validateIf': return 'Validation Rules (validateIf)';
                                        default: return ruleName;
                                    }
                                };
                                return ['visibleIf', 'requiredIf', 'readonlyIf', 'enabledIf', 'validateIf'].map(ruleName => {
                                    const conditions = field.rules?.[ruleName] || [];
                                    return `
                                    <div class="card mb-3 border-secondary-subtle">
                                        <div class="card-header bg-light d-flex justify-content-between align-items-center py-2" style="cursor: pointer;" onclick="toggleRuleCard('${ruleName}-${index}', event)">
                                            <span class="fw-semibold text-secondary small">${getFriendlyRuleTitle(ruleName)} (${conditions.length})</span>
                                            <span class="badge bg-primary text-white" style="cursor: pointer;">Toggle View</span>
                                        </div>
                                        <div class="card-body p-3 d-none" id="rule-card-body-${ruleName}-${index}">
                                            <!-- Conditions list -->
                                            <div id="conditions-list-${ruleName}-${index}">
                                                ${conditions.length === 0 ? '<p class="text-muted small mb-0">No conditions set. This rule is always inactive.</p>' : ''}
                                                ${conditions.map((cond, cIdx) => `
                                                    <div class="rule-condition-item">
                                                        <div class="rule-condition-fields" style="display: flex; flex-direction: column; gap: 8px; flex: 1;">
                                                            <div class="rule-condition-field-wrapper">
                                                                <label class="form-label text-muted small fw-semibold mb-1">When Field</label>
                                                                <select class="form-select form-select-sm" onchange="syncRuleCondition('${ruleName}', ${cIdx}, 'field', this.value, ${index})">
                                                                    <option value="">-- Choose Field --</option>
                                                                    ${fields.filter((_, fI) => fI !== index).map(f => `<option value="${f.key}" ${cond.field === f.key ? 'selected' : ''}>${f.label} (${f.key})</option>`).join('')}
                                                                </select>
                                                            </div>
                                                            <div style="display: flex; gap: 12px; width: 100%;">
                                                                <div style="flex: 1;">
                                                                    <label class="form-label text-muted small fw-semibold mb-1">Comparison</label>
                                                                    <select class="form-select form-select-sm" onchange="syncRuleCondition('${ruleName}', ${cIdx}, 'operator', this.value, ${index})">
                                                                        <option value="EQUALS" ${cond.operator === 'EQUALS' ? 'selected' : ''}>EQUALS</option>
                                                                        <option value="NOT_EQUALS" ${cond.operator === 'NOT_EQUALS' ? 'selected' : ''}>NOT_EQUALS</option>
                                                                        <option value="CONTAINS" ${cond.operator === 'CONTAINS' ? 'selected' : ''}>CONTAINS</option>
                                                                        <option value="GREATER_THAN" ${cond.operator === 'GREATER_THAN' ? 'selected' : ''}>GREATER_THAN</option>
                                                                        <option value="LESS_THAN" ${cond.operator === 'LESS_THAN' ? 'selected' : ''}>LESS_THAN</option>
                                                                    </select>
                                                                </div>
                                                                <div style="flex: 1.5;">
                                                                    <label class="form-label text-muted small fw-semibold mb-1">With Value</label>
                                                                    <input type="text" class="form-control form-control-sm" placeholder="Value" value="${cond.value || ''}" oninput="syncRuleCondition('${ruleName}', ${cIdx}, 'value', this.value, ${index})">
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <button type="button" class="rule-condition-remove-btn animate-icon" onclick="removeRuleCondition('${ruleName}', ${cIdx}, ${index}, event)" title="Remove Condition">
                                                            <i data-lucide="trash-2" style="width: 18px; height: 18px;"></i>
                                                        </button>
                                                    </div>
                                                `).join('')}
                                            </div>
                                            <div class="mt-2 text-end">
                                                <button type="button" class="btn btn-sm btn-outline-primary" onclick="addRuleCondition('${ruleName}', ${index}, event)">+ Add New Condition</button>
                                            </div>
                                        </div>
                                    </div>
                                    `;
                                }).join('');
                            })()}

                            <div class="card mb-3 border-secondary-subtle">
                                <div class="card-header bg-light py-2">
                                    <span class="fw-semibold text-secondary small">Formula Calculation (calculateIf)</span>
                                </div>
                                <div class="card-body p-3">
                                    <label class="form-label text-muted small">Enter expression formula using question semantic keys, e.g., <code>price * quantity</code></label>
                                    <input type="text" class="form-control" placeholder="price * quantity" value="${field.rules?.calculateIf || ''}" oninput="syncCalculateIf(this.value, ${index})">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 4. SECURITY TAB -->
                    <div class="tab-pane ${currentActiveQuestionTab === 'security' ? 'd-block' : 'd-none'}">
                        <div class="row">
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-sensitive-security-${index}">Sensitive Data</label>
                                    <input class="form-check-input ms-0" id="switch-sensitive-security-${index}" type="checkbox" role="switch" ${field.sensitive ? 'checked' : ''} onchange="syncFieldProperty('sensitive', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-pii-${index}">Contains PII</label>
                                    <input class="form-check-input ms-0" id="switch-pii-${index}" type="checkbox" role="switch" ${field.config?.pii ? 'checked' : ''} onchange="syncConfig('pii', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-encrypt-${index}">Encrypt Value in DB</label>
                                    <input class="form-check-input ms-0" id="switch-encrypt-${index}" type="checkbox" role="switch" ${field.config?.encryptValue ? 'checked' : ''} onchange="syncConfig('encryptValue', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-mask-${index}">Mask Display Value</label>
                                    <input class="form-check-input ms-0" id="switch-mask-${index}" type="checkbox" role="switch" ${field.config?.maskDisplay ? 'checked' : ''} onchange="syncConfig('maskDisplay', this.checked, ${index})">
                                </div>
                            </div>
                            <div class="col-md-12 mb-2">
                                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center">
                                    <label class="form-check-label mb-0 fw-semibold text-secondary" for="switch-audit-${index}">Audit Changes</label>
                                    <input class="form-check-input ms-0" id="switch-audit-${index}" type="checkbox" role="switch" ${field.config?.auditChanges ? 'checked' : ''} onchange="syncConfig('auditChanges', this.checked, ${index})">
                                </div>
                            </div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold text-secondary">Visible Roles (comma separated)</label>
                            <input type="text" class="form-control" placeholder="e.g. ROLE_ADMIN, ROLE_MANAGER" value="${field.config?.visibleRoles || ''}" oninput="syncConfig('visibleRoles', this.value, ${index})">
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold text-secondary">Editable Roles (comma separated)</label>
                            <input type="text" class="form-control" placeholder="e.g. ROLE_ADMIN" value="${field.config?.editableRoles || ''}" oninput="syncConfig('editableRoles', this.value, ${index})">
                        </div>
                    </div>

                    <!-- 5. ADVANCED TAB -->
                    <div class="tab-pane ${currentActiveQuestionTab === 'advanced' ? 'd-block' : 'd-none'}">
                        <div class="mb-3">
                            <label class="form-label fw-semibold text-secondary">Tooltip Message</label>
                            <input type="text" class="form-control" placeholder="Helpful hint for users" value="${field.helpText || ''}" oninput="syncFieldProperty('helpText', this.value, ${index})">
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold text-secondary">Regex Validation Pattern</label>
                            <input type="text" class="form-control" placeholder="e.g. ^[A-Z]{3}-\\d{3}$" value="${field.config?.regex || ''}" oninput="syncConfig('regex', this.value, ${index})">
                        </div>
                        <div class="mb-3">
                            <label class="form-label fw-semibold text-secondary">API Source URL / Key</label>
                            <input type="text" class="form-control" placeholder="e.g. /api/countries" value="${field.config?.apiSource || ''}" oninput="syncConfig('apiSource', this.value, ${index})">
                        </div>
                    </div>

                    <!-- 6. TYPE CONFIG TAB -->
                    ${typeConfigHtml ? `
                    <div class="tab-pane ${currentActiveQuestionTab === 'config' ? 'd-block' : 'd-none'}">
                        <div class="editor-section">
                            <h5 class="section-subtitle">${field.type} Parameters</h5>
                            <div style="padding: 20px; background: #f8fafc; border-radius: 8px; border: 1px solid #e2e8f0;">
                                ${typeConfigHtml}
                            </div>
                        </div>
                    </div>` : ''}
                </div>

                <!-- Card Footer -->
                <div style="display: flex; justify-content: flex-end; margin-top: 20px; border-top: 1px solid #f1f5f9; padding-top: 16px;">
                    <button type="button" class="btn btn-outline-danger btn-sm" onclick="removeField(${index})" style="display: inline-flex; align-items: center; gap: 6px;">
                        <i data-lucide="trash-2" style="width: 14px; height: 14px;"></i>
                        Remove Question
                    </button>
                </div>
            </div>` : ''}`;
        list.appendChild(div);
    });
    lucide.createIcons();
}

function renderTypeSpecificConfig(field, index) {
    const config = field.config || {};
    
    if (field.type === 'TEXT') {
        return `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Min Length</label>
                <input type="number" class="form-control" value="${config.minLength || ''}" oninput="syncConfig('minLength', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Max Length</label>
                <input type="number" class="form-control" value="${config.maxLength || ''}" oninput="syncConfig('maxLength', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Input Mask</label>
                <input type="text" class="form-control" placeholder="e.g. (999) 999-9999" value="${config.mask || ''}" oninput="syncConfig('mask', this.value, ${index})">
            </div>
            <div class="col-md-3 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Uppercase</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.uppercase ? 'checked' : ''} onchange="syncConfig('uppercase', this.checked, ${index})">
                </div>
            </div>
            <div class="col-md-3 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Trim</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.trim ? 'checked' : ''} onchange="syncConfig('trim', this.checked, ${index})">
                </div>
            </div>
        </div>`;
    }
    if (field.type === 'NUMBER') {
        return `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Min Value</label>
                <input type="number" class="form-control" value="${config.min || ''}" oninput="syncConfig('min', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Max Value</label>
                <input type="number" class="form-control" value="${config.max || ''}" oninput="syncConfig('max', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Step</label>
                <input type="number" class="form-control" value="${config.step || ''}" oninput="syncConfig('step', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Allow Negative</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.allowNegative ? 'checked' : ''} onchange="syncConfig('allowNegative', this.checked, ${index})">
                </div>
            </div>
        </div>`;
    }
    if (field.type === 'DECIMAL') {
        return `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Min Value</label>
                <input type="number" class="form-control" value="${config.min || ''}" oninput="syncConfig('min', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Max Value</label>
                <input type="number" class="form-control" value="${config.max || ''}" oninput="syncConfig('max', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Precision</label>
                <input type="number" class="form-control" placeholder="Total Digits (e.g. 10)" value="${config.precision || ''}" oninput="syncConfig('precision', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Scale</label>
                <input type="number" class="form-control" placeholder="Decimals (e.g. 2)" value="${config.scale || ''}" oninput="syncConfig('scale', this.value, ${index})">
            </div>
        </div>`;
    }
    if (field.type === 'DATE') {
        return `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Min Date</label>
                <input type="date" class="form-control" value="${config.minDate || ''}" oninput="syncConfig('minDate', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Max Date</label>
                <input type="date" class="form-control" value="${config.maxDate || ''}" oninput="syncConfig('maxDate', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Disable Past Dates</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.disablePast ? 'checked' : ''} onchange="syncConfig('disablePast', this.checked, ${index})">
                </div>
            </div>
            <div class="col-md-6 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Disable Future Dates</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.disableFuture ? 'checked' : ''} onchange="syncConfig('disableFuture', this.checked, ${index})">
                </div>
            </div>
        </div>`;
    }
    if (['SELECT', 'RADIO'].includes(field.type)) {
        return `
        <div class="row">
            <div class="col-md-8 mb-3">
                <label class="form-label fw-semibold">Options (One per line: Label|Value)</label>
                <textarea class="form-control" rows="4" oninput="syncOptions(this.value, ${index})" placeholder="e.g. Option A|value_a">${(config.options || []).map(o => `${o.label}|${o.value}`).join('\n')}</textarea>
            </div>
            <div class="col-md-4 mb-3 d-flex align-items-end">
                <div class="form-check form-switch p-3 bg-light rounded border w-100 d-flex justify-content-between align-items-center">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Allow "Other"</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.allowOther ? 'checked' : ''} onchange="syncConfig('allowOther', this.checked, ${index})">
                </div>
            </div>
        </div>`;
    }
    if (field.type === 'MULTISELECT') {
        return `
        <div class="row">
            <div class="col-md-8 mb-3">
                <label class="form-label fw-semibold">Options (One per line: Label|Value)</label>
                <textarea class="form-control" rows="4" oninput="syncOptions(this.value, ${index})" placeholder="e.g. Option A|value_a">${(config.options || []).map(o => `${o.label}|${o.value}`).join('\n')}</textarea>
            </div>
            <div class="col-md-4 mb-3">
                <div class="mb-2">
                    <label class="form-label fw-semibold">Min Selections</label>
                    <input type="number" class="form-control" value="${config.minSelections || ''}" oninput="syncConfig('minSelections', this.value, ${index})">
                </div>
                <div class="mb-2">
                    <label class="form-label fw-semibold">Max Selections</label>
                    <input type="number" class="form-control" value="${config.maxSelections || ''}" oninput="syncConfig('maxSelections', this.value, ${index})">
                </div>
                <div class="form-check form-switch p-2 bg-light rounded border d-flex justify-content-between align-items-center">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Searchable</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.searchable ? 'checked' : ''} onchange="syncConfig('searchable', this.checked, ${index})">
                </div>
            </div>
        </div>`;
    }
    if (field.type === 'FILE') {
        return `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Allowed Extensions (comma separated)</label>
                <input type="text" class="form-control" placeholder="e.g. .pdf,.docx,.png" value="${config.allowedExtensions || ''}" oninput="syncConfig('allowedExtensions', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Max File Size (MB)</label>
                <input type="number" class="form-control" value="${config.maxFileSizeMb || ''}" oninput="syncConfig('maxFileSizeMb', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Max Files Allowed</label>
                <input type="number" class="form-control" value="${config.maxFiles || ''}" oninput="syncConfig('maxFiles', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Enable Image Preview</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.previewEnabled ? 'checked' : ''} onchange="syncConfig('previewEnabled', this.checked, ${index})">
                </div>
            </div>
        </div>`;
    }
    if (field.type === 'USER_PICKER') {
        return `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Allowed Roles (comma separated)</label>
                <input type="text" class="form-control" placeholder="e.g. ROLE_ADMIN,ROLE_MANAGER" value="${config.allowedRoles || ''}" oninput="syncConfig('allowedRoles', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Allowed Groups (comma separated)</label>
                <input type="text" class="form-control" placeholder="e.g. HR,IT,Finance" value="${config.allowedGroups || ''}" oninput="syncConfig('allowedGroups', this.value, ${index})">
            </div>
            <div class="col-md-4 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Allow Multiple Users</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.multiple ? 'checked' : ''} onchange="syncConfig('multiple', this.checked, ${index})">
                </div>
            </div>
            <div class="col-md-4 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Show Email</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.showEmail ? 'checked' : ''} onchange="syncConfig('showEmail', this.checked, ${index})">
                </div>
            </div>
            <div class="col-md-4 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Show Avatar</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.showAvatar ? 'checked' : ''} onchange="syncConfig('showAvatar', this.checked, ${index})">
                </div>
            </div>
        </div>`;
    }
    if (field.type === 'TABLE') {
        return `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Columns JSON definition</label>
                <textarea class="form-control" rows="4" placeholder='e.g. [{"key":"name","label":"Name"}]' oninput="syncConfig('columns', this.value, ${index})">${config.columns ? (typeof config.columns === 'string' ? config.columns : JSON.stringify(config.columns)) : ''}</textarea>
            </div>
            <div class="col-md-3 mb-3">
                <div class="mb-2">
                    <label class="form-label fw-semibold">Min Rows</label>
                    <input type="number" class="form-control" value="${config.minRows || ''}" oninput="syncConfig('minRows', this.value, ${index})">
                </div>
                <div class="mb-2">
                    <label class="form-label fw-semibold">Max Rows</label>
                    <input type="number" class="form-control" value="${config.maxRows || ''}" oninput="syncConfig('maxRows', this.value, ${index})">
                </div>
            </div>
            <div class="col-md-3 mb-3 d-flex align-items-end">
                <div class="form-check form-switch p-3 bg-light rounded border w-100 d-flex justify-content-between align-items-center">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Inline Edit</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.inlineEdit ? 'checked' : ''} onchange="syncConfig('inlineEdit', this.checked, ${index})">
                </div>
            </div>
        </div>`;
    }
    if (field.type === 'REPEATER') {
        return `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Template Fields JSON</label>
                <textarea class="form-control" rows="4" placeholder='e.g. [{"key":"field1","label":"Field 1","type":"TEXT"}]' oninput="syncConfig('templateFields', this.value, ${index})">${config.templateFields ? (typeof config.templateFields === 'string' ? config.templateFields : JSON.stringify(config.templateFields)) : ''}</textarea>
            </div>
            <div class="col-md-6 mb-3">
                <div class="mb-2">
                    <label class="form-label fw-semibold">Min Items</label>
                    <input type="number" class="form-control" value="${config.minItems || ''}" oninput="syncConfig('minItems', this.value, ${index})">
                </div>
                <div class="mb-2">
                    <label class="form-label fw-semibold">Max Items</label>
                    <input type="number" class="form-control" value="${config.maxItems || ''}" oninput="syncConfig('maxItems', this.value, ${index})">
                </div>
            </div>
        </div>`;
    }
    if (field.type === 'RICH_TEXT') {
        return `
        <div class="row">
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Toolbar Options (comma separated)</label>
                <input type="text" class="form-control" placeholder="e.g. bold,italic,underline,link" value="${config.toolbarOptions || ''}" oninput="syncConfig('toolbarOptions', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <label class="form-label fw-semibold">Max Characters</label>
                <input type="number" class="form-control" value="${config.maxLength || ''}" oninput="syncConfig('maxLength', this.value, ${index})">
            </div>
            <div class="col-md-6 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Allow HTML Source</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.allowHtml ? 'checked' : ''} onchange="syncConfig('allowHtml', this.checked, ${index})">
                </div>
            </div>
            <div class="col-md-6 mb-3">
                <div class="form-check form-switch p-3 bg-light rounded border d-flex justify-content-between align-items-center h-100">
                    <label class="form-check-label mb-0 fw-semibold text-secondary">Sanitize HTML</label>
                    <input class="form-check-input ms-0" type="checkbox" role="switch" ${config.sanitizeHtml ? 'checked' : ''} onchange="syncConfig('sanitizeHtml', this.checked, ${index})">
                </div>
            </div>
        </div>`;
    }
    
    return null;
}

function getIconForType(type) {
    const map = { 
        'TEXT': 'type', 'TEXTAREA': 'align-left', 'EMAIL': 'mail', 'PHONE': 'phone', 
        'URL': 'link', 'PASSWORD': 'lock', 'COLOR': 'palette', 'NUMBER': 'hash', 
        'DECIMAL': 'percent', 'DATE': 'calendar', 'DATETIME': 'clock', 'BOOLEAN': 'toggle-left', 
        'RADIO': 'circle-dot', 'CHECKBOX': 'check-square', 'SELECT': 'list', 'MULTISELECT': 'sliders', 
        'FILE': 'file-up', 'IMAGE': 'image', 'SIGNATURE': 'pen-tool', 'USER_PICKER': 'user', 
        'ROLE_PICKER': 'shield', 'ORG_PICKER': 'building', 'TABLE': 'table', 'REPEATER': 'copy', 
        'RICH_TEXT': 'file-text', 'HIDDEN': 'eye-off', 'CALCULATED': 'calculator', 'DISPLAY_TEXT': 'file-signature'
    };
    return map[type] || 'help-circle';
}

function toggleQuestionExpand(index) {
    currentEditingFieldIndex = (currentEditingFieldIndex === index) ? null : index;
    renderEnterpriseQuestions();
}

/* --- Sync Functions (Optimized to avoid re-render) --- */
function syncFieldLabel(val, idx) {
    const id = parseInt(editingNodeId || selectedNodeId);
    const field = getNodeById(id).data.config.fields[idx];
    field.label = val;
    field.key = generateKey(val);
    const labelEl = document.getElementById(`label-sum-${idx}`);
    if (labelEl) labelEl.innerHTML = `${val || 'Unnamed'} ${field.required ? '<span class="text-danger">*</span>' : ''}`;
    const keyEl = document.getElementById(`key-sum-${idx}`);
    if (keyEl) keyEl.innerText = `ID: ${field.key}`;
}

function syncFieldType(val, idx) {
    const id = parseInt(editingNodeId || selectedNodeId);
    const field = getNodeById(id).data.config.fields[idx];
    field.type = val;
    renderEnterpriseQuestions(); // Re-render for type-specific config
}

function syncFieldDescription(val, idx) { 
    const id = parseInt(editingNodeId || selectedNodeId);
    getNodeById(id).data.config.fields[idx].description = val; 
}

function syncFieldRequired(val, idx) {
    const id = parseInt(editingNodeId || selectedNodeId);
    const field = getNodeById(id).data.config.fields[idx];
    field.required = val;
    const labelEl = document.getElementById(`label-sum-${idx}`);
    if (labelEl) labelEl.innerHTML = `${field.label || 'Unnamed'} ${val ? '<span class="text-danger">*</span>' : ''}`;
}

function syncConfig(k, v, idx) { 
    const id = parseInt(editingNodeId || selectedNodeId);
    const field = getNodeById(id).data.config.fields[idx];
    if (!field.config) field.config = {};
    field.config[k] = v; 
}

function syncOptions(v, idx) {
    const id = parseInt(editingNodeId || selectedNodeId);
    const field = getNodeById(id).data.config.fields[idx];
    field.config.options = v.split('\n').filter(l => l.includes('|')).map(l => {
        const [label, value] = l.split('|'); return { label: label.trim(), value: value.trim() };
    });
}

function syncFieldProperty(prop, val, idx) {
    const id = parseInt(editingNodeId || selectedNodeId);
    const field = getNodeById(id).data.config.fields[idx];
    field[prop] = val;
    updateFormStats();
}

function updateFormStats() {
    const id = parseInt(selectedNodeId);
    const node = getNodeById(id);
    if (!node || !node.data || !node.data.config) return;
    const fields = node.data.config.fields || [];
    
    const total = fields.length;
    const mandatory = fields.filter(f => f.required).length;
    const sensitive = fields.some(f => f.sensitive || f.config?.sensitive) ? 'Yes' : 'No';
    
    const sideSensitive = document.getElementById('side-stat-sensitive');
    const sideTotal = document.getElementById('side-stat-total');
    const sideMandatory = document.getElementById('side-stat-mandatory');
    
    if (sideSensitive) sideSensitive.innerText = sensitive;
    if (sideTotal) sideTotal.innerText = total;
    if (sideMandatory) sideMandatory.innerText = mandatory;
}

function toggleRuleCard(id, event) {
    if (event) event.stopPropagation();
    const el = document.getElementById('rule-card-body-' + id);
    if (el) {
        el.classList.toggle('d-none');
    }
}

function addRuleCondition(ruleName, fieldIndex, event) {
    if (event) event.stopPropagation();
    const node = getNodeById(editingNodeId);
    if (!node) return;
    const field = node.data.config.fields[fieldIndex];
    if (!field.rules) field.rules = {};
    if (!field.rules[ruleName]) field.rules[ruleName] = [];
    field.rules[ruleName].push({ field: '', operator: 'EQUALS', value: '' });
    editor.updateNodeDataFromId(editingNodeId, node.data);
    renderEnterpriseQuestions();
    
    setTimeout(() => {
        const el = document.getElementById('rule-card-body-' + ruleName + '-' + fieldIndex);
        if (el) el.classList.remove('d-none');
    }, 50);
}

function removeRuleCondition(ruleName, cIdx, fieldIndex, event) {
    if (event) event.stopPropagation();
    const node = getNodeById(editingNodeId);
    if (!node) return;
    const field = node.data.config.fields[fieldIndex];
    if (field.rules && field.rules[ruleName]) {
        field.rules[ruleName].splice(cIdx, 1);
        editor.updateNodeDataFromId(editingNodeId, node.data);
        renderEnterpriseQuestions();
        
        setTimeout(() => {
            const el = document.getElementById('rule-card-body-' + ruleName + '-' + fieldIndex);
            if (el) el.classList.remove('d-none');
        }, 50);
    }
}

function syncRuleCondition(ruleName, cIdx, prop, value, fieldIndex) {
    const node = getNodeById(editingNodeId);
    if (!node) return;
    const field = node.data.config.fields[fieldIndex];
    if (field.rules && field.rules[ruleName] && field.rules[ruleName][cIdx]) {
        field.rules[ruleName][cIdx][prop] = value;
        editor.updateNodeDataFromId(editingNodeId, node.data);
    }
}

function syncCalculateIf(value, fieldIndex) {
    const node = getNodeById(editingNodeId);
    if (!node) return;
    const field = node.data.config.fields[fieldIndex];
    if (!field.rules) field.rules = {};
    field.rules.calculateIf = value;
    editor.updateNodeDataFromId(editingNodeId, node.data);
}

function updateNodeConfig(key, val) {
    const id = parseInt(selectedNodeId);
    const node = getNodeById(id);
    if (node) {
        if (!node.data.config) node.data.config = {};
        node.data.config[key] = val;
    }
}

/* --- API Integration --- */
async function loadWorkflow() {
    try {
        const response = await fetch(`${apiBase}/${workflowId}/model`);
        const data = await response.json();
        if (data.nodes) {
            const drawflowData = { version: '0.0.4', drawflow: { Home: { data: {} } } };
            data.nodes.forEach(node => {
                const nodeId = parseInt(node.id);
                drawflowData.drawflow.Home.data[nodeId] = {
                    id: nodeId, name: node.type, data: { type: node.type, label: node.label, config: node.config || { fields: [] }, slaHours: node.slaHours },
                    class: node.type.toLowerCase(), html: getNodeHtml(node.type, node.label), typenode: false,
                    inputs: node.type==='START'?{}:{input_1:{connections:[]}}, 
                    outputs: node.type==='END'?{}:{output_1:{connections:[]}},
                    pos_x: node.x, pos_y: node.y
                };
            });
            data.edges.forEach(edge => {
                const sId = parseInt(edge.source), tId = parseInt(edge.target);
                const s = drawflowData.drawflow.Home.data[sId], t = drawflowData.drawflow.Home.data[tId];
                if (s && t) {
                    const out = edge.sourceOutput || 'output_1', inp = edge.targetInput || 'input_1';
                    if (!s.outputs[out]) s.outputs[out] = { connections: [] };
                    s.outputs[out].connections.push({ node: tId.toString(), input: inp });
                    if (!t.inputs[inp]) t.inputs[inp] = { connections: [] };
                    t.inputs[inp].connections.push({ node: sId.toString(), input: out });
                }
            });
            editor.import(drawflowData);
            setTimeout(() => lucide.createIcons(), 100);
        }
    } catch (e) { console.error("Error loading workflow", e); }
}

async function saveWorkflow() {
    const exportData = editor.export();
    const nodes = [], edges = [];
    const drawflowNodes = exportData.drawflow.Home.data;
    Object.keys(drawflowNodes).forEach(key => {
        const n = drawflowNodes[key];
        nodes.push({ id: n.id.toString(), type: n.data.type || n.name, label: n.data.label || n.name, x: n.pos_x, y: n.pos_y, config: n.data.config, slaHours: n.data.slaHours });
        Object.keys(n.outputs).forEach(outKey => {
            n.outputs[outKey].connections.forEach(conn => {
                edges.push({ source: n.id.toString(), target: conn.node, sourceOutput: outKey, targetInput: conn.input || conn.output });
            });
        });
    });

    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    try {
        const response = await fetch(`${apiBase}/${workflowId}/model`, {
            method: 'POST', 
            headers: { 
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }, 
            body: JSON.stringify({ nodes, edges })
        });
        showToast(response.ok ? "Saved successfully" : "Error saving", response.ok ? "success" : "error");
    } catch (e) { 
        console.error("Save error", e);
        showToast("Network error", "error"); 
    }
}

function showToast(message, type) {
    const toast = document.createElement('div');
    Object.assign(toast.style, { position: 'fixed', bottom: '20px', left: '50%', transform: 'translateX(-50%)', padding: '12px 24px', borderRadius: '8px', color: 'white', zIndex: '1000', background: type === 'success' ? '#22c55e' : '#ef4444' });
    toast.innerText = message; document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}
