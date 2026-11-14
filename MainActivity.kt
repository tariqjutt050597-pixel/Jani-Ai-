package com.jaaniai.app

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale("ur", "PK")
        }
        setContent { JaaniScreen(
            onSpeak = { text -> tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jaani_reply") },
            onWhatsApp = { text ->
                val i = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"; setPackage("com.whatsapp"); putExtra(Intent.EXTRA_TEXT, text)
                }
                runCatching { startActivity(i) }
            },
            onSms = { text -> startActivity(Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("smsto:")
                putExtra("sms_body", text)
            }) },
            onDial = { startActivity(Intent(Intent.ACTION_DIAL)) }
        ) }
    }
    override fun onDestroy() { tts?.stop(); tts?.shutdown(); super.onDestroy() }
}

@Composable
fun JaaniScreen(onSpeak: (String) -> Unit, onWhatsApp: (String) -> Unit, onSms: (String) -> Unit, onDial: () -> Unit) {
    var heard by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("Roman Urdu") }
    var status by remember { mutableStateOf("Main yahan hoon, bolo jaan.") }
    var showConfirm by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        if (spoken.isNotBlank()) {
            heard = spoken
            draft = when (language) {
                "English" -> spoken
                "Hindi" -> spoken
                "Urdu" -> spoken
                else -> romanizeBasic(spoken)
            }
            status = "Message tayyar hai. Bhejne se pehle check kar lo."
            showConfirm = true
        }
    }
    val purple = Color(0xFF24103D)
    val pink = Color(0xFFFF3EC8)
    MaterialTheme(colorScheme = darkColorScheme(primary = pink, background = Color(0xFF10071D), surface = purple)) {
        Column(Modifier.fillMaxSize().background(Color(0xFF10071D)).verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💗 JAANI AI", color = pink, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
            Text("Oye Jaani!  •  Your Personal Voice Assistant", color = Color.White, fontSize = 13.sp)
            Spacer(Modifier.height(16.dp))
            Card(colors = CardDefaults.cardColors(containerColor = purple), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text(status, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    Text("Conversation language", color = Color.LightGray)
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expanded = true }) { Text(language) }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("Roman Urdu", "English", "Hindi", "Urdu").forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = { language = item; expanded = false })
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Bolo, main sun rahi hoon")
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, when(language) {
                                "English" -> "en-US"; "Hindi" -> "hi-IN"; "Urdu" -> "ur-PK"; else -> "ur-PK"
                            })
                        }
                        runCatching { launcher.launch(intent) }.onFailure { status = "Is phone par speech recognition available nahi." }
                    }, modifier = Modifier.fillMaxWidth()) { Text("🎙  Oye Jaani — Bolo") }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = draft, onValueChange = { draft = it }, label = { Text("Message draft") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onSpeak(status) }, modifier = Modifier.weight(1f)) { Text("Speak") }
                        OutlinedButton(onClick = { showConfirm = true }, modifier = Modifier.weight(1f)) { Text("Review") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onWhatsApp(draft) }, modifier = Modifier.weight(1f)) { Text("WhatsApp") }
                        OutlinedButton(onClick = { onSms(draft) }, modifier = Modifier.weight(1f)) { Text("SMS") }
                    }
                    OutlinedButton(onClick = onDial, modifier = Modifier.fillMaxWidth()) { Text("Open phone dialer") }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Prototype 0.1 • Wake phrase background mode abhi included nahi.", color = Color.LightGray, fontSize = 11.sp)
        }
    }
    if (showConfirm) AlertDialog(
        onDismissRequest = { showConfirm = false },
        title = { Text("Message check kar lo") },
        text = { Text(draft.ifBlank { "Abhi koi message nahi likha." }) },
        confirmButton = { TextButton(onClick = { showConfirm = false; status = "Message draft ready hai. App choose karke send karo." }) { Text("Theek hai") } },
        dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Edit") } }
    )
}

// Safe placeholder: preserves the recognized text rather than pretending to translate it.
private fun romanizeBasic(text: String): String = text
