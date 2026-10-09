package sv.edu.utec.etps1.registroincidencias

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sv.edu.utec.etps1.registroincidencias.ui.theme.RegistroIncidenciasTheme
import kotlin.math.sqrt

// ============================================================================
// 1. PUNTO DE ENTRADA DE LA APLICACIÓN (Activity)
// ============================================================================
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegistroIncidenciasTheme {
                RegistroIncidenciasApp()
            }
        }
    }
}

// ============================================================================
// 2. COMPONENTE PRINCIPAL DE LA PANTALLA
// ============================================================================
@Composable
fun RegistroIncidenciasApp() {
    // ------------------------------------------------------------------------
    // SECCIÓN A: ESTADOS REACTIVOS DEL FORMULARIO
    // Guardan en memoria el texto ingresado, la opción activa y la respuesta final.
    // Al mutar cualquiera de estas variables, Compose recompondrá solo los nodos necesarios.
    // ------------------------------------------------------------------------
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf("Media")}
    var mensaje by remember { mutableStateOf("No se encontraron registros.") }

    // Controladores de interfaz: desplazamiento, foco de teclado y visibilidad de teclado
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val prioridades = listOf("Baja", "Media", "Alta")

    // ------------------------------------------------------------------------
    // SECCIÓN B: GESTIÓN DEL SENSOR HARDWARE (Acelerómetro)
    // Conecta la app con el subsistema de sensores de Android mediante SensorManager.
    // ------------------------------------------------------------------------
    val context = LocalContext.current

    // Obtenemos el servicio del sistema SensorManager
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    // Consultamos si el dispositivo físico posee un acelerómetro[cite: 5, 6]
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    // Estados reactivos para reflejar las lecturas físicas en tiempo real en la pantalla
    var sensorDisponible by remember { mutableStateOf(accelerometer != null) }
    var xVal by remember { mutableFloatStateOf(0f) }
    var yVal by remember { mutableFloatStateOf(0f) }
    var zVal by remember { mutableFloatStateOf(0f) }
    var estado by remember { mutableStateOf("Dispositivo estable") }

    // ------------------------------------------------------------------------
    // SECCIÓN C: CICLO DE VIDA CONTROLADO CON DisposableEffect
    // Evita registrar múltiples escuchadores en cada recomposición y previene fugas de memoria.
    // ------------------------------------------------------------------------
    DisposableEffect(accelerometer) {
        // Si el dispositivo no tiene acelerómetro disponible, registramos la limitación
        if (accelerometer == null) {
            sensorDisponible = false
            onDispose {  }
        } else {
            // Escuchador que recibe los eventos físicos cada vez que el dispositivo se mueve
            val listener = object: SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    // Extracción de los 3 ejes de aceleración en m/s^2
                    val x = event.values[0] // Eje lateral (izquierda / derecha)
                    val y = event.values[1] // Eje vertical (arriba / abajo)
                    val z = event.values[2] // Eje perpendicular
                    xVal = x
                    yVal = y
                    zVal = z

                    // Cálculo del vector de aceleración total: sqrt(x² + y² + z²)
                    // En reposo, la gravedad terrestre aporta ~9.8 m/s²
                    val totalAcc = sqrt((x * x + y * y + z * z).toDouble())
                    // Interpretación: si la aceleración supera 15 m/s², se considera movimiento brusco
                    estado = if (totalAcc > 15.0) {
                        "Movimiento brusco o posible caida detectado!!"
                    } else {
                        "Dispositivo en reposo :)"
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Invocado si cambia la precisión del sensor
                }
            }
            // Registramos el listener con una frecuencia adecuada para UI (SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            // onDispose se ejecuta al salir de la pantalla o destruirse el componente:
            // Desregistra el sensor para ahorrar batería y no dejar procesos huérfanos
            onDispose {
                sensorManager.unregisterListener(listener)

            }
        }
    }

    // ------------------------------------------------------------------------
    // SECCIÓN D: ESTRUCTURA VISUAL DE LA INTERFAZ (UI)
    // ------------------------------------------------------------------------
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                // verticalScroll garantiza que el teclado no tape los botones ni las tarjetas
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Text(
                    // Encabezado de la pantalla
                    text = "Control de equipos",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Llenar el siguiente formulario para registrar la " +
                            "incidencia de un equipo.",
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ------------------------------------------------------------
                // SUB-SECCIÓN D1: TARJETA DE DIAGNÓSTICO DE HARDWARE (SENSOR)
                // Muestra valores en tiempo real o mensaje de no disponibilidad
                // ------------------------------------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Diagnostico de Hardware: Acelerometro",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!sensorDisponible) {
                            Text(
                                text = " Estado: Sensor no disponible en este dispositivo",
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                text = "Lecturas: X = %.2f | Y = %.2f | Z = %.2f".format(xVal, yVal, zVal),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Estado: $estado",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (estado.contains("brusco")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ------------------------------------------------------------
                // SUB-SECCIÓN D2: ENTRADA DE TEXTO CON ACCIONES IME
                // ------------------------------------------------------------
                // Campo Título: Mayúscula automática y salto con tecla Next
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Titulo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down)}
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Campo Descripción: Mayúscula automática y cierre con tecla Done
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripcion") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide()}
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // ------------------------------------------------------------
                // SUB-SECCIÓN D3: SELECTOR TÁCTIL DE PRIORIDAD (Cards)
                // ------------------------------------------------------------
                Text(
                    text = "Prioridad de la incidencia: ",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    prioridades.forEach { item ->
                        val isSelected = prioridad == item
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { prioridad = item }, // Interacción táctil
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            border = if (isSelected) {
                                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                            } else null
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Text(
                                    text = item,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ------------------------------------------------------------
                // SUB-SECCIÓN D4: ACCIÓN PRINCIPAL Y RETROALIMENTACIÓN
                // ------------------------------------------------------------
                Button(
                    onClick = {
                        keyboardController?.hide()
                        mensaje = if (titulo.isNotBlank() && descripcion.isNotBlank()) {
                            "Reporte preparado: $titulo [Prioridad $prioridad]"
                        } else {
                            "Por favor ingresa un titulo y una descripcion para la incidencia."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Crear reporte")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta de retroalimentación final
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Equipos reportados.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = mensaje)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

            }
            // Pie de página con versión del prototipo
            Text(
                text = "Prototipo inicial - Sengundo Avance con Sensor.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegistroIncidenciasPreview() {
    RegistroIncidenciasTheme {
        RegistroIncidenciasApp()
    }
}