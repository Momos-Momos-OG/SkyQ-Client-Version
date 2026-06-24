-- ============================================================
-- SkyQ - Schema de Base de Datos v4.0 (Aeropuerto Único)
-- PILOTOS y HOSPEDAJE eliminados (fuera de alcance SRS v4.0)
-- SQL Server 2025 en Docker (puerto 1433)
-- ============================================================

-- 1. Creación e inicialización segura de la Base de Datos
IF DB_ID('skyq_db') IS NULL
BEGIN
    CREATE DATABASE skyq_db;
END;
GO

USE skyq_db;
GO

-- 2. Eliminación ordenada respetando dependencias (baja en cascada correcta)
IF OBJECT_ID('dbo.hospedaje_piloto',      'U') IS NOT NULL DROP TABLE dbo.hospedaje_piloto;
IF OBJECT_ID('dbo.mantenimiento',         'U') IS NOT NULL DROP TABLE dbo.mantenimiento;
IF OBJECT_ID('dbo.vuelos',                'U') IS NOT NULL DROP TABLE dbo.vuelos;
IF OBJECT_ID('dbo.configuracion_asientos','U') IS NOT NULL DROP TABLE dbo.configuracion_asientos;
IF OBJECT_ID('dbo.equipaje',              'U') IS NOT NULL DROP TABLE dbo.equipaje;
IF OBJECT_ID('dbo.pasajero',              'U') IS NOT NULL DROP TABLE dbo.pasajero;
IF OBJECT_ID('dbo.pilotos',               'U') IS NOT NULL DROP TABLE dbo.pilotos;
IF OBJECT_ID('dbo.auditoria',             'U') IS NOT NULL DROP TABLE dbo.auditoria;
IF OBJECT_ID('dbo.usuarios',              'U') IS NOT NULL DROP TABLE dbo.usuarios;
IF OBJECT_ID('dbo.aviones',               'U') IS NOT NULL DROP TABLE dbo.aviones;
GO

-- ============================================================
-- TABLA -1: auditoria
-- Log centralizado de todas las acciones del sistema
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'auditoria')
BEGIN
    CREATE TABLE dbo.auditoria (
        idAuditoria INT           IDENTITY(1,1) NOT NULL,
        username    VARCHAR(50)   NOT NULL,
        accion      VARCHAR(50)   NOT NULL,
        detalle     VARCHAR(255)  NULL,
        fecha_hora  DATETIME2     DEFAULT GETDATE(),
        CONSTRAINT PK_auditoria PRIMARY KEY (idAuditoria)
    );
END;
GO

-- ============================================================
-- TABLA 0: usuarios
-- Roles: 'GERENTE' | 'OPERARIO'
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'usuarios')
BEGIN
    CREATE TABLE dbo.usuarios (
        idUsuario       INT           IDENTITY(1,1) NOT NULL,
        username        VARCHAR(50)   NOT NULL UNIQUE,
        password_hash   VARCHAR(255)  NOT NULL,
        rol             VARCHAR(30)   NOT NULL,    -- 'GERENTE' | 'OPERARIO'
        estado          VARCHAR(20)   NOT NULL,    -- 'Activo' | 'Inactivo'
        fechaCreacion   DATETIME2     DEFAULT GETDATE(),
        CONSTRAINT PK_usuarios PRIMARY KEY (idUsuario)
    );
END;
GO

-- ============================================================
-- TABLA 1: aviones
-- Estados: 'EN_TERMINAL' | 'EN_VUELO' | 'EN_MANTENIMIENTO'
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'aviones')
BEGIN
    CREATE TABLE dbo.aviones (
        matricula VARCHAR(20)  NOT NULL,
        modelo    VARCHAR(100) NOT NULL,
        capacidad INT          NOT NULL,
        estado    VARCHAR(50)  NOT NULL,
        CONSTRAINT PK_aviones PRIMARY KEY (matricula)
    );
END;
GO

-- ============================================================
-- TABLA 2: pasajero
-- Contiene atributos SRS: sillaRuedas, upgrade, pnr
-- nivelPrioridad: 1=VIP, 2=Ejecutiva, 3=Económica
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'pasajero')
BEGIN
    CREATE TABLE dbo.pasajero (
        idPasajero       INT           IDENTITY(1,1) NOT NULL,
        nombre           VARCHAR(100)  NOT NULL,
        numAsiento       VARCHAR(20)   NOT NULL,
        nivelPrioridad   INT           NOT NULL,      -- 1=VIP, 2=Ejecutiva, 3=Económica
        timestampLlegada DATETIME2     NULL,
        matricula        VARCHAR(20)   NOT NULL DEFAULT 'HC-BXA',  -- FK lógica → aviones
        pnr              VARCHAR(20)   NULL,
        sillaRuedas      BIT           NOT NULL DEFAULT 0,
        upgrade          BIT           NOT NULL DEFAULT 0,
        CONSTRAINT PK_pasajero PRIMARY KEY (idPasajero)
    );
END;
GO

-- ============================================================
-- TABLA 3: equipaje
-- Estados: 'Registrado' | 'Embarcado' | 'Entregado'
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'equipaje')
BEGIN
    CREATE TABLE dbo.equipaje (
        idMaleta   INT            IDENTITY(1,1) NOT NULL,
        idPasajero INT            NOT NULL,
        peso       DECIMAL(10,2)  NOT NULL,
        estado     VARCHAR(20)    NOT NULL,
        CONSTRAINT PK_equipaje PRIMARY KEY (idMaleta),
        CONSTRAINT FK_equipaje_pasajero
            FOREIGN KEY (idPasajero) REFERENCES dbo.pasajero(idPasajero)
            ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO

-- ============================================================
-- TABLA 4: configuracion_asientos
-- Formato distribucion_clases: "VIP:2-2:4|ECON:3-3:20"
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'configuracion_asientos')
BEGIN
    CREATE TABLE dbo.configuracion_asientos (
        matricula            VARCHAR(20)  NOT NULL,
        distribucion_clases  VARCHAR(255) NOT NULL,
        CONSTRAINT PK_configuracion_asientos PRIMARY KEY (matricula),
        CONSTRAINT FK_configuracion_aviones
            FOREIGN KEY (matricula) REFERENCES dbo.aviones(matricula)
            ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO

-- ============================================================
-- TABLA 5: mantenimiento
-- Historial de mantenimientos de la flota
-- Estados: 'Programado' | 'En Curso' | 'Completado'
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'mantenimiento')
BEGIN
    CREATE TABLE dbo.mantenimiento (
        idMantenimiento INT           IDENTITY(1,1) NOT NULL,
        matricula       VARCHAR(20)   NOT NULL,    -- FK → aviones
        fechaInicio     DATE          NOT NULL,
        fechaFin        DATE          NULL,
        descripcion     VARCHAR(255)  NOT NULL,
        estado          VARCHAR(30)   NOT NULL,
        CONSTRAINT PK_mantenimiento PRIMARY KEY (idMantenimiento),
        CONSTRAINT FK_mantenimiento_avion
            FOREIGN KEY (matricula) REFERENCES dbo.aviones(matricula)
            ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO

-- ============================================================
-- TABLA 6: vuelos
-- Registro de vuelos del aeropuerto único.
-- SIN FK a pilotos (fuera de alcance SRS v4.0)
-- Estados: 'Programado' | 'En Vuelo' | 'Completado' | 'Cancelado'
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'vuelos')
BEGIN
    CREATE TABLE dbo.vuelos (
        idVuelo     INT          IDENTITY(1,1) NOT NULL,
        matricula   VARCHAR(20)  NOT NULL,    -- FK → aviones
        codigoVuelo VARCHAR(20)  NULL,        -- Ej: "UIO-305"
        fechaSalida DATETIME2    NOT NULL,
        fechaArribo DATETIME2    NOT NULL,
        estado      VARCHAR(30)  NOT NULL DEFAULT 'Programado',
        origen      VARCHAR(100) NULL,
        destino     VARCHAR(100) NULL,
        CONSTRAINT PK_vuelos PRIMARY KEY (idVuelo),
        CONSTRAINT FK_vuelos_avion
            FOREIGN KEY (matricula) REFERENCES dbo.aviones(matricula)
            ON UPDATE CASCADE ON DELETE CASCADE
    );
END;
GO
