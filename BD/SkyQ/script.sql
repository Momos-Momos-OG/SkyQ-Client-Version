-- 1. Creación e inicialización segura de la Base de Datos
IF DB_ID('skyq_db') IS NULL
BEGIN
    CREATE DATABASE skyq_db;
END;
GO

USE skyq_db;
GO

-- 2. Eliminación ordenada de tablas existentes para evitar conflictos de dependencias (Baja en Cascada)
IF OBJECT_ID('dbo.configuracion_asientos', 'U') IS NOT NULL DROP TABLE dbo.configuracion_asientos;
IF OBJECT_ID('dbo.equipaje', 'U') IS NOT NULL DROP TABLE dbo.equipaje;
IF OBJECT_ID('dbo.pasajero', 'U') IS NOT NULL DROP TABLE dbo.pasajero;
IF OBJECT_ID('dbo.pilotos', 'U') IS NOT NULL DROP TABLE dbo.pilotos;
IF OBJECT_ID('dbo.aviones', 'U') IS NOT NULL DROP TABLE dbo.aviones;
GO

-- 3. Tabla: Aviones comerciales de la flota
CREATE TABLE dbo.aviones (
                             matricula VARCHAR(20) NOT NULL,
                             modelo VARCHAR(100) NOT NULL,
                             capacidad INT NOT NULL,
                             estado VARCHAR(50) NOT NULL,
                             CONSTRAINT PK_aviones PRIMARY KEY (matricula)
);
GO

-- 4. Tabla: Pasajeros registrados
CREATE TABLE dbo.pasajero (
                              idPasajero INT IDENTITY(1,1) NOT NULL,
                              nombre VARCHAR(100) NOT NULL,
                              numAsiento VARCHAR(20) NOT NULL,
                              nivelPrioridad INT NOT NULL,
                              timestampLlegada DATETIME2 NULL,
                              CONSTRAINT PK_pasajero PRIMARY KEY (idPasajero)
);
GO

-- 5. Tabla: Registro de maletas y pesos por pasajero
CREATE TABLE dbo.equipaje (
                              idMaleta INT IDENTITY(1,1) NOT NULL,
                              idPasajero INT NOT NULL,
                              peso DECIMAL(10,2) NOT NULL,
                              estado VARCHAR(20) NOT NULL,
                              CONSTRAINT PK_equipaje PRIMARY KEY (idMaleta),
                              CONSTRAINT FK_equipaje_pasajero
                                  FOREIGN KEY (idPasajero)
                                      REFERENCES dbo.pasajero (idPasajero)
                                      ON UPDATE CASCADE
                                      ON DELETE CASCADE
);
GO

-- 6. Tabla: Staff técnico de vuelo (Pilotos)
CREATE TABLE dbo.pilotos (
                             idPiloto INT IDENTITY(1,1) NOT NULL,
                             nombre VARCHAR(100) NOT NULL,
                             rango VARCHAR(50) NOT NULL,
                             estado VARCHAR(30) NOT NULL,
                             CONSTRAINT PK_pilotos PRIMARY KEY (idPiloto)
);
GO

-- 7. Tabla: Configuraciones geométricas personalizadas de cabinas hechas por el Gerente
CREATE TABLE dbo.configuracion_asientos (
                                            matricula VARCHAR(20) NOT NULL, -- Corregido: VARCHAR(20) para coincidir exactamente con dbo.aviones
                                            filas INT NOT NULL,
                                            columnas INT NOT NULL,
                                            pasillos VARCHAR(100) NOT NULL,
                                            CONSTRAINT PK_configuracion_asientos PRIMARY KEY (matricula),
                                            CONSTRAINT FK_configuracion_aviones
                                                FOREIGN KEY (matricula)
                                                    REFERENCES dbo.aviones(matricula)
                                                    ON UPDATE CASCADE
                                                    ON DELETE CASCADE
);
GO

-- ==========================================================
-- 🚀 INSERCIÓN DE DATOS INICIALES DE PRUEBA (SEED DATA)
-- ==========================================================

-- Flota inicial extraída directamente del Backlog oficial de Figma
INSERT INTO dbo.aviones (matricula, modelo, capacidad, estado) VALUES
('HC-BXA', 'Boeing 737-800', 162, 'Disponible'),
('HC-CJP', 'Airbus A320', 150, 'Disponible'),
('HC-DMK', 'ATR 72-600', 68, 'En mantenimiento'),
('HC-EAQ', 'Embraer E190', 96, 'Disponible');
GO

-- Distribuciones pre-configuradas para los dos primeros aviones
INSERT INTO dbo.configuracion_asientos (matricula, filas, columnas, pasillos) VALUES
('HC-BXA', 14, 7, '3'),   -- 14 filas, 7 columnas de ancho total, pasillo en la columna 3
('HC-CJP', 12, 7, '3');   -- 12 filas, 7 columnas de ancho total, pasillo en la columna 3
GO

-- Pilotos base de la aerolínea
INSERT INTO dbo.pilotos (nombre, rango, estado) VALUES
('Cap. Carlos Mendoza', 'Comandante', 'Disponible'),
('Cap. Ana Guevara', 'Comandante', 'Disponible'),
('F.O. Luis Rojas', 'Co-Piloto', 'En Vuelo');
GO

ALTER TABLE dbo.pasajero ADD matricula VARCHAR(20) NOT NULL DEFAULT 'HC-BXA';
GO