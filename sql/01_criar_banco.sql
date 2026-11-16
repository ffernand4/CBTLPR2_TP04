/*
 * ADS 471 - Fernanda Cristina Oliveira Pinheiro
 * Letícia Amaral Xavier
 *
 * TRABALHO PRÁTICO 04
 *
 * Desenvolver uma interface gráfica em Java para consultar
 * funcionários cadastrados em um banco SQL Server.
 *
 * A aplicação utiliza JDBC, PreparedStatement e ResultSet
 * para pesquisar funcionários pelo nome e navegar entre
 * os registros utilizando os botões Anterior e Próximo.
 */

IF DB_ID(N'aulajava') IS NULL
    CREATE DATABASE aulajava;
GO
USE aulajava;
GO
IF OBJECT_ID(N'dbo.tb_func', N'U') IS NOT NULL DROP TABLE dbo.tb_func;
IF OBJECT_ID(N'dbo.tb_cargo', N'U') IS NOT NULL DROP TABLE dbo.tb_cargo;
GO
CREATE TABLE dbo.tb_cargo (
    id_cargo INT NOT NULL PRIMARY KEY,
    ds_cargo VARCHAR(20) NOT NULL
);
GO
CREATE TABLE dbo.tb_func (
    cod_func INT NOT NULL PRIMARY KEY,
    nome_func VARCHAR(50) NOT NULL,
    sal_func DECIMAL(10,2) NOT NULL,
    cod_cargo INT NOT NULL,
    CONSTRAINT FK_tb_func_tb_cargo FOREIGN KEY (cod_cargo) REFERENCES dbo.tb_cargo(id_cargo)
);
GO
INSERT INTO dbo.tb_cargo (id_cargo, ds_cargo) VALUES
(1, 'Administrativo'), (2, 'Analista'), (3, 'Gerente'), (4, 'Tecnico');
INSERT INTO dbo.tb_func (cod_func, nome_func, sal_func, cod_cargo) VALUES
(1, 'Marcelo', 2000.00, 1),
(2, 'Marcia', 2800.50, 2),
(3, 'Marcos', 3500.00, 3),
(4, 'Maria', 2300.00, 1),
(5, 'Fernanda', 3200.00, 2),
(6, 'Joao', 2500.00, 4),
(7, 'Ana', 4100.00, 3),
(8, 'Mariana', 2700.00, 4);
GO
SELECT f.cod_func, f.nome_func, f.sal_func, c.ds_cargo
FROM dbo.tb_func f INNER JOIN dbo.tb_cargo c ON f.cod_cargo = c.id_cargo
ORDER BY f.cod_func;
GO