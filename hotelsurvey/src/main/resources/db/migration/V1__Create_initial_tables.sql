-- Tipos ENUM customizados
CREATE TYPE user_profile AS ENUM ('ADMIN', 'USUARIO');
CREATE TYPE question_type AS ENUM ('TEXT', 'CHOICE', 'YES_NO', 'SCALE');
-- Tabela de Empresas/Hotéis
CREATE TABLE companies ( id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL
);
-- Tabela de Usuários
CREATE TABLE users ( id BIGSERIAL PRIMARY KEY, login VARCHAR(100) NOT NULL UNIQUE, password VARCHAR(255) NOT NULL, active BOOLEAN NOT NULL DEFAULT true, must_change_password BOOLEAN NOT NULL DEFAULT true, profile user_profile NOT NULL, company_id BIGINT REFERENCES companies(id)
);
-- Tabela de Seções dos Formulários
CREATE TABLE survey_sections ( id BIGSERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, active BOOLEAN NOT NULL DEFAULT true, deny_use BOOLEAN NOT NULL DEFAULT false, language VARCHAR(10) NOT NULL, company_id BIGINT NOT NULL REFERENCES companies(id), CONSTRAINT uq_section_name_company_language UNIQUE (name, company_id, language)
);
-- Tabela de Perguntas
CREATE TABLE questions ( id BIGSERIAL PRIMARY KEY, label TEXT NOT NULL, type question_type NOT NULL, mandatory BOOLEAN NOT NULL DEFAULT false, options TEXT[] DEFAULT '{}', section_id BIGINT NOT NULL REFERENCES survey_sections(id) ON DELETE CASCADE
);