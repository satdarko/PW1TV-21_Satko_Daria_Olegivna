package com.example.kotlinpr1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class FuelComposition(
    val H: Double,
    val C: Double,
    val S: Double,
    val N: Double,
    val O: Double,
    val W: Double,
    val A: Double
)

object FuelCalculator {
    fun kToDry(W: Double) = 100.0 / (100.0 - W)
    fun kToCombustible(W: Double, A: Double) = 100.0 / (100.0 - W - A)

    // Нижча теплота згоряння для робочої маси (формула Мендєлєєва)
    fun lowerHeatingValue(fuel: FuelComposition): Double {
        return 339 * fuel.C +
                1030 * fuel.H -
                108.8 * (fuel.O - fuel.S) -
                25 * fuel.W
    }

    // Перерахунок Qр -> Qd (суха маса)
    fun toDryHeat(Qr: Double, W: Double): Double {
        return Qr * 100.0 / (100.0 - W)
    }

    // Перерахунок Qр -> Qdaf (горюча маса)
    fun toCombHeat(Qr: Double, W: Double, A: Double): Double {
        return Qr * 100.0 / (100.0 - W - A)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorUI()
        }
    }
}

@Composable
fun CalculatorUI() {
    var H by remember { mutableStateOf("2.8") }
    var C by remember { mutableStateOf("72.3") }
    var S by remember { mutableStateOf("2.0") }
    var N by remember { mutableStateOf("1.10") }
    var O by remember { mutableStateOf("1.30") }
    var W by remember { mutableStateOf("5.5") }
    var A by remember { mutableStateOf("15.0") }

    var result by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        Text("Мобільний калькулятор палива", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = H, onValueChange = { H = it }, label = { Text("H (%)") })
        OutlinedTextField(value = C, onValueChange = { C = it }, label = { Text("C (%)") })
        OutlinedTextField(value = S, onValueChange = { S = it }, label = { Text("S (%)") })
        OutlinedTextField(value = N, onValueChange = { N = it }, label = { Text("N (%)") })
        OutlinedTextField(value = O, onValueChange = { O = it }, label = { Text("O (%)") })
        OutlinedTextField(value = W, onValueChange = { W = it }, label = { Text("W (%)") })
        OutlinedTextField(value = A, onValueChange = { A = it }, label = { Text("A (%)") })

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            val fuel = FuelComposition(
                H.toDoubleOrNull() ?: 0.0,
                C.toDoubleOrNull() ?: 0.0,
                S.toDoubleOrNull() ?: 0.0,
                N.toDoubleOrNull() ?: 0.0,
                O.toDoubleOrNull() ?: 0.0,
                W.toDoubleOrNull() ?: 0.0,
                A.toDoubleOrNull() ?: 0.0
            )
            val kDry = FuelCalculator.kToDry(fuel.W)
            val kComb = FuelCalculator.kToCombustible(fuel.W, fuel.A)

            // Суха маса
            val Hd = fuel.H * kDry
            val Cd = fuel.C * kDry
            val Sd = fuel.S * kDry
            val Nd = fuel.N * kDry
            val Od = fuel.O * kDry
            val Ad = fuel.A * kDry

            // Горюча маса
            val Hg = fuel.H * kComb
            val Cg = fuel.C * kComb
            val Sg = fuel.S * kComb
            val Ng = fuel.N * kComb
            val Og = fuel.O * kComb

            // Теплота згоряння
            val Qr = FuelCalculator.lowerHeatingValue(fuel) / 1000.0
            val Qd = FuelCalculator.toDryHeat(Qr, fuel.W)
            val Qdaf = FuelCalculator.toCombHeat(Qr, fuel.W, fuel.A)

            result = """
                ► Коефіцієнти:
                kDry (роб → суха) = %.2f
                kComb (роб → горюча) = %.2f

                ► Склад сухої маси:
                H = %.2f%%, C = %.2f%%, S = %.2f%%, N = %.2f%%, O = %.2f%%, A = %.2f%%

                ► Склад горючої маси:
                H = %.2f%%, C = %.2f%%, S = %.2f%%, N = %.2f%%, O = %.2f%%

                ► Нижча теплота згоряння:
                Qr (робоча маса) = %.3f МДж/кг
                Qd (суха маса)   = %.3f МДж/кг
                Qdaf (горюча)    = %.3f МДж/кг
            """.trimIndent().format(
                kDry, kComb,
                Hd, Cd, Sd, Nd, Od, Ad,
                Hg, Cg, Sg, Ng, Og,
                Qr, Qd, Qdaf
            )
        }) {
            Text("Розрахувати")
        }

        Spacer(Modifier.height(16.dp))
        Text(result)
    }
}
