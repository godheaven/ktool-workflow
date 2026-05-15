-- Users and Roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Workflow Definition
CREATE TABLE wf_definitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE wf_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    definition_id BIGINT NOT NULL,
    version_number INT NOT NULL,
    active BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (definition_id) REFERENCES wf_definitions(id)
);

CREATE TABLE wf_states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    is_initial BOOLEAN DEFAULT FALSE,
    is_final BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (version_id) REFERENCES wf_versions(id)
);

CREATE TABLE wf_transitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id BIGINT NOT NULL,
    from_state_id BIGINT NOT NULL,
    to_state_id BIGINT NOT NULL,
    name VARCHAR(100),
    rule_expression TEXT,
    FOREIGN KEY (version_id) REFERENCES wf_versions(id),
    FOREIGN KEY (from_state_id) REFERENCES wf_states(id),
    FOREIGN KEY (to_state_id) REFERENCES wf_states(id)
);

-- Forms
CREATE TABLE wf_forms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    FOREIGN KEY (version_id) REFERENCES wf_versions(id)
);

CREATE TABLE wf_form_fields (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    form_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    field_type VARCHAR(20) NOT NULL, -- TEXT, NUMBER, DATE, SELECT, etc.
    required BOOLEAN DEFAULT FALSE,
    order_index INT DEFAULT 0,
    options TEXT, -- JSON or comma separated for SELECT
    FOREIGN KEY (form_id) REFERENCES wf_forms(id)
);

-- Instances and Execution
CREATE TABLE wf_instances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version_id BIGINT NOT NULL,
    current_state_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, CANCELLED
    FOREIGN KEY (version_id) REFERENCES wf_versions(id),
    FOREIGN KEY (current_state_id) REFERENCES wf_states(id),
    FOREIGN KEY (creator_id) REFERENCES users(id)
);

CREATE TABLE wf_instance_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    field_name VARCHAR(50) NOT NULL,
    field_value TEXT,
    FOREIGN KEY (instance_id) REFERENCES wf_instances(id)
);

CREATE TABLE wf_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    state_id BIGINT NOT NULL,
    assignee_id BIGINT,
    role_id BIGINT,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, COMPLETED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (instance_id) REFERENCES wf_instances(id),
    FOREIGN KEY (state_id) REFERENCES wf_states(id),
    FOREIGN KEY (assignee_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE wf_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    from_state_id BIGINT,
    to_state_id BIGINT,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (instance_id) REFERENCES wf_instances(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
