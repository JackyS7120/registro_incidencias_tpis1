# Registro de Indicencias - Practica Formativa

## Proposito
Aplicacion movil desarrollada para el registro y gestion de incidencias de equipos

## Herramientas utilizadas
- **IDE:** Android Studio
- **Lenguaje:** Kotlin
- **UI Framework:** Jetpack Compose
- **Control de versiones:** Git y GitHub

## Estado actual del proyecto
- Configuracion inicial del proyecto Android.
- Interfaz grafica basica con pantalla de inicio.
- Componentes visuales y campos de entrada.
- Soporte para desplazamiento vertical en orientacion horizontal.
- Manejo de estado basico con 'remember' y 'mutableStateOf' para captura de datos
- Configuración de teclado IME con capitalización de oraciones (`Sentences`), salto de foco (`ImeAction.Next`) y ocultamiento automático (`ImeAction.Done`).
- Interacción táctil en selector de criticidad (*Baja*, *Media*, *Alta*) con retroalimentación visual perimetral y tonal mediante `Card` y `clickable`.
- **Integración de Sensores de Movimiento:**
    - Consulta de disponibilidad y consumo de hardware mediante `SensorManager` y `Sensor.TYPE_ACCELEROMETER`.
    - Escucha reactiva en tiempo real de los ejes espaciales $X, Y, Z$ mediante `SensorEventListener`.
    - Control de ciclo de vida seguro mediante `DisposableEffect` para registrar el listener al entrar a la vista y liberarlo obligatoriamente (`unregisterListener`) al abandonarla, evitando fugas de memoria y consumo innecesario de batería.
    - Tarjeta de diagnóstico de hardware en interfaz que interpreta la aceleración total del dispositivo para alertar sobre reposo o posibles impactos/caídas.
    - Manejo preventivo para dispositivos sin sensor compatible (`sensor == null`).
## Como ejecutar el proyecto
1. Clonar este repositorio o descargarlo como zip
2. Abrir la carpeta en **Android Studio**
3. Esperar a que **Gradle** sincronice las dependencias automaticas
4. Ejecutar la aplicacion en un emulador AVD o dispositivo fisico

--------------------

**Alumna:** Jacqueline Beatriz Serrano Robles
**Asignatura:** Tecnicas de Produccion Industrial de Software I (Ciclo 02-2026)
**Institucion:** Universidad Tecnologica de El Salvador