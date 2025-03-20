package com.gabriel.cal.ui.macros

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gabriel.cal.MacroEntry
import com.gabriel.cal.SharedViewModel
import java.util.Calendar
import java.util.UUID
import androidx.core.graphics.toColorInt

class MacrosFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MacrosScreen(sharedViewModel)
            }
        }
    }
}

@Composable
fun MacrosScreen(viewModel: SharedViewModel) {
    val macros by viewModel.macros.observeAsState(emptySet())
    var editingMacro by remember { mutableStateOf<MacroEntry?>(null) }
    var creatingNewMacro by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Column {
            if (editingMacro != null || creatingNewMacro) {
                MacroForm(
                    initialMacro = editingMacro,
                    onSaveMacro = { macro ->
                        if (creatingNewMacro)
                            viewModel.addMacro(macro.name, macro.color, macro.hour, macro.minute)
                        else
                            viewModel.updateMacro(context, macro)
                        editingMacro = null
                        creatingNewMacro = false
                    },
                    onCancel = {
                        editingMacro = null
                        creatingNewMacro = false
                    }
                )
            } else {
                Button(
                    onClick = { creatingNewMacro = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Crear Nueva Macro")
                }
                MacrosList(
                    macros = macros.toList(),
                    onDeleteMacro = viewModel::removeMacro,
                    onEditMacro = { macro -> editingMacro = macro }
                )
            }
        }
    }
}

@Composable
fun MacrosList(
    macros: List<MacroEntry>,
    onDeleteMacro: (MacroEntry) -> Unit,
    onEditMacro: (MacroEntry) -> Unit
) {
    LazyColumn {
        items(macros, key = { it.id }) { macro ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = try {
                        androidx.compose.ui.graphics.Color(macro.color.toColorInt())
                    } catch (e: Exception) {
                        androidx.compose.ui.graphics.Color.White
                    }
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    // Información de la macro alineada a la izquierda
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = macro.name)
                        Text(text = "Hora: ${macro.hour}:${macro.minute.toString().padStart(2, '0')}")
                    }
                    // Botones "Modificar" y "Borrar" alineados a la derecha
                    Row {
                        Button(
                            onClick = { onEditMacro(macro) },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("Modificar")
                        }
                        Button(
                            onClick = { onDeleteMacro(macro) },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                        ) {
                            Text("Borrar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroForm(
    initialMacro: MacroEntry?,
    onSaveMacro: (MacroEntry) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var name by remember(initialMacro) { mutableStateOf(initialMacro?.name ?: "") }
    // Opciones de color: (nombre, valor hexadecimal)
    val colorOptions = listOf(
        "Rojo" to "#FF0000",
        "Verde" to "#00FF00",
        "Azul" to "#0000FF",
        "Amarillo" to "#FFFF00"
    )
    var selectedColorHex by remember(initialMacro) { mutableStateOf(initialMacro?.color ?: colorOptions.first().second) }
    val selectedColorName = colorOptions.find { it.second == selectedColorHex }?.first ?: "Rojo"
    var hour by remember(initialMacro) { mutableStateOf(initialMacro?.hour ?: 0) }
    var minute by remember(initialMacro) { mutableStateOf(initialMacro?.minute ?: 0) }
    var colorDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CustomNameField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre"
        )
        Box {
            OutlinedButton(onClick = { colorDropdownExpanded = true }) {
                Text("Color seleccionado: $selectedColorName")
            }
            DropdownMenu(
                expanded = colorDropdownExpanded,
                onDismissRequest = { colorDropdownExpanded = false }
            ) {
                colorOptions.forEach { (colorName, colorHex) ->
                    DropdownMenuItem(
                        onClick = {
                            selectedColorHex = colorHex
                            colorDropdownExpanded = false
                        },
                        text = { Text(text = colorName) }
                    )
                }
            }
        }
        Button(onClick = {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    hour = selectedHour
                    minute = selectedMinute
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }) {
            Text("Seleccionar hora: $hour:${minute.toString().padStart(2, '0')}")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (name.isBlank()) {
                    // Puedes mostrar un mensaje de error aquí
                } else {
                    onSaveMacro(
                        initialMacro?.copy(
                            name = name,
                            color = selectedColorHex,
                            hour = hour,
                            minute = minute
                        ) ?: MacroEntry(UUID.randomUUID().toString(), name, selectedColorHex, hour, minute)
                    )
                }
            }) {
                Text("Guardar")
            }
            OutlinedButton(onClick = onCancel) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
fun CustomNameField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Nombre"
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(4.dp)) {
                if (value.isEmpty()) {
                    Text(
                        text = label,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}
