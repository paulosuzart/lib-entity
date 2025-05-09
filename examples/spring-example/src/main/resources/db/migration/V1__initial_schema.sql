-- Create enum type for invoice status
DROP TYPE IF EXISTS invoice_status CASCADE;
CREATE TYPE invoice_status AS ENUM ('DRAFT', 'APPROVED', 'REJECTED', 'PAID');

DROP TABLE IF EXISTS invoice CASCADE;
CREATE TABLE invoice (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(128),
    amount DECIMAL(19,2) NOT NULL,
    vat DECIMAL(19,2) NOT NULL DEFAULT 0,
    due_date DATE,
    submitted_at DATE,
    submitter_id VARCHAR(128),
    submitter_device_id VARCHAR(128),
    approval_date DATE,
    approver_id VARCHAR(128),
    rejection_reason VARCHAR(255),
    rejected_by VARCHAR(128),
    rejection_date DATE,
    receipt_number VARCHAR(255),
    ready_for_approval BOOLEAN DEFAULT FALSE,
    approval_comment VARCHAR(255),
    status invoice_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS reimbursement CASCADE;
CREATE TABLE reimbursement (
    id BIGSERIAL PRIMARY KEY,
    amount DECIMAL(19,2) NOT NULL,
    status invoice_status NOT NULL DEFAULT 'DRAFT',
    due_date DATE NOT NULL,
    vat DECIMAL(19,2) NOT NULL DEFAULT 0,
    employee_id UUID not null,
    receipt_number VARCHAR(255) not null,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
