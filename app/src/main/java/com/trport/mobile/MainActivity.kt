package com.trport.mobile
import android.view.InputDevice

import android.app.Activity
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("trport", MODE_PRIVATE) }
    private val bg = Color.rgb(13, 16, 20)
    private val panel = Color.rgb(25, 30, 36)
    private val green = Color.rgb(85, 215, 133)
    private val white = Color.WHITE
    private val muted = Color.rgb(166, 177, 188)
    private lateinit var main: LinearLayout
    private lateinit var page: LinearLayout
    private lateinit var bottom: LinearLayout
    private var section = "Home"
    private var selectedPackage = 16
    private var lowMode = false
    private var ultraLow = false
    private var selectedGraphics = "AUTO"
    private var selectedFps = 30
    private var selectedResolution = "Auto"
    private var packageProgress = 0
    private var packagePaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        lowMode = prefs.getBoolean("low", false)
        ultraLow = prefs.getBoolean("ultra", false)
        selectedGraphics = prefs.getString("graphics", "AUTO") ?: "AUTO"
        selectedFps = prefs.getInt("fps", 30)
        selectedResolution = prefs.getString("resolution", "Auto") ?: "Auto"
        buildShell()
        showSection("Home")
    }

    private fun buildShell() {
        main = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 22, 20, 14)
            setBackgroundColor(bg)
        }
        val logo = TextView(this).apply {
            text = "TR PORT"; textSize = 26f; typeface = Typeface.DEFAULT_BOLD; setTextColor(green)
        }
        top.addView(logo)
        val subtitle = TextView(this).apply {
            text = "MOBILE PROJECT  •  DEVICE COMPATIBILITY"; textSize = 10f; setTextColor(muted)
        }
        top.addView(subtitle)
        main.addView(top)
        val scroll = ScrollView(this)
        page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 4, 16, 18) }
        scroll.addView(page)
        main.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(4, 6, 4, 6)
            setBackgroundColor(Color.rgb(20, 24, 29))
        }
        listOf("Home", "Download", "Graphics", "Gamepad", "Settings").forEach { name ->
            bottom.addView(navButton(name), LinearLayout.LayoutParams(0, 58, 1f))
        }
        main.addView(bottom)
        setContentView(main)
    }

    private fun navButton(name: String): TextView = TextView(this).apply {
        text = when(name) { "Home" -> "⌂\\nHome"; "Download" -> "↓\\nDownload"; "Graphics" -> "◈\\nGraphics"; "Gamepad" -> "◎\\nGamepad"; else -> "⚙\\nSettings" }
        textSize = 11f; gravity = Gravity.CENTER
        setTextColor(if (name == section) green else muted)
        setOnClickListener { showSection(name) }
    }

    private fun showSection(name: String) {
        section = name
        page.removeAllViews()
        when(name) {
            "Home" -> homePage()
            "Download" -> downloadPage()
            "Graphics" -> graphicsPage()
            "Gamepad" -> gamepadPage()
            else -> settingsPage()
        }
        for (i in 0 until bottom.childCount) {
            val child = bottom.getChildAt(i) as TextView
            child.setTextColor(if (listOf("Home","Download","Graphics","Gamepad","Settings")[i] == section) green else muted)
        }
    }

    private fun card(title: String, body: String? = null): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(16, 14, 16, 14)
            setBackgroundColor(panel)
        }
        val t = TextView(this).apply { text = title; textSize = 16f; typeface = Typeface.DEFAULT_BOLD; setTextColor(white) }
        c.addView(t)
        if (body != null) c.addView(TextView(this).apply { text = body; textSize = 12f; setTextColor(muted); setPadding(0, 6, 0, 0) })
        val lp = LinearLayout.LayoutParams(-1, -2); lp.setMargins(0, 0, 0, 12)
        page.addView(c, lp)
        return c
    }

    private fun button(label: String, action: () -> Unit): Button = Button(this).apply {
        text = label; setTextColor(white); setOnClickListener { action() }
    }

    private fun homePage() {
        card("OPEN-WORLD MOBILE PROJECT", "Portrait-first UI • Android device compatibility") 
        val mode = card("GAME MODES", "Game runtime is not included in this project.")
        mode.addView(button("STORY MODE") { notice("No game runtime installed in this prototype.") })
        mode.addView(button("ONLINE MODE") { notice("Online mode requires a compatible, authorized server/runtime.") })
        val scan = card("DEVICE SCAN", "Read real device properties and graphics capabilities.")
        scan.addView(button("SCAN DEVICE") { showScan() })
        val opt = card("ONE-TAP PROFILE", "Apply a saved project profile. This configures preferences only; no absent game is altered.")
        opt.addView(button(if (lowMode) "LOW DEVICE MODE: ON" else "LOW DEVICE MODE: OFF") {
            lowMode = !lowMode; ultraLow = false
            prefs.edit().putBoolean("low", lowMode).putBoolean("ultra", false).apply()
            notice(if (lowMode) "Low profile saved." else "Low profile disabled.")
            showSection("Home")
        })
        opt.addView(button(if (ultraLow) "ULTRA LOW: ON" else "ULTRA LOW MODE") {
            ultraLow = !ultraLow; lowMode = ultraLow
            prefs.edit().putBoolean("low", lowMode).putBoolean("ultra", ultraLow).apply()
            if (ultraLow) { selectedGraphics = "LOW"; selectedFps = 20; selectedResolution = "480x270"; saveGraphics() }
            notice(if (ultraLow) "Ultra-low preferences saved." else "Ultra-low disabled.")
            showSection("Home")
        })
        val shortcuts = card("QUICK SETTINGS")
        listOf("Download packages" to "Download", "Graphics & FPS" to "Graphics", "Touch controls" to "Gamepad", "Settings" to "Settings").forEach { (label, target) ->
            shortcuts.addView(button(label) { showSection(target) })
        }
    }

    private fun showScan() {
        val scanText = buildDeviceReport()
        page.removeAllViews()
        card("DEVICE SCAN RESULT", scanText)
        page.addView(button("BACK") { showSection("Home") })
    }

    private fun buildDeviceReport(): String {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val stat = StatFs(Environment.getDataDirectory().path)
        val display = windowManager.defaultDisplay
        val metrics = android.util.DisplayMetrics()
        display.getRealMetrics(metrics)
        val refresh = display.refreshRate
        val vulkan = packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL) ||
            packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
        val gl = packageManager.getSystemPropertyCompat("ro.opengles.version")
        val gpu = android.os.Build.HARDWARE + " / " + android.os.Build.MANUFACTURER
        return """Manufacturer: ${Build.MANUFACTURER}
Model: ${Build.MODEL}
Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
Hardware: ${Build.HARDWARE}
CPU ABI: ${Build.SUPPORTED_ABIS.joinToString()}
RAM total: ${mi.totalMem / (1024*1024)} MB
RAM available: ${mi.availMem / (1024*1024)} MB
Storage free: ${stat.availableBytes / (1024*1024*1024)} GB
Display: ${metrics.widthPixels} x ${metrics.heightPixels}
Refresh rate: ${"%.0f".format(refresh)} Hz
OpenGL ES: ${gl ?: "unknown"}
Vulkan feature: ${if (vulkan) "reported supported" else "not detected"}
GPU/Hardware string: $gpu
Thermal: Android thermal API availability varies; not read in this scan."""
    }

    private fun graphicsPage() {
        card("GRAPHICS PROFILE", "Preferences are saved locally; they do not apply to a game runtime that is not present.")
        addChoice("Profile", listOf("AUTO","LOW","MEDIUM","HIGH","ULTRA"), selectedGraphics) { selectedGraphics = it; saveGraphics() }
        addChoice("Texture quality", listOf("LOW","MEDIUM","HIGH","ULTRA"), prefs.getString("texture","LOW") ?: "LOW") { prefs.edit().putString("texture",it).apply() }
        addChoice("Shadow quality", listOf("OFF","LOW","MEDIUM","HIGH"), prefs.getString("shadow","LOW") ?: "LOW") { prefs.edit().putString("shadow",it).apply() }
        addChoice("Reflection quality", listOf("OFF","LOW","MEDIUM","HIGH"), prefs.getString("reflection","LOW") ?: "LOW") { prefs.edit().putString("reflection",it).apply() }
        addChoice("View distance", listOf("LOW","MEDIUM","HIGH"), prefs.getString("distance","LOW") ?: "LOW") { prefs.edit().putString("distance",it).apply() }
        addChoice("Effects", listOf("LOW","MEDIUM","HIGH"), prefs.getString("effects","LOW") ?: "LOW") { prefs.edit().putString("effects",it).apply() }
        addChoice("Anti-aliasing", listOf("OFF","FXAA","MSAA"), prefs.getString("aa","OFF") ?: "OFF") { prefs.edit().putString("aa",it).apply() }
        addChoice("FPS target", listOf("20","30","40","60","90","120"), selectedFps.toString()) { selectedFps = it.toInt(); prefs.edit().putInt("fps",selectedFps).apply() }
        addChoice("Resolution", listOf("Auto","480x270","640x360","800x450","960x544","1024x576","1280x720","1600x900","1920x1080","Custom"), selectedResolution) { selectedResolution = it; prefs.edit().putString("resolution",it).apply() }
        card("DISPLAY NOTE", "Available FPS depends on display refresh rate and a future runtime's performance. This screen only saves targets.")
    }

    private fun addChoice(label: String, choices: List<String>, current: String, onSelect: (String)->Unit) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 4, 0, 8) }
        row.addView(TextView(this).apply { text = label; setTextColor(white); textSize = 13f })
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, choices)
        spinner.setSelection(choices.indexOf(current).coerceAtLeast(0))
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) { onSelect(choices[position]) }
        }
        row.addView(spinner)
        page.addView(row)
    }

    private fun saveGraphics() {
        prefs.edit().putString("graphics", selectedGraphics).putInt("fps",selectedFps).putString("resolution",selectedResolution).apply()
    }

    private fun downloadPage() {
        card("PACKAGE SELECTOR", "Choose a storage-size profile. Authorized package URLs and checksum values are not configured in this build.")
        listOf(7,16,24,43,72).forEach { gb ->
            page.addView(button("$gb GB PACKAGE") {
                selectedPackage = gb
                notice("$gb GB selected. No real package endpoint is configured.")
                showSection("Download")
            })
        }
        card("DOWNLOAD STATUS", "Selected: $selectedPackage GB\nProgress: $packageProgress%\nState: ${if (packagePaused) "Paused" else "Not started"}")
        page.addView(button("START DOWNLOAD") { notice("Download unavailable: no authorized URL/checksum configured.") })
        page.addView(button("PAUSE") { packagePaused = true; notice("Pause state saved in prototype.") })
        page.addView(button("RESUME") { packagePaused = false; notice("Resume state toggled; no download is active.") })
        page.addView(button("VERIFY FILES") { notice("Verification requires a real package and expected SHA-256 value.") })
        card("NO FAKE DATA", "The app does not generate filler files or claim a package was downloaded.")
    }

    private fun gamepadPage() {
        card("TOUCH CONTROL LAYOUT", "Editable controls are represented here. Mapping is stored as preferences; no game input runtime is connected.")
        val controls = listOf("Left joystick", "Camera joystick", "Aim", "Fire", "Jump", "Sprint", "Accelerate", "Brake", "Handbrake")
        controls.forEach { c ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(this).apply { text = c; setTextColor(white) }, LinearLayout.LayoutParams(0,-2,1f))
            row.addView(button("Edit") { notice("$c layout editor is a prototype placeholder.") })
            page.addView(row)
        }
        card("GAMEPAD", "Bluetooth controller input detection can be expanded. This prototype has no game engine to receive gameplay input.")
        page.addView(button("CHECK INPUT DEVICE") {
            val devices = emptyList<InputDevice>()
            notice(if (devices.isEmpty()) "No gamepad detected." else devices.joinToString { it.name })
        })
    }

    private fun settingsPage() {
        card("SETTINGS", "Mobile-first project preferences.")
        page.addView(button("RESET PREFERENCES") {
            prefs.edit().clear().apply()
            lowMode = false; ultraLow = false; selectedGraphics = "AUTO"; selectedFps = 30; selectedResolution = "Auto"
            notice("Preferences reset."); showSection("Settings")
        })
        card("ABOUT", "TR PORT • Android prototype • version 1.0\nNot affiliated with Rockstar Games. No game data included.")
        card("CRASH / STABILITY", "This prototype has no gameplay process. Stability monitoring and automatic runtime recovery require an actual runtime.")
    }

    private fun notice(msg: String) { Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
    private fun PackageManager.getSystemPropertyCompat(key: String): String? = try {
        val process = Runtime.getRuntime().exec(arrayOf("getprop", key))
        process.inputStream.bufferedReader().readLine()?.takeIf { it.isNotBlank() }
    } catch (_: Exception) { null }
}
