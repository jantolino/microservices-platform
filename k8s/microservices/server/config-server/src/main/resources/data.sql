-- Ejemplo de propiedades para distintos ambientes
INSERT INTO PROPERTIES (APPLICATION, PROFILE, LABEL, PROP_KEY, PROP_VALUE) VALUES
('myservice', 'dev', 'main', 'message', 'Hola desde DEV'),
('myservice', 'qa',  'main', 'message', 'Hola desde QA'),
('myservice', 'inc', 'main', 'message', 'Hola desde INC'),
('myservice', 'prod','main', 'message', 'Hola desde PROD');
