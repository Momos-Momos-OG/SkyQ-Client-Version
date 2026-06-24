-- ============================================================
-- SkyQ - Seed Data v4.0 (Aeropuerto Único)
-- PILOTOS y HOSPEDAJE eliminados (Out of Scope SRS)
-- ============================================================

USE skyq_db;
GO

-- 1. Usuarios del sistema
-- GERENTE:  user="gerente"  pass="Gerente@123"
-- OPERARIO: user="operario" pass="Operario@123"
INSERT INTO dbo.usuarios (username, password_hash, rol, estado) VALUES
('gerente',  'Gerente@123',  'GERENTE',  'Activo'),
('operario', 'Operario@123', 'OPERARIO', 'Activo');
GO

-- 2. Flota de aviones (Aeropuerto Único)
-- 2 en terminal, 1 en mantenimiento, 1 en vuelo
INSERT INTO dbo.aviones (matricula, modelo, capacidad, estado) VALUES
('HC-BXA', 'Boeing 737-800', 136, 'EN_TERMINAL'),
('HC-CJP', 'Airbus A320',    152, 'EN_TERMINAL'),
('HC-DMK', 'ATR 72-600',      68, 'EN_MANTENIMIENTO'),
('HC-EAQ', 'Embraer E190',    96, 'EN_VUELO');
GO

-- 3. Distribuciones de cabina pre-configuradas
INSERT INTO dbo.configuracion_asientos (matricula, distribucion_clases) VALUES
('HC-BXA', 'VIP:2-2:4|ECON:3-3:20'),
('HC-CJP', 'VIP:2-2:5|ECON:3-3:22'),
('HC-DMK', 'VIP:2-2:2|ECON:3-3:10'),
('HC-EAQ', 'VIP:2-2:3|ECON:3-3:14');
GO

-- 4. Mantenimiento activo de flota
INSERT INTO dbo.mantenimiento (matricula, fechaInicio, fechaFin, descripcion, estado) VALUES
('HC-DMK', '2026-06-15', NULL, 'Inspección estructural activa de motores y fuselaje', 'En Curso');
GO

-- 5. Vuelos del aeropuerto (sin FK a pilotos)
-- Vuelo UIO-305: HC-EAQ sale hacia Miami (EN VUELO)
-- Vuelo GYE-820: HC-BXA sale hacia Madrid (PROGRAMADO)
INSERT INTO dbo.vuelos (matricula, codigoVuelo, fechaSalida, fechaArribo, estado, origen, destino) VALUES
('HC-EAQ', 'UIO-305', '2026-06-24 10:00:00', '2026-06-24 18:00:00', 'En Vuelo',   'Aeropuerto Local', 'Miami'),
('HC-BXA', 'GYE-820', '2026-06-25 18:00:00', '2026-06-26 08:00:00', 'Programado', 'Aeropuerto Local', 'Madrid');
GO

-- 6. Grupo familiar con mismo PNR (Demostración de US-03)
-- Regla US-03: grupo hereda la MÁXIMA prioridad del miembro de mayor jerarquía.
-- Juan tiene sillaRuedas=1 → todo el grupo SQ-77X9 abordará primero.
-- Vuelo GYE-820 → HC-BXA
INSERT INTO dbo.pasajero (nombre, numAsiento, nivelPrioridad, timestampLlegada, matricula, pnr, sillaRuedas, upgrade) VALUES
('Juan Perez (Adulto - VIP/Silla)',   '1A', 1, '2026-06-25 15:00:00', 'HC-BXA', 'SQ-77X9', 1, 0),
('Maria Perez (Adulto - Ejecutiva)',  '2B', 2, '2026-06-25 15:05:00', 'HC-BXA', 'SQ-77X9', 0, 0),
('Pedrito Perez (Niño - Económica)',  '5C', 3, '2026-06-25 15:10:00', 'HC-BXA', 'SQ-77X9', 0, 0),
('Sonia Perez (Adulto - Económica)', '5D', 3, '2026-06-25 15:15:00', 'HC-BXA', 'SQ-77X9', 0, 0);
GO

-- 7. Pasajeros individuales (sin PNR compartido) para demostración de cola pura
-- Vuelo GYE-820 → HC-BXA
INSERT INTO dbo.pasajero (nombre, numAsiento, nivelPrioridad, timestampLlegada, matricula, pnr, sillaRuedas, upgrade) VALUES
('Carlos Ruiz (VIP)',         '3A', 1, '2026-06-25 15:20:00', 'HC-BXA', 'IND-001', 0, 0),
('Elena Torres (Upgrade)',    '3B', 2, '2026-06-25 15:25:00', 'HC-BXA', 'IND-002', 0, 1),
('Miguel Vega (Económica)',   '6A', 3, '2026-06-25 15:30:00', 'HC-BXA', 'IND-003', 0, 0);
GO
