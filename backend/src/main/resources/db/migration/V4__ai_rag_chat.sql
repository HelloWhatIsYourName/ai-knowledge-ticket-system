CREATE SEQUENCE ai_session_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE ai_message_seq START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE ai_message_citation_seq START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE ai_session (
    id NUMBER(19) DEFAULT ai_session_seq.NEXTVAL PRIMARY KEY,
    user_id NUMBER(19) NOT NULL,
    title VARCHAR2(200) NOT NULL,
    last_question VARCHAR2(1000),
    transfer_suggested NUMBER(1) DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_ai_session_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT ck_ai_session_transfer CHECK (transfer_suggested IN (0, 1))
);

CREATE TABLE ai_message (
    id NUMBER(19) DEFAULT ai_message_seq.NEXTVAL PRIMARY KEY,
    session_id NUMBER(19) NOT NULL,
    user_id NUMBER(19) NOT NULL,
    role VARCHAR2(16) NOT NULL,
    content CLOB NOT NULL,
    model_name VARCHAR2(100),
    can_answer NUMBER(1),
    confidence NUMBER(8, 6),
    transfer_suggested NUMBER(1) DEFAULT 0 NOT NULL,
    transfer_reason VARCHAR2(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_ai_message_session FOREIGN KEY (session_id) REFERENCES ai_session(id),
    CONSTRAINT fk_ai_message_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT ck_ai_message_role CHECK (role IN ('USER', 'ASSISTANT')),
    CONSTRAINT ck_ai_message_can_answer CHECK (can_answer IS NULL OR can_answer IN (0, 1)),
    CONSTRAINT ck_ai_message_transfer CHECK (transfer_suggested IN (0, 1))
);

CREATE TABLE ai_message_citation (
    id NUMBER(19) DEFAULT ai_message_citation_seq.NEXTVAL PRIMARY KEY,
    message_id NUMBER(19) NOT NULL,
    chunk_id NUMBER(19) NOT NULL,
    document_id NUMBER(19) NOT NULL,
    citation_index NUMBER(10) NOT NULL,
    source_title VARCHAR2(200) NOT NULL,
    snippet VARCHAR2(1000) NOT NULL,
    similarity NUMBER(8, 6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_ai_citation_message FOREIGN KEY (message_id) REFERENCES ai_message(id),
    CONSTRAINT fk_ai_citation_chunk FOREIGN KEY (chunk_id) REFERENCES kb_chunk(id),
    CONSTRAINT fk_ai_citation_document FOREIGN KEY (document_id) REFERENCES kb_document(id),
    CONSTRAINT uk_ai_citation_message_index UNIQUE (message_id, citation_index)
);

CREATE INDEX idx_ai_session_user_updated ON ai_session (user_id, updated_at);
CREATE INDEX idx_ai_message_session_created ON ai_message (session_id, created_at);
CREATE INDEX idx_ai_citation_message ON ai_message_citation (message_id);
