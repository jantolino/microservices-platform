<#
.SYNOPSIS
Script para crear la estructura de carpetas de un microservicio siguiendo la arquitectura hexagonal.

.DESCRIPTION
Este script crea una estructura de carpetas completa para un microservicio Java siguiendo
los principios de la arquitectura hexagonal (puertos y adaptadores). La estructura incluye
carpetas para la capa de aplicación, dominio e infraestructura, así como recursos y pruebas.

.PARAMETER ServiceName
Nombre del microservicio a crear. Este nombre se utilizará para nombrar la carpeta del servicio
y como parte del paquete Java. Por defecto es "service".

.PARAMETER BasePackage
Paquete base Java para el microservicio. El nombre del servicio se añadirá al final.
Por defecto es "edu.market".

.PARAMETER OutputPath
Ruta donde se creará la estructura de carpetas. Por defecto es el directorio actual (".").

.EXAMPLE
.\create-structure.ps1 -ServiceName "notification-service" -BasePackage "edu.market" -OutputPath ".\services\notification-service\"

Crea la estructura de carpetas para un microservicio de notificaciones en la ruta especificada.
#>

param (
    [Parameter(Mandatory=$false)]
    [string]$ServiceName = "service",
    
    [Parameter(Mandatory=$false)]
    [string]$BasePackage = "edu.market",
    
    [Parameter(Mandatory=$false)]
    [string]$OutputPath = "."
)

# Convertir el nombre del servicio a minúsculas para el paquete Java
# Esto asegura consistencia en la nomenclatura de los paquetes Java
$serviceNameLower = $ServiceName.ToLower()

# Construir la ruta base donde se crearán todas las carpetas
# Join-Path garantiza que las rutas sean correctas independientemente del sistema operativo
$basePath = Join-Path -Path $OutputPath -ChildPath ""

# Crear la estructura de carpetas base
# Estas son carpetas principales que no forman parte de la estructura Java estándar
$baseFolders = @(
    "logs"  # Carpeta para almacenar los archivos de registro de la aplicación
)

# Crear estructura de código fuente siguiendo la convención Maven/Gradle
# Esta estructura es estándar para proyectos Java con Spring Boot
$sourceFolders = @(
    "src\main\java",             # Código fuente principal
    "src\main\resources",  # Archivos de configuración
    "src\main\resources\static",  # Recursos estáticos (CSS, JS, imágenes)
    "src\main\resources\templates", # Plantillas (Thymeleaf, FreeMarker, etc.)    
    "src\test\java",             # Código de pruebas
    "src\test\resources"          # Recursos para pruebas
)

# Construir la estructura de paquetes Java
# Convertimos los puntos del paquete en separadores de directorios
# Ejemplo: edu.market.notification-service se convierte en edu\market\notification-service
$packagePath = "src\main\java\" + $BasePackage.Replace(".", "\") + "\$serviceNameLower"
$packagePathTest = "src\test\java\" + $BasePackage.Replace(".", "\") + "\$serviceNameLower"

# Definir la estructura hexagonal para código fuente
# Esta estructura sigue el patrón de arquitectura hexagonal (puertos y adaptadores)
$hexagonalFolders = @(
    # Application layer - Capa de aplicación que contiene la lógica de casos de uso
    "$packagePath\application\port\input",    # Puertos de entrada (interfaces que definen los casos de uso)
    "$packagePath\application\usecase",       # Implementaciones de casos de uso
    
    # Domain layer - Capa de dominio que contiene la lógica de negocio
    "$packagePath\domain\enums",             # Enumeraciones del dominio
    "$packagePath\domain\event",             # Eventos de dominio
    "$packagePath\domain\exception",         # Excepciones específicas del dominio
    "$packagePath\domain\mapper",            # Mapeadores de objetos de dominio
    "$packagePath\domain\model",             # Entidades y agregados del dominio
    "$packagePath\domain\port\output",       # Puertos de salida (interfaces para repositorios y servicios externos)
    "$packagePath\domain\service",           # Servicios de dominio
    "$packagePath\domain\vo",                # Value Objects
    
    # Infrastructure layer - Capa de infraestructura que implementa los adaptadores
    "$packagePath\infrastructure\adapter\input\web\api",                # Definiciones de API
    "$packagePath\infrastructure\adapter\input\web\controller",         # Controladores REST
    "$packagePath\infrastructure\adapter\input\web\dto\request",       # DTOs para solicitudes
    "$packagePath\infrastructure\adapter\input\web\dto\response",      # DTOs para respuestas
    "$packagePath\infrastructure\adapter\input\web\mapper",           # Mapeadores para la capa web
    "$packagePath\infrastructure\adapter\output\event",               # Adaptadores para eventos
    "$packagePath\infrastructure\adapter\output\persistence\entity",   # Entidades JPA/persistencia
    "$packagePath\infrastructure\adapter\output\persistence\mapper",   # Mapeadores para persistencia
    "$packagePath\infrastructure\adapter\output\persistence\repository", # Repositorios
    "$packagePath\infrastructure\config",                             # Configuraciones
    "$packagePath\infrastructure\crosscutting\exception",              # Manejo de excepciones
    "$packagePath\infrastructure\crosscutting\logging"                # Configuración de logs
)

# Definir la estructura hexagonal para pruebas
#$testFolders = @(
#    # Application layer tests
#    "$packagePathTest\application\usecase",
#    
#    # Domain layer tests
#    "$packagePathTest\domain\service",
#    
#    # Infrastructure layer tests
#    "$packagePathTest\infrastructure\adapter\input\web\controller",
#    "$packagePathTest\infrastructure\adapter\output\persistence",
#    "$packagePathTest\infrastructure\config"
#)

# Combinar todas las carpetas definidas en un solo array para procesarlas
$allFolders = $baseFolders + $sourceFolders + $hexagonalFolders

# Crear todas las carpetas definidas en la estructura
foreach ($folder in $allFolders) {
    # Construir la ruta completa para cada carpeta
    $fullPath = Join-Path -Path $basePath -ChildPath $folder
    # Crear el directorio (-Force crea directorios intermedios si no existen)
    New-Item -Path $fullPath -ItemType Directory -Force | Out-Null
    # Mostrar mensaje de confirmación
    Write-Host "Creado: $fullPath"
}

# Mostrar resumen de la operación
Write-Host ""
Write-Host "Estructura de carpetas creada exitosamente para el servicio '$ServiceName'"
Write-Host "Paquete base: $BasePackage.$serviceNameLower"
Write-Host "Ubicación: $basePath"
