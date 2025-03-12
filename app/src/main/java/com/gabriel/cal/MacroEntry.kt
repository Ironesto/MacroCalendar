package com.gabriel.cal

data class MacroEntry(
    val id: String = "", // Se generará un ID único (por ejemplo, usando UUID)
    val name: String = "",
    val color: String = "", // Puedes almacenar el color en formato hexadecimal, por ejemplo, "#FF0000"
    val hour: Int = 0,
    val minute: Int = 0
)
