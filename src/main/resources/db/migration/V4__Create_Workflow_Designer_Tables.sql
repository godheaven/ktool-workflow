-- V4: Create Workflow Designer Tables

CREATE TABLE designer_workflow_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    current_version INT DEFAULT 1,
    owner_name VARCHAR(255),
    owner_avatar_url VARCHAR(255),
    complexity VARCHAR(50) DEFAULT 'Medium',
    rating INT DEFAULT 3,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE designer_workflow_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    definition_id BIGINT NOT NULL,
    version_number INT,
    is_draft BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    CONSTRAINT fk_version_definition FOREIGN KEY (definition_id) REFERENCES designer_workflow_definitions(id)
);

CREATE TABLE designer_workflow_nodes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visual_id VARCHAR(255),
    version_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    label VARCHAR(255),
    position_x DOUBLE,
    position_y DOUBLE,
    config_json TEXT,
    sla_hours INT,
    timeout_action VARCHAR(255),
    escalation_rule VARCHAR(255),
    visibility_rule VARCHAR(255),
    CONSTRAINT fk_node_version FOREIGN KEY (version_id) REFERENCES designer_workflow_versions(id)
);

CREATE TABLE designer_workflow_edges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id BIGINT NOT NULL,
    source_node_id VARCHAR(255),
    target_node_id VARCHAR(255),
    source_output VARCHAR(255),
    target_input VARCHAR(255),
    condition_expression TEXT,
    label VARCHAR(255),
    CONSTRAINT fk_edge_version FOREIGN KEY (version_id) REFERENCES designer_workflow_versions(id)
);
