-- V3 Phase 7 Enterprise SaaS Features

CREATE TABLE IF NOT EXISTS organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    custom_domain VARCHAR(255) UNIQUE,
    logo_url VARCHAR(1000),
    primary_color VARCHAR(50),
    favicon_url VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

ALTER TABLE users ADD COLUMN IF NOT EXISTS current_organization_id UUID;
ALTER TABLE users ADD CONSTRAINT fk_users_org FOREIGN KEY (current_organization_id) REFERENCES organizations(id);

ALTER TABLE workspaces ADD COLUMN IF NOT EXISTS organization_id UUID;
ALTER TABLE workspaces ADD CONSTRAINT fk_workspaces_org FOREIGN KEY (organization_id) REFERENCES organizations(id);

CREATE TABLE IF NOT EXISTS organization_members (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP,
    CONSTRAINT fk_om_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_om_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS teams (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    department VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_teams_org FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE IF NOT EXISTS team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP,
    CONSTRAINT fk_tm_team FOREIGN KEY (team_id) REFERENCES teams(id),
    CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS plans (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    max_storage BIGINT NOT NULL,
    max_users INTEGER NOT NULL,
    custom_domain_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    white_label_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    api_access_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    monthly_price BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    plan_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    stripe_customer_id VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    cancel_at_period_end BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_sub_org FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_sub_plan FOREIGN KEY (plan_id) REFERENCES plans(id)
);

CREATE TABLE IF NOT EXISTS api_keys (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    key_value VARCHAR(255) UNIQUE NOT NULL,
    scopes VARCHAR(1000) NOT NULL,
    expires_at TIMESTAMP,
    last_used_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    CONSTRAINT fk_api_org FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE IF NOT EXISTS webhooks (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    url VARCHAR(1000) NOT NULL,
    secret VARCHAR(255),
    events VARCHAR(1000) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    CONSTRAINT fk_webhooks_org FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE IF NOT EXISTS webhook_deliveries (
    id UUID PRIMARY KEY,
    webhook_id UUID NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload VARCHAR(2000),
    response_status INTEGER,
    response_body VARCHAR(2000),
    delivered_at TIMESTAMP,
    successful BOOLEAN NOT NULL,
    CONSTRAINT fk_wd_webhook FOREIGN KEY (webhook_id) REFERENCES webhooks(id)
);

CREATE TABLE IF NOT EXISTS document_insights (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL,
    summary VARCHAR(2000),
    keywords VARCHAR(1000),
    topics VARCHAR(1000),
    entities VARCHAR(1000),
    language VARCHAR(50),
    estimated_reading_time INTEGER,
    document_type VARCHAR(255),
    sentiment VARCHAR(255),
    complexity VARCHAR(255),
    generated_at TIMESTAMP,
    CONSTRAINT fk_di_file FOREIGN KEY (file_id) REFERENCES files(id)
);

CREATE TABLE IF NOT EXISTS image_tags (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL,
    tag VARCHAR(255) NOT NULL,
    confidence_score FLOAT,
    category VARCHAR(255),
    created_at TIMESTAMP,
    CONSTRAINT fk_it_file FOREIGN KEY (file_id) REFERENCES files(id)
);

