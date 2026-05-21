package com.example

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DownloadedPack
import com.example.data.model.QuickPhrase
import com.example.data.model.TranslationHistory
import com.example.ui.theme.*
import com.example.ui.viewmodel.*
import kotlinx.coroutines.delay
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.google.accompanist.permissions.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: TranslationViewModel = viewModel()
                MainAppContent(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainAppContent(viewModel: TranslationViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFFAF9F6)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.Splash -> SplashScreen(viewModel)
                AppScreen.Onboarding -> OnboardingScreen(viewModel)
                else -> AppContainerLayout(viewModel)
            }
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(viewModel: TranslationViewModel) {
    var startPulse by remember { mutableStateOf(false) }
    val pulseScale by animateFloatAsState(
        targetValue = if (startPulse) 1.15f else 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    LaunchedEffect(Unit) {
        startPulse = true
        delay(2600) // Beautiful cinematic loader period
        viewModel.navigateTo(AppScreen.Onboarding)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        Color(0xFF03001e)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .drawBehind {
                        drawCircle(
                            Brush.radialGradient(
                                colors = listOf(ElectricCyan.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            radius = size.width * 0.75f
                        )
                    }
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = "App Logo",
                    tint = ElectricCyan,
                    modifier = Modifier.size(72.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "AI OFFLINE TRANSLATOR",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.SansSerif
                ),
                color = ElectricCyan,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "HYBRID TRANSLATION ENGINE",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Running glowing progress bar
            val infiniteTransition = rememberInfiniteTransition(label = "Progress")
            val progressOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ProgressOffset"
            )

            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(60.dp)
                        .offset(x = (120 * progressOffset).dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(ElectricCyan, NeonPurple)
                            )
                        )
                )
            }
        }
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(viewModel: TranslationViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.navigateTo(AppScreen.Home) },
                    modifier = Modifier.testTag("skip_button")
                ) {
                    Text("SKIP", color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Translate,
                    contentDescription = "Onboard Icon",
                    tint = NeonPurple,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = "Smart Translating, Anytime",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A revolutionary dual translator featuring on-device Offline High-Speed Packages and generative Gemini AI modes. No internet? No worries.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                OnboardFeatureItem(Icons.Filled.NetworkWifi, "Gemini Hybrid Pro Engine", "Connects to Google Gemini for contextual translations.")
                OnboardFeatureItem(Icons.Filled.SignalWifiOff, "100% On-Device Packs", "Instant text/voice lookups directly in 10+ languages.")
                OnboardFeatureItem(Icons.Filled.CameraAlt, "Augmented OCR Vision", "Point and shoot menus, boards, and books to overlay translations.")
            }

            Button(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .navigationBarsPadding()
                    .testTag("get_started_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "GET STARTED", 
                    color = MaterialTheme.colorScheme.onPrimary, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun OnboardFeatureItem(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ElectricCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = ElectricCyan, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f))
        }
    }
}


// 3. RESPONSIVE NAVIGATION CONTAINER (BOTTOM NAV BAR + SIDE RAIL)
@Composable
fun AppContainerLayout(viewModel: TranslationViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val screens = listOf(
        NavItem(AppScreen.Home, "Translate", Icons.Filled.Translate, Icons.Outlined.Translate),
        NavItem(AppScreen.VoiceTranslate, "Voice", Icons.Filled.Mic, Icons.Outlined.Mic),
        NavItem(AppScreen.Conversation, "Conversation", Icons.Filled.Forum, Icons.Outlined.Forum),
        NavItem(AppScreen.CameraTranslate, "Camera", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
        NavItem(AppScreen.LiveSubtitle, "Live Sub", Icons.Filled.Subtitles, Icons.Outlined.Subtitles),
    )

    val secondaryScreens = listOf(
        NavItem(AppScreen.DownloadPacks, "Packs", Icons.Filled.DownloadForOffline, Icons.Outlined.DownloadForOffline),
        NavItem(AppScreen.TravelMode, "Travel Guide", Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore),
        NavItem(AppScreen.History, "History", Icons.Filled.History, Icons.Outlined.History),
        NavItem(AppScreen.Favorites, "Favorites", Icons.Filled.Star, Icons.Outlined.StarBorder),
        NavItem(AppScreen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
        NavItem(AppScreen.Profile, "Profile", Icons.Filled.Person, Icons.Outlined.Person),
    )

    // Layout configuration depending on width classes
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp
        
        Row(modifier = Modifier.fillMaxSize()) {
            if (isWide) {
                // Adaptive Navigation Rail
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    val combinedList = screens + secondaryScreens
                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(combinedList) { item ->
                            val isSelected = currentScreen == item.screen
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.screen) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) ElectricCyan else SubtleGray
                                    )
                                },
                                label = { Text(item.label, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = ElectricCyan,
                                    selectedTextColor = ElectricCyan,
                                    indicatorColor = ElectricCyan.copy(alpha = 0.2f),
                                    unselectedIconColor = SubtleGray,
                                    unselectedTextColor = SubtleGray
                                )
                            )
                        }
                    }
                }
            }

            // Screen content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentScreen) {
                            AppScreen.Home -> HomeTranslationScreen(viewModel)
                            AppScreen.VoiceTranslate -> VoiceTranslationScreen(viewModel)
                            AppScreen.Conversation -> ConversationModeScreen(viewModel)
                            AppScreen.CameraTranslate -> CameraTranslationScreen(viewModel)
                            AppScreen.LiveSubtitle -> LiveSubtitleScreen(viewModel)
                            AppScreen.DownloadPacks -> DownloadPacksScreen(viewModel)
                            AppScreen.TravelMode -> TravelModeScreen(viewModel)
                            AppScreen.History -> HistoryScreen(viewModel)
                            AppScreen.Favorites -> FavoritesScreen(viewModel)
                            AppScreen.Settings -> SettingsScreen(viewModel)
                            AppScreen.Profile -> ProfileScreen(viewModel)
                            else -> HomeTranslationScreen(viewModel)
                        }
                    }

                    if (!isWide) {
                        // Standard Bottom Navigation Bar with scrolling secondary tray or slide panel for extras!
                        BottomAppBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                screens.forEach { item ->
                                    val isSelected = currentScreen == item.screen
                                    val actColor = if (isSelected) ElectricCyan else SubtleGray

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.navigateTo(item.screen) }
                                            .padding(8.dp)
                                            .weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label,
                                            tint = actColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.label,
                                            fontSize = 11.sp,
                                            color = actColor,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // "More" button to display secondary lists as an action sheet!
                                var showMenu by remember { mutableStateOf(false) }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showMenu = true }
                                        .padding(8.dp)
                                        .weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MenuOpen,
                                        contentDescription = "More Tools",
                                        tint = if (secondaryScreens.any { it.screen == currentScreen }) ElectricCyan else SubtleGray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "More",
                                        fontSize = 11.sp,
                                        color = if (secondaryScreens.any { it.screen == currentScreen }) ElectricCyan else SubtleGray,
                                        maxLines = 1
                                    )
                                }

                                if (showMenu) {
                                    AlertDialog(
                                        onDismissRequest = { showMenu = false },
                                        title = { Text("Menu & Utilities", fontWeight = FontWeight.Bold, color = ElectricCyan) },
                                        text = {
                                            LazyColumn(
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                items(secondaryScreens) { item ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(if (currentScreen == item.screen) ElectricCyan.copy(alpha = 0.2f) else Transparent)
                                                            .clickable {
                                                                viewModel.navigateTo(item.screen)
                                                                showMenu = false
                                                            }
                                                            .padding(14.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(item.selectedIcon, contentDescription = item.label, tint = ElectricCyan)
                                                        Spacer(modifier = Modifier.width(16.dp))
                                                        Text(item.label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            TextButton(onClick = { showMenu = false }) { Text("Close") }
                                        },
                                        containerColor = SpaceCardBg,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

val Transparent = Color.Transparent

data class NavItem(
    val screen: AppScreen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)


// 4. SCREEN COMPONENT: HOME TRANSLATION
@Composable
fun HomeTranslationScreen(viewModel: TranslationViewModel) {
    val srcLang by viewModel.sourceLang.collectAsStateWithLifecycle()
    val tgtLang by viewModel.targetLang.collectAsStateWithLifecycle()
    val txtInput by viewModel.sourceText.collectAsStateWithLifecycle()
    val isAuto by viewModel.isAutoDetect.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()
    val result by viewModel.translationResult.collectAsStateWithLifecycle()
    val isOnline by viewModel.useAiOnline.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Futuristic Dashboard Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Universe Translator",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = ElectricCyan
                )
                Text(
                    text = if (isOnline) "🟢 Online Gemini AI Active" else "🔵 Local Pack Active (Offline)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            
            // Speed settings shortcut
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.Settings) },
                modifier = Modifier.testTag("settings_shortcut")
            ) {
                Icon(Icons.Filled.Settings, "Config", tint = ElectricCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Selectors bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SpaceCardBg)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var srcMenuOpen by remember { mutableStateOf(false) }
            val srcLangItem = viewModel.supportedLanguages.find { it.code == srcLang }
            
            // Source Language Selector
            Button(
                onClick = { srcMenuOpen = true },
                colors = ButtonDefaults.buttonColors(containerColor = Transparent),
                modifier = Modifier
                    .weight(0.45f)
                    .testTag("source_lang_picker")
            ) {
                Text(
                    "${srcLangItem?.flag ?: "🇯🇵"} ${if (isAuto) "Auto (${srcLangItem?.name})" else srcLangItem?.name}",
                    color = GlowWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Swap Button
            IconButton(
                onClick = { viewModel.swapLanguages() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.1f))
                    .testTag("swap_languages_button")
            ) {
                Icon(Icons.Filled.SwapHoriz, "Swap", tint = ElectricCyan)
            }

            var tgtMenuOpen by remember { mutableStateOf(false) }
            val tgtLangItem = viewModel.supportedLanguages.find { it.code == tgtLang }

            // Target Language Selector
            Button(
                onClick = { tgtMenuOpen = true },
                colors = ButtonDefaults.buttonColors(containerColor = Transparent),
                modifier = Modifier
                    .weight(0.45f)
                    .testTag("target_lang_picker")
            ) {
                Text(
                    "${tgtLangItem?.flag ?: "🇪🇸"} ${tgtLangItem?.name}",
                    color = GlowWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Dropdown menus
            LanguageDropdownMenu(srcMenuOpen, { srcMenuOpen = false }) { selected ->
                viewModel.sourceLang.value = selected
                viewModel.isAutoDetect.value = false
            }
            LanguageDropdownMenu(tgtMenuOpen, { tgtMenuOpen = false }) { selected ->
                viewModel.targetLang.value = selected
            }
        }

        // Auto detect toggle row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Auto-detect source speech", style = MaterialTheme.typography.bodySmall, color = GlowWhite.copy(alpha = 0.8f))
            Switch(
                checked = isAuto,
                onCheckedChange = { viewModel.isAutoDetect.value = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ElectricCyan,
                    checkedTrackColor = ElectricCyan.copy(alpha = 0.4f)
                ),
                modifier = Modifier.scale(0.85f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text input field Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box {
                    if (txtInput.isEmpty()) {
                        Text(
                            "Enter speech or tap translate...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SubtleGray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    OutlinedTextField(
                        value = txtInput,
                        onValueChange = { viewModel.sourceText.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("translation_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Transparent,
                            unfocusedBorderColor = Transparent
                        ),
                        singleLine = false,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.translateText() })
                    )

                    // Clear button
                    if (txtInput.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.sourceText.value = "" },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(36.dp)
                        ) {
                            Icon(Icons.Filled.Close, "Clear", tint = SubtleGray)
                        }
                    }
                }

                // Input Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.speakText(txtInput, srcLang) },
                            enabled = txtInput.isNotEmpty(),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Filled.VolumeUp,
                                "Speak Source",
                                tint = if (txtInput.isNotEmpty()) ElectricCyan else SubtleGray
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.translateText() },
                        enabled = txtInput.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            disabledContainerColor = SubtleGray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("translate_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CosmicNavy, strokeWidth = 2.dp)
                        } else {
                            Text("TRANSLATE", fontWeight = FontWeight.Black, color = CosmicNavy)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Translation output and AI suggestions
        AnimatedVisibility(
            visible = result != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            val res = result
            if (res != null) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Main output cards
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SpaceCardBg.copy(alpha = 0.8f)),
                        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(ElectricCyan, NeonPurple))),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "TRANSLATION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                res.translation,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = GlowWhite
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action items
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { viewModel.speakText(res.translation, tgtLang) }) {
                                        Icon(Icons.Filled.VolumeUp, "Speak Result", tint = ElectricCyan)
                                    }
                                    
                                    // Match clipboard copy requirement
                                    IconButton(onClick = {
                                        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboardManager.setPrimaryClip(ClipData.newPlainText("translation", res.translation))
                                        Toast.makeText(context, "Translation copied!", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Filled.ContentCopy, "Copy", tint = ElectricCyan)
                                    }

                                    IconButton(onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, res.translation)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share translation"))
                                    }) {
                                        Icon(Icons.Filled.Share, "Share", tint = ElectricCyan)
                                    }
                                }

                                Button(
                                    onClick = {
                                        viewModel.toggleHistoryFavorite(
                                            TranslationHistory(
                                                id = 0,
                                                sourceText = txtInput,
                                                translatedText = res.translation,
                                                sourceLang = srcLang,
                                                targetLang = tgtLang
                                            )
                                        )
                                        Toast.makeText(context, "Saved to Favorites", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Transparent)
                                ) {
                                    Icon(Icons.Filled.Star, "Save Favorite", tint = ElectricCyan)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("FAVORITE", color = ElectricCyan)
                                }
                            }
                        }
                    }

                    // On Demand AI Enhancement Details
                    if (res.contextNotes != null || res.grammarImprovement != null || res.slangDetected != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SpaceCardBg.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AutoAwesome, "AI Notes", tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AI Insights & Context Check", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = ElectricCyan)
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                res.grammarImprovement?.let {
                                    AiInsightRow("💡 Grammar Tip", it)
                                }
                                res.slangDetected?.let {
                                    AiInsightRow("🌶️ Idiom/Slang Check", it)
                                }
                                res.contextNotes?.let {
                                    AiInsightRow("🗣️ Real-world Context", it)
                                }
                            }
                        }
                    }

                    // Related Phrase Suggestions
                    if (res.phraseSuggestions.isNotEmpty()) {
                        Text("Suggested Phrases", style = MaterialTheme.typography.titleSmall, color = ElectricCyan, modifier = Modifier.padding(horizontal = 4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(res.phraseSuggestions) { suggestion ->
                                Card(
                                    modifier = Modifier.clickable {
                                        viewModel.sourceText.value = suggestion
                                        viewModel.translateText()
                                    },
                                    colors = CardDefaults.cardColors(containerColor = SpaceCardBg),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = suggestion,
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 13.sp,
                                        color = GlowWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageDropdownMenu(open: Boolean, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val languages = listOf(
        LangItem("en", "English", "🇺🇸"),
        LangItem("ur", "Urdu", "🇵🇰"),
        LangItem("es", "Spanish", "🇪🇸"),
        LangItem("ar", "Arabic", "🇸🇦"),
        LangItem("fr", "French", "🇫🇷"),
        LangItem("de", "German", "🇩🇪"),
        LangItem("zh", "Chinese", "🇨🇳"),
        LangItem("hi", "Hindi", "🇮🇳"),
        LangItem("ps", "Pashto", "🇦🇫"),
        LangItem("tr", "Turkish", "🇹🇷"),
        LangItem("ru", "Russian", "🇷🇺"),
        LangItem("ja", "Japanese", "🇯🇵"),
        LangItem("ko", "Korean", "🇰🇷")
    )

    DropdownMenu(
        expanded = open,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(SpaceCardBg)
            .width(180.dp)
    ) {
        languages.forEach { lang ->
            DropdownMenuItem(
                text = { Text("${lang.flag} ${lang.name}", color = GlowWhite) },
                onClick = {
                    onSelect(lang.code)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun AiInsightRow(label: String, valText: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = ElectricCyan.copy(alpha = 0.8f))
        Text(valText, style = MaterialTheme.typography.bodyMedium, color = GlowWhite)
    }
}


// 5. SCREEN COMPONENT: VOICE TRANSLATION
@Composable
fun VoiceTranslationScreen(viewModel: TranslationViewModel) {
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val waveform by viewModel.voiceWaveform.collectAsStateWithLifecycle()
    val spoken by viewModel.spokenText.collectAsStateWithLifecycle()
    val result by viewModel.translationResult.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Voice Translation",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = ElectricCyan
            )
            Text(
                "Tap mic to start real-time speak lookups",
                style = MaterialTheme.typography.bodySmall,
                color = SubtleGray
            )
        }

        // Voice visualization bubble unit
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .background(SpaceCardBg)
                .drawBehind {
                    if (isRecording) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(ElectricCyan.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            radius = size.width * 0.9f
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                // Interactive dynamic voice frequency bar simulator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    waveform.forEach { height ->
                        val animHeight by animateDpAsState(
                            targetValue = height.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "WaveAnim"
                        )
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(animHeight)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(ElectricCyan, NeonPurple)))
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = { viewModel.toggleVoiceRecording() },
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                        .testTag("record_mic_button")
                ) {
                    Icon(
                        Icons.Filled.Mic,
                        "Record Microphone",
                        modifier = Modifier.size(44.dp),
                        tint = CosmicNavy
                    )
                }
            }
        }

        // Output dialogue view
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRecording) {
                Text(
                    "Listening... Speak now",
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            } else if (spoken.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SpaceCardBg,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Spoken:", fontSize = 12.sp, color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                        Text(spoken, fontSize = 16.sp, color = GlowWhite)
                        
                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = SubtleGray.copy(alpha = 0.15f))
                        
                        Text("Result:", fontSize = 12.sp, color = NeonPurple, fontWeight = FontWeight.SemiBold)
                        Text(result?.translation ?: "", fontSize = 20.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        IconButton(
                            onClick = { viewModel.speakText(result?.translation ?: "", viewModel.targetLang.value) },
                            modifier = Modifier.align(Alignment.End).size(36.dp)
                        ) {
                            Icon(Icons.Filled.VolumeUp, "Speak Response", tint = ElectricCyan)
                        }
                    }
                }
            }
        }

        if (isRecording) {
            Button(
                onClick = { viewModel.toggleVoiceRecording() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("stop_record_button")
            ) {
                Text("FINISH SPEAKING", color = GlowWhite)
            }
        } else {
            Spacer(modifier = Modifier.height(56.dp))
        }
    }
}


// 6. SCREEN COMPONENT: SPLIT SCREEN CONVERSATION MODE
@Composable
fun ConversationModeScreen(viewModel: TranslationViewModel) {
    val messages by viewModel.conversationHistory.collectAsStateWithLifecycle()
    val activeSide by viewModel.activeSpeaker.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val srcLang by viewModel.sourceLang.collectAsStateWithLifecycle()
    val tgtLang by viewModel.targetLang.collectAsStateWithLifecycle()

    val p1 = viewModel.supportedLanguages.find { it.code == srcLang }
    val p2 = viewModel.supportedLanguages.find { it.code == tgtLang }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // TOP Section: User B UI Interface (Upside Down for conversation balance!)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(SpaceCardBg.copy(alpha = 0.4f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { viewModel.clearConversation() }) {
                        Text("Reset Chat", color = SubtleGray)
                    }
                    Text(
                        "${p2?.flag ?: "🇪🇸"} Speaker B Language",
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                if (activeSide == 1 && isRecording) {
                    Text("SPEAKER B TALKING...", color = ElectricCyan, fontWeight = FontWeight.Black)
                } else {
                    Text(
                        if (messages.none { it.senderId == 1 }) "Speaker B reads responses directly here." else "Speaker B: " + messages.lastOrNull { it.senderId == 1 }?.text,
                        textAlign = TextAlign.Center,
                        color = GlowWhite
                    )
                }

                Button(
                    onClick = {
                        viewModel.activeSpeaker.value = 1
                        viewModel.toggleVoiceRecording(isConversation = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeSide == 1 && isRecording) NeonPurple else ElectricCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Mic, "Speaker B mic")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TALK [${p2?.code?.uppercase()}]")
                }
            }
        }

        Divider(color = ElectricCyan, thickness = 2.dp)

        // BOTTOM Section: User A UI Interface (Facing correctly!)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(SpaceCardBg.copy(alpha = 0.6f))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        viewModel.activeSpeaker.value = 0
                        viewModel.toggleVoiceRecording(isConversation = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeSide == 0 && isRecording) NeonPurple else ElectricCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Mic, "Speaker A mic")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TALK [${p1?.code?.uppercase()}]")
                }

                if (activeSide == 0 && isRecording) {
                    Text("SPEAKER A TALKING...", color = ElectricCyan, fontWeight = FontWeight.Black)
                } else {
                    Text(
                        if (messages.none { it.senderId == 0 }) "Speaker A starts conversation by typing / speaking." else "Speaker A: " + messages.lastOrNull { it.senderId == 0 }?.text,
                        textAlign = TextAlign.Center,
                        color = GlowWhite
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${p1?.flag ?: "🇺🇸"} Speaker A Language",
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                    TextButton(onClick = { viewModel.navigateTo(AppScreen.Home) }) {
                        Text("Exit Split Mode", color = SubtleGray)
                    }
                }
            }
        }
    }
}


// 7. SCREEN COMPONENT: CAMERA TRANSLATION SCREEN
@Composable
fun CameraTranslationScreen(viewModel: TranslationViewModel) {
    val flashState by viewModel.isFlashActive.collectAsStateWithLifecycle()
    val isTranslating by viewModel.isTranslating.collectAsStateWithLifecycle()
    val detectionResult by viewModel.cameraDetectionBox.collectAsStateWithLifecycle()
    val srcLang by viewModel.sourceLang.collectAsStateWithLifecycle()
    val tgtLang by viewModel.targetLang.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        permissionGranted = permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (permissionGranted) {
            CameraPreviewComponent(flashState)
        } else {
            // High Tech Simulated Scanner overlay when permissions aren't ready
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Drawing cybernetic gridlines helper
                        val columns = 8
                        val rows = 12
                        val gridColor = ElectricCyan.copy(alpha = 0.12f)
                        for (i in 1..columns) {
                            val x = (size.width / columns) * i
                            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        }
                        for (i in 1..rows) {
                            val y = (size.height / rows) * i
                            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text(
                        "CAMERA FEED INITIALIZED",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = ElectricCyan
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Point your device towards signs, books, labels, menus, or posters. Tap capture below to instant OCR translate.",
                        textAlign = TextAlign.Center,
                        color = GlowWhite
                    )
                }
            }
        }

        // Camera frame guides overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .border(2.dp, ElectricCyan, RoundedCornerShape(24.dp))
                    .drawBehind {
                        // Corner glowing notches
                    }
            )
        }

        // Live holographic Overlay translations
        detectionResult?.let { box ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardBg.copy(alpha = 0.85f)),
                    border = BorderStroke(2.dp, ElectricCyan),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ORIGINAL:", fontSize = 11.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                        Text(box.originalText, color = GlowWhite, fontWeight = FontWeight.SemiBold)
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = ElectricCyan.copy(alpha = 0.2f))
                        
                        Text("HOLOGRAPHIC OVERLAY TRANSLATION:", fontSize = 11.sp, color = NeonPurple, fontWeight = FontWeight.Bold)
                        Text(box.translatedText, fontSize = 18.sp, color = ElectricCyan, fontWeight = FontWeight.ExtraBold)
                    }
                }
                
                // Close button for overlay
                IconButton(
                    onClick = { viewModel.clearCameraDetection() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 130.dp, end = 16.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Filled.Close, "Reset overlay", tint = GlowWhite)
                }
            }
        }

        // Top command bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(Icons.Filled.ArrowBack, "Go Back", tint = GlowWhite)
            }

            Text(
                "AR Translation Mode",
                fontWeight = FontWeight.Bold,
                color = ElectricCyan,
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(
                onClick = { viewModel.isFlashActive.value = !flashState },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = if (flashState) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = "Flashlight Toggle",
                    tint = if (flashState) ElectricCyan else GlowWhite
                )
            }
        }

        // Bottom triggers layout
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo picker shortcut
                IconButton(
                    onClick = { viewModel.triggerCameraShot() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Filled.PhotoLibrary, "Photo Library", tint = GlowWhite)
                }

                // Snap OCR translation trigger button
                IconButton(
                    onClick = { viewModel.triggerCameraShot() },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(4.dp, ElectricCyan, CircleShape)
                        .background(Color.White)
                        .testTag("snap_camera_ocr")
                ) {
                    Icon(Icons.Filled.Camera, "Capture target text", modifier = Modifier.size(36.dp), tint = CosmicNavy)
                }

                // Close button target locator
                IconButton(
                    onClick = { viewModel.clearCameraDetection() },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Filled.Refresh, "Re-examine", tint = GlowWhite)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            LanguageSelectionTicker(viewModel)
        }
    }
}

@Composable
fun CameraPreviewComponent(flashEnabled: Boolean) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            ctx as androidx.lifecycle.LifecycleOwner,
                            cameraSelector,
                            preview
                        )
                        camera.cameraControl.enableTorch(flashEnabled)
                    } catch (e: Exception) {
                        Log.e("CameraPreviewComponent", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun LanguageSelectionTicker(viewModel: TranslationViewModel) {
    val srcLang by viewModel.sourceLang.collectAsStateWithLifecycle()
    val tgtLang by viewModel.targetLang.collectAsStateWithLifecycle()

    val wordCode1 = srcLang.uppercase()
    val wordCode2 = tgtLang.uppercase()

    Surface(
        color = Color.Black.copy(alpha = 0.7f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$wordCode1", color = ElectricCyan, fontWeight = FontWeight.Bold)
            Icon(Icons.Filled.ArrowForward, "Translation Direction", modifier = Modifier.padding(horizontal = 8.dp).size(16.dp), tint = GlowWhite)
            Text("$wordCode2", color = ElectricCyan, fontWeight = FontWeight.Bold)
        }
    }
}


// 8. SCREEN COMPONENT: LIVE SUBTITLE OVERLAY
@Composable
fun LiveSubtitleScreen(viewModel: TranslationViewModel) {
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val originalText by viewModel.subSpokenText.collectAsStateWithLifecycle()
    val translatedText by viewModel.subTranslatedText.collectAsStateWithLifecycle()
    val tSize by viewModel.subtitleSize.collectAsStateWithLifecycle()
    val tAlpha by viewModel.subtitleTransparency.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Speech Live Subtitles",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = ElectricCyan
            )
            Text(
                "Subtitles overlay during meetings and travel",
                style = MaterialTheme.typography.bodySmall,
                color = SubtleGray
            )
        }

        // Subtitle glass display board
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 32.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceCardBg.copy(alpha = tAlpha)),
            border = BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isRecording) {
                    Text(
                        originalText,
                        fontSize = tSize.sp,
                        color = SubtleGray,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Divider(color = ElectricCyan.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 16.dp))
                    
                    Text(
                        translatedText,
                        fontSize = (tSize + 4).sp,
                        color = ElectricCyan,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Icon(
                        Icons.Filled.Hearing,
                        "Listen",
                        tint = SubtitleIconColor(tAlpha),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Tap Start stream below. Subtitles will transcribe and map instantly using high-speed offline parsing.",
                        textAlign = TextAlign.Center,
                        color = SubtleGray
                    )
                }
            }
        }

        // Transparency and sizing custom bar settings
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Font size: ${tSize}sp", fontSize = 12.sp, color = GlowWhite)
                    Text("Alpha level: ${"%.1f".format(tAlpha)}", fontSize = 12.sp, color = GlowWhite)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Slider(
                        value = tSize.toFloat(),
                        onValueChange = { viewModel.subtitleSize.value = it.toInt() },
                        valueRange = 14f..28f,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )
                    Slider(
                        value = tAlpha,
                        onValueChange = { viewModel.subtitleTransparency.value = it },
                        valueRange = 0.3f..1.0f,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
                    )
                }
            }
        }

        // Buttons
        Button(
            onClick = { viewModel.toggleLiveSubtitles() },
            colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) NeonPurple else ElectricCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_subtitles_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Filled.StopCircle else Icons.Filled.PlayCircleFilled,
                contentDescription = if (isRecording) "Stop" else "Start",
                tint = if (isRecording) GlowWhite else CosmicNavy
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isRecording) "STOP SUBTITLE TRANSLATION" else "START SUBTITLE TRANSLATION",
                fontWeight = FontWeight.Bold,
                color = if (isRecording) GlowWhite else CosmicNavy
            )
        }
    }
}

@Composable
fun SubtitleIconColor(alpha: Float): Color {
    return if (alpha > 0.5f) ElectricCyan else NeonPurple
}


// 9. SCREEN COMPONENT: DOWNLOADABLE LANGUAGE PACKS
@Composable
fun DownloadPacksScreen(viewModel: TranslationViewModel) {
    val packs by viewModel.languagePacks.collectAsStateWithLifecycle()
    val storageUsage = packs.filter { it.isDownloaded }.sumOf { it.sizeMb }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            "Language Packages",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = ElectricCyan
        )
        Text(
            "Manage storage & direct offline translations",
            style = MaterialTheme.typography.bodySmall,
            color = SubtleGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Disk Meter Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpaceCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Offline footprint", fontWeight = FontWeight.SemiBold, color = GlowWhite)
                    Text("${"%.1f".format(storageUsage)} MB used", color = ElectricCyan, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = (storageUsage.toFloat() / 500f),
                    color = ElectricCyan,
                    trackColor = SubtleGray.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cap limit: 500.0 MB", fontSize = 11.sp, color = SubtleGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Packs List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(packs) { pack ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SpaceCardBg.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(pack.languageName, fontWeight = FontWeight.Bold, color = GlowWhite)
                            Text("${pack.sizeMb} MB package", style = MaterialTheme.typography.labelMedium, color = SubtleGray)
                        }

                        if (pack.isDownloading) {
                            Column(horizontalAlignment = Alignment.End) {
                                CircularProgressIndicator(
                                    progress = pack.progress.toFloat() / 100f,
                                    modifier = Modifier.size(24.dp),
                                    color = ElectricCyan
                                )
                                Text("${pack.progress}%", fontSize = 11.sp, color = ElectricCyan)
                            }
                        } else if (pack.isDownloaded) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Downloaded", fontSize = 12.sp, color = BrightGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.simulateDeletePack(pack.languageCode) }) {
                                    Icon(Icons.Filled.Delete, "Delete Pack", tint = Color.Red.copy(alpha = 0.8f))
                                }
                            }
                        } else {
                            IconButton(
                                onClick = { viewModel.simulateDownloadPack(pack.languageCode) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                            ) {
                                Icon(Icons.Filled.Download, "Download Pack", tint = ElectricCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}


// 10. SCREEN COMPONENT: TRAVEL MODE / QUICK PHRASES
@Composable
fun TravelModeScreen(viewModel: TranslationViewModel) {
    val phrases by viewModel.travelPhrases.collectAsStateWithLifecycle()
    val activeCategory by viewModel.selectedTravelCategory.collectAsStateWithLifecycle()
    val targetLang by viewModel.targetLang.collectAsStateWithLifecycle()

    val categories = listOf("Hotel", "Airport", "Taxi", "Restaurant", "Shopping", "Medical")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            "Travel Phrase Dictionary",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = ElectricCyan
        )
        Text(
            "Tap any phrase to hear natural translated pronunciations",
            style = MaterialTheme.typography.bodySmall,
            color = SubtleGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Category Tabs
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            items(categories) { cat ->
                val isActive = cat == activeCategory
                Card(
                    modifier = Modifier
                        .clickable { viewModel.selectedTravelCategory.value = cat },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) ElectricCyan else SpaceCardBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isActive) CosmicNavy else GlowWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // Phrases listing
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (phrases.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.TravelExplore, "Vacant", tint = SubtleGray, modifier = Modifier.size(52.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No pre-populated dictionary phrases available for the selected target language pack.",
                            color = SubtleGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(phrases) { phrase ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.speakText(phrase.translatedText, targetLang) },
                        colors = CardDefaults.cardColors(containerColor = SpaceCardBg.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(phrase.englishText, fontWeight = FontWeight.Bold, color = GlowWhite)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(phrase.translatedText, color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Phonetics: " + phrase.phoneticText, style = MaterialTheme.typography.labelSmall, color = SubtleGray)
                            }
                            IconButton(onClick = { viewModel.speakText(phrase.translatedText, targetLang) }) {
                                Icon(Icons.Filled.VolumeUp, "Listen", tint = ElectricCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}


// 11. SCREEN COMPONENT: HISTORY SCREEN
@Composable
fun HistoryScreen(viewModel: TranslationViewModel) {
    val history by viewModel.historyList.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Translation History",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = ElectricCyan
                )
                Text("Stored securely locally offline on your device", style = MaterialTheme.typography.bodySmall, color = SubtleGray)
            }

            TextButton(
                onClick = {
                    viewModel.clearAllHistory()
                    Toast.makeText(context, "History cleared!", Toast.LENGTH_SHORT).show()
                },
                enabled = history.isNotEmpty()
            ) {
                Text("CLEAR ALL", color = if (history.isNotEmpty()) Color.Red else SubtleGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (history.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.History, "Empty history", tint = SubtleGray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No recent log items recorded yet.", color = SubtleGray)
                    }
                }
            } else {
                items(history) { item ->
                    HistoryListItem(item, { viewModel.toggleHistoryFavorite(item) }, { viewModel.deleteHistory(item) })
                }
            }
        }
    }
}

@Composable
fun HistoryListItem(item: TranslationHistory, onFavToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SpaceCardBg.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val indicatorIcon = when {
                        item.isVoice -> Icons.Filled.Mic
                        item.isCamera -> Icons.Filled.CameraAlt
                        else -> Icons.Filled.Description
                    }
                    Icon(indicatorIcon, "Source type", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${item.sourceLang.uppercase()} → ${item.targetLang.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SubtleGray
                    )
                }
                
                Row {
                    IconButton(onClick = onFavToggle, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                            contentDescription = "Save Favorite",
                            tint = if (item.isFavorite) ElectricCyan else SubtleGray
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, "Delete History", tint = SubtleGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(item.sourceText, color = GlowWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(item.translatedText, color = ElectricCyan, fontWeight = FontWeight.Bold)
            
            item.contextNotes?.let { notes ->
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = SubtleGray.copy(alpha = 0.1f))
                Text(notes, style = MaterialTheme.typography.bodySmall, color = SubtleGray)
            }
        }
    }
}


// 12. SCREEN COMPONENT: FAVORITES SCREEN
@Composable
fun FavoritesScreen(viewModel: TranslationViewModel) {
    val favorites by viewModel.favoritesList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            "Saved Translations",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = ElectricCyan
        )
        Text("Your pinned fast phrase list", style = MaterialTheme.typography.bodySmall, color = SubtleGray)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (favorites.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Star, "No favorites", tint = SubtleGray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No translations starred yet.", color = SubtleGray)
                    }
                }
            } else {
                items(favorites) { item ->
                    HistoryListItem(item, { viewModel.toggleHistoryFavorite(item) }, { viewModel.deleteHistory(item) })
                }
            }
        }
    }
}


// 13. SCREEN COMPONENT: APP SETTINGS
@Composable
fun SettingsScreen(viewModel: TranslationViewModel) {
    val isDark by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isOnline by viewModel.useAiOnline.collectAsStateWithLifecycle()
    val talkSpeed by viewModel.speechSpeed.collectAsStateWithLifecycle()
    val selectedVoice by viewModel.voiceType.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Translation Settings",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = ElectricCyan
        )
        Text("Set default engines, pitch, and API variables", style = MaterialTheme.typography.bodySmall, color = SubtleGray)

        Spacer(modifier = Modifier.height(24.dp))

        Text("TRANSLATION ENGINE", style = MaterialTheme.typography.labelSmall, color = ElectricCyan, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpaceCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(0.75f)) {
                        Text("Hybrid Online Gemini Mode", fontWeight = FontWeight.Bold, color = GlowWhite)
                        Text("Falls back to offline pack on failures.", style = MaterialTheme.typography.bodySmall, color = SubtleGray)
                    }
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { viewModel.useAiOnline.value = it },
                        modifier = Modifier.testTag("ai_hybrid_switch"),
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = ElectricCyan.copy(alpha = 0.4f))
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = SubtleGray.copy(alpha = 0.1f))

                // Mode status description
                Text(
                    text = if (isOnline) "⚡ CURRENT ACTIVE: Advanced Gemini Translation, Slang/Grammar analysis and Phrase categories enabled."
                           else "🧊 CURRENT ACTIVE: Direct 100% Offline Database and local lookup speed pack active.",
                    fontSize = 11.sp,
                    color = ElectricCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("TEXT TO SPEECH CONFIGURATION", style = MaterialTheme.typography.labelSmall, color = ElectricCyan, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpaceCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Sizing sliders
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speaking Velocity", fontWeight = FontWeight.SemiBold, color = GlowWhite)
                        Text("${"%.1f".format(talkSpeed)}x", color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = talkSpeed,
                        onValueChange = { viewModel.speechSpeed.value = it },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                    )
                }

                Column {
                    Text("Voice Profile", fontWeight = FontWeight.SemiBold, color = GlowWhite)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("AI Professional Female", "AI Natural Male").forEach { tag ->
                            val isChosen = tag == selectedVoice
                            ElevatedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.voiceType.value = tag },
                                colors = CardDefaults.cardColors(containerColor = if (isChosen) ElectricCyan else SpaceCardBg.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    tag,
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) CosmicNavy else GlowWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("INTERFACE PREFERENCES", style = MaterialTheme.typography.labelSmall, color = ElectricCyan, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpaceCardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Mode", fontWeight = FontWeight.Bold, color = GlowWhite)
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.isDarkTheme.value = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan, checkedTrackColor = ElectricCyan.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}


// 14. SCREEN COMPONENT: PROFILE
@Composable
fun ProfileScreen(viewModel: TranslationViewModel) {
    val history by viewModel.historyList.collectAsStateWithLifecycle()
    val favorites by viewModel.favoritesList.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Avatar Glowing visual
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SpaceCardBg)
                .border(2.dp, ElectricCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, "User profile icon", modifier = Modifier.size(52.dp), tint = ElectricCyan)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Multilingual Explorer", style = MaterialTheme.typography.titleMedium, color = GlowWhite, fontWeight = FontWeight.Bold)
        
        // Subscription Tier Badge
        Card(
            colors = CardDefaults.cardColors(containerColor = NeonPurple),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Text(
                "GLOBAL+ SUBSCRIPTION",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GlowWhite
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Direct Stats numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStatBlock("Translated", "${history.size}", "items")
            ProfileStatBlock("Saved Favorites", "${favorites.size}", "phrases")
            ProfileStatBlock("Active Streak", "12", "days")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SpaceCardBg.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Subscribed Tier Privileges", style = MaterialTheme.typography.titleSmall, color = ElectricCyan, fontWeight = FontWeight.Bold)
                PrivilegeRow("🌟 Unlimited Gemini API translations enabled.")
                PrivilegeRow("🛩️ Offline Pack Language packs database available on-device.")
                PrivilegeRow("📸 Advanced augmented AR OCR vision OCR module active.")
                PrivilegeRow("🎧 Smart Bluetooth earbud dual translations.")
            }
        }
    }
}

@Composable
fun ProfileStatBlock(label: String, valStr: String, suffix: String) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(containerColor = SpaceCardBg)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = SubtleGray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(4.dp))
            Text(valStr, style = MaterialTheme.typography.titleLarge, color = ElectricCyan, fontWeight = FontWeight.ExtraBold)
            Text(suffix, fontSize = 10.sp, color = SubtleGray)
        }
    }
}

@Composable
fun PrivilegeRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Check, "Privilege", tint = BrightGreen, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = GlowWhite)
    }
}
