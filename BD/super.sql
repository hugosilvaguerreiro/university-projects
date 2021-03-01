DROP TABLE categoria cascade;
DROP TABLE categoria_simples cascade;
DROP TABLE super_categoria cascade;
DROP TABLE constituida cascade;
DROP TABLE produto cascade;
DROP TABLE fornecedor cascade;
DROP TABLE fornece_sec cascade;
DROP TABLE fornce_sec cascade;
DROP TABLE corredor cascade;
DROP TABLE prateleria cascade;
DROP TABLE prateleira cascade;
DROP TABLE planograma cascade;
DROP TABLE evento_reposicao cascade;
DROP TABLE reposicao cascade;

----------------------------------------
-- Table Creation
----------------------------------------

-- Named CONSTRAINTs are global to the database.
-- Therefore the following use the following naming rules:
--   1. pk_table for names of PRIMARY KEY CONSTRAINTs
--   2. fk_table_another for names of foreign key CONSTRAINTs

CREATE TYPE e_lado as enum(
  'esquerdo', 
  'direito');

CREATE TABLE categoria
   (nome varchar(80) NOT NULL UNIQUE,
    CONSTRAINT pk_categoria PRIMARY KEY(nome));

CREATE TABLE categoria_simples
   (nome varchar(80) NOT NULL UNIQUE,
    CONSTRAINT pk_categoria_simples PRIMARY KEY(nome),
    CONSTRAINT fk_categoria_simples_categoria foreign key(nome) 
               REFERENCES categoria ON DELETE CASCADE);

CREATE TABLE super_categoria
   (nome varchar(80) NOT NULL UNIQUE,
    CONSTRAINT pk_super_categoria PRIMARY KEY(nome),
    CONSTRAINT fk_super_categoria_cateforia foreign key(nome)
               REFERENCES categoria ON DELETE CASCADE);
    
CREATE TABLE constituida
   (super_categoria varchar(80) NOT NULL,
    categoria varchar(80) NOT NULL,
    CONSTRAINT pk_constituida PRIMARY KEY(super_categoria, categoria),
    CONSTRAINT fk_constituida_super_categoria foreign key(super_categoria) 
               REFERENCES super_categoria ON DELETE CASCADE,
    CONSTRAINT fk_constituida_categoria foreign key(categoria) 
               REFERENCES categoria ON DELETE CASCADE,
    CONSTRAINT ct_super_differs_categoria CHECK(super_categoria != categoria));

CREATE TABLE fornecedor
    (nif varchar(20) NOT NULL UNIQUE,
     nome varchar(80) NOT NULL,
     CONSTRAINT pk_fornecedor PRIMARY KEY(nif));

CREATE TABLE produto
    (ean bigint NOT NULL UNIQUE,
     design varchar(180) NOT NULL,
     categoria varchar(80) NOT NULL,
     forn_primario varchar(20) NOT NULL,
     data date,
     CONSTRAINT pk_produto PRIMARY KEY(ean),
     CONSTRAINT fk_produto_categoria foreign key(categoria) 
                REFERENCES categoria ON DELETE CASCADE,
     CONSTRAINT fk_produto_fornecedor foreign key(forn_primario) 
                REFERENCES fornecedor ON DELETE CASCADE);

CREATE TABLE fornece_sec
    (nif varchar(20) NOT NULL,
     ean bigint NOT NULL,
     CONSTRAINT pk_fornece_sec PRIMARY KEY(nif, ean),
     CONSTRAINT fk_fornece_sec_fornecedor foreign key(nif) 
                REFERENCES fornecedor ON DELETE CASCADE,
     CONSTRAINT fk_fornece_sec_produto foreign key(ean) 
                REFERENCES produto ON DELETE CASCADE);
     
CREATE TABLE corredor
    (nro int NOT NULL UNIQUE,
     largura real NOT NULL,
     CONSTRAINT pk_corredor PRIMARY KEY(nro),
    CONSTRAINT ct_largura_positive CHECK (largura >= 0));
     
CREATE TABLE prateleira
    (nro int NOT NULL,
     lado e_lado NOT NULL,
     altura real NOT NULL,
     CONSTRAINT pk_prateleira PRIMARY KEY(nro, lado, altura),
     CONSTRAINT fk_prateleira_corredor foreign key(nro) 
                REFERENCES corredor ON DELETE CASCADE,
    CONSTRAINT ct_altura_positive CHECK(altura >= 0));
     
CREATE TABLE planograma
    (ean bigint NOT NULL,
     nro int NOT NULL,
     lado e_lado NOT NULL,
     altura real NOT NULL,
     face varchar(80) NOT NULL,
     unidades int NOT NULL,
     loc varchar(80) NOT NULL,
     CONSTRAINT pk_planograma PRIMARY KEY(ean, nro, lado, altura),
     CONSTRAINT fk_planograma_produto foreign key(ean) 
                REFERENCES produto ON DELETE CASCADE,
     CONSTRAINT fk_planograma_prateleira foreign key(nro, lado, altura) 
                REFERENCES prateleira ON DELETE CASCADE);
     
CREATE TABLE evento_reposicao
    (operador varchar(80) NOT NULL,
     instante timestamp NOT NULL,
     CONSTRAINT pk_evento_reposicao PRIMARY KEY(operador, instante),
     CONSTRAINT ct_instante_not_in_future CHECK(instante <= now()));
     
CREATE TABLE reposicao
    (ean bigint NOT NULL,
     nro int NOT NULL,
     lado e_lado NOT NULL,
     altura real NOT NULL,
     operador varchar(80) NOT NULL ,
     instante timestamp NOT NULL ,
     unidades int NOT NULL,
     CONSTRAINT pk_reposicao PRIMARY KEY(ean, nro, lado, altura, operador, instante),
     CONSTRAINT fk_reposicao_planograma foreign key(ean, nro, lado, altura) 
                REFERENCES planograma ON DELETE CASCADE,
     CONSTRAINT fk_reposicao_evento_reposicao foreign key(operador, instante) 
                REFERENCES evento_reposicao ON DELETE CASCADE);
     
