[cite_start]Create src/main/resources/db/migration/V1_Create_initial_tables.sql [cite: 61]
-- Custom ENUM types
[span_0](start_span)CREATE TYPE user_profile AS ENUM ('ADMIN', 'USUARIO');[span_0](end_span)
[span_1](start_span)CREATE TYPE question_type AS ENUM ('TEXT', 'CHOICE', 'YES_NO', 'SCALE');[span_1](end_span)

-- Companies/Hotels Table
CREATE TABLE companies (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL UNIQUE
                               [span_2](start_span));[span_2](end_span)

-- Users Table
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
    [span_3](start_span)login VARCHAR(100) NOT NULL UNIQUE,[span_3](end_span)
    password VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    must_change_password BOOLEAN DEFAULT TRUE,
    profile user_profile NOT NULL,
    company_id BIGINT,
    CONSTRAINT fk_company FOREIGN KEY (company_id) REFERENCES companies(id)
        [span_4](start_span));[span_4](end_span)

-- Survey Sections Table
CREATE TABLE survey_sections (
                                 id BIGSERIAL PRIMARY KEY,
    [span_5](start_span)name VARCHAR(255) NOT NULL,[span_5](end_span)
    deny_use BOOLEAN DEFAULT FALSE,
    language VARCHAR(10) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    company_id BIGINT NOT NULL,
    CONSTRAINT fk_company_section FOREIGN KEY (company_id) REFERENCES companies(id),
    UNIQUE (name, language, company_id) -- Name must be unique per company and language
        [span_6](start_span));[span_6](end_span)

-- Questions Table
CREATE TABLE questions (
                           id BIGSERIAL PRIMARY KEY,
                           survey_section_id BIGINT NOT NULL,
    [span_7](start_span)label TEXT NOT NULL,[span_7](end_span)
    [span_8](start_span)type question_type NOT NULL,[span_8](end_span)
    mandatory BOOLEAN DEFAULT FALSE,
    options TEXT, -- Stored as a comma-separated string or JSON for simplicity, consider a separate table for complex options
    CONSTRAINT fk_survey_section FOREIGN KEY (survey_section_id) REFERENCES survey_sections(id)
        [span_9](start_span));[span_9](end_span)