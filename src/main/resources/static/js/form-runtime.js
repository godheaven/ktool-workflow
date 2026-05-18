/**
 * Form Runtime Engine
 * Handles metadata-driven dynamic logic: visibility, validation, and dependencies.
 */
const formRuntime = (function() {
    const registry = new Map();
    const formState = {};

    function init(formId) {
        console.log("Initializing Form Runtime for:", formId);
        const containers = document.querySelectorAll('.form-field-container');
        
        containers.forEach(container => {
            const key = container.dataset.fieldKey;
            const rulesJson = container.dataset.rules;
            const rules = JSON.parse(rulesJson || '{}');
            
            registry.set(key, { container, rules });

            // Attach change listeners to track state
            const input = container.querySelector('input, select, textarea');
            if (input) {
                input.addEventListener('change', (e) => {
                    updateState(key, e.target.value);
                    evaluateAllRules();
                    scheduleAutosave();
                });
                // Initial state
                updateState(key, input.value);
            }
        });

        // Initialize signature pads
        const signatureCanvases = document.querySelectorAll('canvas[id^="signature-canvas-"]');
        signatureCanvases.forEach(canvas => {
            initSignatureCanvas(canvas);
        });

        evaluateAllRules();
    }

    function updateState(key, value) {
        formState[key] = value;
    }

    function evaluateAllRules() {
        registry.forEach((config, key) => {
            const input = config.container.querySelector('input, select, textarea');
            
            // 1. visibleIf
            if (config.rules.visibleIf) {
                const isVisible = evaluateConditions(config.rules.visibleIf);
                config.container.style.display = isVisible ? 'block' : 'none';
            }
            
            // 2. requiredIf
            if (input && config.rules.requiredIf) {
                input.required = evaluateConditions(config.rules.requiredIf);
            }
            
            // 3. readonlyIf
            if (input && config.rules.readonlyIf) {
                input.readOnly = evaluateConditions(config.rules.readonlyIf);
            }
            
            // 4. enabledIf
            if (input && config.rules.enabledIf) {
                const isEnabled = evaluateConditions(config.rules.enabledIf);
                input.disabled = !isEnabled;
            }
            
            // 5. calculateIf (Dynamic Formula Expression)
            if (config.rules.calculateIf) {
                try {
                    let expression = config.rules.calculateIf;
                    let hasAllVars = true;
                    
                    // Find all semantic keys matching word boundaries
                    const varKeys = expression.match(/[a-zA-Z_][a-zA-Z0-9_]*/g) || [];
                    varKeys.forEach(varKey => {
                        const val = Number(formState[varKey]);
                        if (isNaN(val)) {
                            hasAllVars = false;
                        } else {
                            const regex = new RegExp('\\b' + varKey + '\\b', 'g');
                            expression = expression.replace(regex, val);
                        }
                    });
                    
                    if (hasAllVars && /^[0-9+\-*/().\s]+$/.test(expression)) {
                        const calculatedVal = Function('"use strict"; return (' + expression + ')')();
                        if (input && input.value != calculatedVal) {
                            input.value = calculatedVal;
                            updateState(key, calculatedVal);
                        }
                    }
                } catch (err) {
                    console.warn("Calculation error for field " + key, err);
                }
            }
        });
    }

    function evaluateConditions(conditions) {
        if (!conditions || conditions.length === 0) return true;
        
        // Default is AND logic
        return conditions.every(cond => {
            const actualValue = formState[cond.field];
            
            switch (cond.operator) {
                case 'EQUALS':
                    return actualValue == cond.value;
                case 'NOT_EQUALS':
                    return actualValue != cond.value;
                case 'CONTAINS':
                    return actualValue && actualValue.includes(cond.value);
                case 'GREATER_THAN':
                    return Number(actualValue) > Number(cond.value);
                case 'LESS_THAN':
                    return Number(actualValue) < Number(cond.value);
                case 'EMPTY':
                    return !actualValue || actualValue === '';
                case 'NOT_EMPTY':
                    return actualValue && actualValue !== '';
                default:
                    return true;
            }
        });
    }

    function addRow(tableKey) {
        console.log("Adding row to table:", tableKey);
        const table = document.getElementById('table-' + tableKey);
        if (!table) return;

        const tbody = table.querySelector('tbody');
        const rowCount = tbody.rows.length;
        const newRow = tbody.insertRow();
        
        const config = registry.get(tableKey);
        const columns = JSON.parse(config.container.dataset.config || '{}').columns || [];

        columns.forEach((col, index) => {
            const cell = newRow.insertCell(index);
            cell.innerHTML = `<input type="text" name="${tableKey}[${rowCount}].${col.key}" class="form-control form-control-sm">`;
        });

        const actionCell = newRow.insertCell(columns.length);
        actionCell.innerHTML = `<button type="button" class="btn btn-sm btn-outline-danger" onclick="this.closest('tr').remove()">
                                    <i class="bi bi-trash"></i>
                                 </button>`;
    }

    function openPicker(key, type) {
        console.log(`Opening ${type} picker for: ${key}`);
        // This would open a modal with user/org search logic
        alert(`Enterprise ${type} Picker for ${key} would open here.`);
    }

    let autosaveTimer;
    function scheduleAutosave() {
        clearTimeout(autosaveTimer);
        autosaveTimer = setTimeout(autosave, 5000); // 5 seconds after last change
    }

    function autosave() {
        console.log("Autosaving form state...", formState);
        // In a real app: fetch('/api/forms/autosave', { method: 'POST', body: JSON.stringify(formState) });
        const badge = document.getElementById('autosave-status');
        if (badge) {
            badge.textContent = 'Autosaved: ' + new Date().toLocaleTimeString();
            badge.classList.remove('d-none');
            setTimeout(() => badge.classList.add('d-none'), 2000);
        }
    }

    function initSignatureCanvas(canvas) {
        const ctx = canvas.getContext('2d');
        let drawing = false;
        const hiddenInputId = canvas.id.replace('signature-canvas-', '');
        const hiddenInput = document.getElementById(hiddenInputId);

        // Adjust canvas resolution for drawing
        canvas.width = canvas.offsetWidth || 300;
        canvas.height = canvas.offsetHeight || 150;

        ctx.strokeStyle = '#000000';
        ctx.lineWidth = 2.5;
        ctx.lineJoin = 'round';
        ctx.lineCap = 'round';

        function getMousePos(e) {
            const rect = canvas.getBoundingClientRect();
            const clientX = e.touches ? e.touches[0].clientX : e.clientX;
            const clientY = e.touches ? e.touches[0].clientY : e.clientY;
            return {
                x: clientX - rect.left,
                y: clientY - rect.top
            };
        }

        function startDrawing(e) {
            drawing = true;
            const pos = getMousePos(e);
            ctx.beginPath();
            ctx.moveTo(pos.x, pos.y);
            e.preventDefault();
        }

        function draw(e) {
            if (!drawing) return;
            const pos = getMousePos(e);
            ctx.lineTo(pos.x, pos.y);
            ctx.stroke();
            e.preventDefault();
        }

        function stopDrawing() {
            if (!drawing) return;
            drawing = false;
            if (hiddenInput) {
                hiddenInput.value = canvas.toDataURL();
                updateState(hiddenInputId, hiddenInput.value);
                scheduleAutosave();
            }
        }

        canvas.addEventListener('mousedown', startDrawing);
        canvas.addEventListener('mousemove', draw);
        window.addEventListener('mouseup', stopDrawing);

        canvas.addEventListener('touchstart', startDrawing);
        canvas.addEventListener('touchmove', draw);
        window.addEventListener('touchend', stopDrawing);
    }

    function clearSignature(key) {
        const canvas = document.getElementById('signature-canvas-' + key);
        if (canvas) {
            const ctx = canvas.getContext('2d');
            ctx.clearRect(0, 0, canvas.width, canvas.height);
        }
        const hiddenInput = document.getElementById(key);
        if (hiddenInput) {
            hiddenInput.value = '';
            updateState(key, '');
            scheduleAutosave();
        }
    }

    function previewImage(input, key) {
        const container = document.getElementById('image-preview-container-' + key);
        const img = document.getElementById('image-preview-' + key);
        if (input.files && input.files[0]) {
            const reader = new FileReader();
            reader.onload = function(e) {
                if (img) img.src = e.target.result;
                if (container) container.classList.remove('d-none');
                updateState(key, input.value);
                scheduleAutosave();
            };
            reader.readAsDataURL(input.files[0]);
        } else {
            if (container) container.classList.add('d-none');
            if (img) img.src = '';
            updateState(key, '');
            scheduleAutosave();
        }
    }

    return {
        init,
        addRow,
        openPicker,
        clearSignature,
        previewImage,
        autosave,
        scheduleAutosave,
        evaluateAllRules
    };
})();

// Auto-init on load if a dynamic form is present
document.addEventListener('DOMContentLoaded', () => {
    if (document.querySelector('.form-field-container')) {
        formRuntime.init('dynamic-workflow-form');
    }
});
