-- V2 Phase 5 Enterprise Features

ALTER TABLE files ADD COLUMN IF NOT EXISTS workspace_id UUID;
ALTER TABLE files ADD CONSTRAINT fk_files_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE folders ADD COLUMN IF NOT EXISTS workspace_id UUID;
ALTER TABLE folders ADD CONSTRAINT fk_folders_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

CREATE TABLE IF NOT EXISTS file_versions (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    size BIGINT NOT NULL,
    type VARCHAR(255) NOT NULL,
    path VARCHAR(1000) NOT NULL,
    checksum VARCHAR(255),
    change_description VARCHAR(1000),
    uploaded_by_id UUID NOT NULL,
    upload_date TIMESTAMP,
    CONSTRAINT fk_fv_file FOREIGN KEY (file_id) REFERENCES files(id),
    CONSTRAINT fk_fv_user FOREIGN KEY (uploaded_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS trash_items (
    id UUID PRIMARY KEY,
    file_id UUID,
    folder_id UUID,
    original_folder_id UUID,
    workspace_id UUID,
    deleted_by_id UUID NOT NULL,
    deleted_at TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_ti_file FOREIGN KEY (file_id) REFERENCES files(id),
    CONSTRAINT fk_ti_folder FOREIGN KEY (folder_id) REFERENCES folders(id),
    CONSTRAINT fk_ti_orig_folder FOREIGN KEY (original_folder_id) REFERENCES folders(id),
    CONSTRAINT fk_ti_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id),
    CONSTRAINT fk_ti_user FOREIGN KEY (deleted_by_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    user_id UUID,
    ip_address VARCHAR(255),
    device VARCHAR(255),
    browser VARCHAR(255),
    action VARCHAR(255) NOT NULL,
    target_resource VARCHAR(255),
    target_resource_id VARCHAR(255),
    workspace_id UUID,
    status VARCHAR(255),
    CONSTRAINT fk_al_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_al_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
);

CREATE TABLE IF NOT EXISTS security_logs (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    user_id UUID,
    username_attempt VARCHAR(255),
    event_type VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255),
    device VARCHAR(255),
    details VARCHAR(1000),
    CONSTRAINT fk_sl_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS tags (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(255),
    owner_id UUID NOT NULL,
    CONSTRAINT fk_tags_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS file_tags (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL,
    tag_id UUID NOT NULL,
    created_at TIMESTAMP,
    CONSTRAINT fk_ft_file FOREIGN KEY (file_id) REFERENCES files(id),
    CONSTRAINT fk_ft_tag FOREIGN KEY (tag_id) REFERENCES tags(id)
);

CREATE TABLE IF NOT EXISTS favorites (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    file_id UUID,
    folder_id UUID,
    workspace_id UUID,
    created_at TIMESTAMP,
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_fav_file FOREIGN KEY (file_id) REFERENCES files(id),
    CONSTRAINT fk_fav_folder FOREIGN KEY (folder_id) REFERENCES folders(id),
    CONSTRAINT fk_fav_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id)
);

CREATE TABLE IF NOT EXISTS admin_settings (
    setting_key VARCHAR(255) PRIMARY KEY,
    setting_value VARCHAR(1000) NOT NULL,
    description VARCHAR(1000)
);

INSERT INTO admin_settings (setting_key, setting_value, description) VALUES ('trash_retention_days', '30', 'Number of days to keep items in trash') ON CONFLICT DO NOTHING;

