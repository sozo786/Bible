package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.BibleViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class Screen {
    Splash,
    Onboarding,
    Home,
    Reading,
    Search,
    Bookmarks,
    Settings
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationContainer(viewModel: BibleViewModel) {
    val isInitialized by viewModel.isInitialized.collectAsState()
    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    val haptic = LocalHapticFeedback.current

    MyApplicationTheme(darkTheme = isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                Screen.Splash -> {
                    SplashScreen(
                        onSplashFinished = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentScreen = if (isOnboardingCompleted) Screen.Home else Screen.Onboarding
                        }
                    )
                }
                Screen.Onboarding -> {
                    OnboardingScreen(
                        onOnboardingComplete = {
                            viewModel.completeOnboarding()
                            currentScreen = Screen.Home
                        }
                    )
                }
                else -> {
                    // Core app layout with custom navigation bottom bar
                    Scaffold(
                        bottomBar = {
                            CustomBottomNavigation(
                                currentScreen = currentScreen,
                                onNavigate = { screen ->
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    currentScreen = screen
                                }
                            )
                        },
                        contentWindowInsets = WindowInsets.navigationBars
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            AnimatedContent(
                                targetState = currentScreen,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                                },
                                label = "ScreenTransition"
                            ) { target ->
                                when (target) {
                                    Screen.Home -> HomeScreen(
                                        viewModel = viewModel,
                                        onOpenBook = { currentScreen = Screen.Reading },
                                        onNavigateToSearch = { currentScreen = Screen.Search }
                                    )
                                    Screen.Reading -> ReadingScreen(viewModel = viewModel)
                                    Screen.Search -> SearchScreen(viewModel = viewModel)
                                    Screen.Bookmarks -> BookmarksScreen(
                                        viewModel = viewModel,
                                        onOpenBookmark = { currentScreen = Screen.Reading }
                                    )
                                    Screen.Settings -> SettingsScreen(viewModel = viewModel)
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.4f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "LogoAlpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2200) // 2.2 seconds splash delay
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF074F34))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // SOZO Logo Icon (Emerald + Gold Cross/Minar Hybrid Design)
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .drawBehind {
                        // Drawing an elegant golden glowing background orb
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x33D4AF37), Color.Transparent),
                                center = center,
                                radius = size.minDimension / 1.5f
                            )
                        )
                    }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                // Cross / Minar Hybrid geometry drawn programmatically
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Minar crescent/arch dome
                    val domePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.5f, h * 0.1f)
                        cubicTo(w * 0.25f, h * 0.25f, w * 0.35f, h * 0.6f, w * 0.35f, h * 0.85f)
                        lineTo(w * 0.65f, h * 0.85f)
                        cubicTo(w * 0.65f, h * 0.6f, w * 0.75f, h * 0.25f, w * 0.5f, h * 0.1f)
                        close()
                    }
                    drawPath(domePath, color = EmeraldPrimary)

                    // Central Holy Cross highlighting the salvation (SOZO)
                    // Vertical beam
                    drawRoundRect(
                        color = GoldSecondary,
                        topLeft = Offset(w * 0.46f, h * 0.22f),
                        size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.55f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    // Horizontal beam
                    drawRoundRect(
                        color = GoldSecondary,
                        topLeft = Offset(w * 0.28f, h * 0.38f),
                        size = androidx.compose.ui.geometry.Size(w * 0.44f, h * 0.08f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Noor-e-Kalaam",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = GoldSecondary,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Presented by SOZO",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = GoldSecondary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Noor-e-Kalaam, Har Dil Ke Naam",
                style = getUrduTextStyle(18f).copy(
                    color = DarkTextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    val titles = listOf(
        "Kalaam-e-Muqaddas",
        "Urdu & English Together",
        "Apni Ayat Mehfooz Karen"
    )
    val descsUrdu = listOf(
        "توریت، زبور اور انجیلِ مقدس کو اب پڑھیں نہایت ہی خوبصورت اردو نستعلیق رسم الخط میں۔",
        "اردو جیو ورژن اور انگریزی کنگ جیمز ورژن (KJV) کے تقابلی مطالعہ کی مکمل سہولت۔",
        "اپنی پسندیدہ آیات کو محفوظ کریں، شیئر کریں اور کسی بھی وقت بنا انٹرنیٹ کے دوبارہ پڑھیں۔"
    )
    val descsEnglish = listOf(
        "Explore the Holy Scriptures (Torah, Psalms, and Gospel) offline in beautiful, high-contrast layouts.",
        "Compare scriptures side-by-side using English KJV and Urdu Geo translation modes.",
        "Bookmark sacred verses, copy scriptures, share with loved ones, and access history anytime offline."
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF141F1A))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top branding/skip row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NOOR-E-KALAAM",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = GoldSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOnboardingComplete()
                    }
                ) {
                    Text("Skip", color = DarkTextSecondary)
                }
            }

            // Middle illustrative panel
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Vector decorative mandala background
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color(0x0A0B7A52), shape = CircleShape)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (currentPage) {
                            0 -> Icons.Outlined.MenuBook
                            1 -> Icons.Outlined.Translate
                            else -> Icons.Outlined.BookmarkBorder
                        },
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = GoldSecondary
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = titles[currentPage],
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = descsUrdu[currentPage],
                    style = getUrduTextStyle(18f).copy(
                        color = DarkTextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = descsEnglish[currentPage],
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DarkTextSecondary,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Bottom controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (currentPage == index) 12.dp else 8.dp)
                                .background(
                                    color = if (currentPage == index) GoldSecondary else DarkTextSecondary.copy(
                                        alpha = 0.4f
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (currentPage < 2) {
                            currentPage++
                        } else {
                            onOnboardingComplete()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("onboarding_next_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (currentPage == 2) "Shuru Karen" else "Aagay Barhein",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 3. HOME SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BibleViewModel,
    onOpenBook: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val lastBookIdx by viewModel.lastReadBookIndex.collectAsState()
    val lastChNum by viewModel.lastReadChapterNum.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val vod by viewModel.verseOfTheDay.collectAsState()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedSection by remember { mutableStateOf<BibleSection?>(null) }
    var refreshState by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Filter books based on active category
    val filteredBooks = if (selectedSection == null) {
        BibleData.books
    } else {
        BibleData.books.filter { it.section == selectedSection }
    }

    // Scrollable dashboard with premium layout
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        // Hero Brand Bar
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NOOR-E-KALAAM",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = if (isDark) GoldSecondary else EmeraldPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Noor-e-Kalaam, Har Dil Ke Naam",
                        style = getUrduTextStyle(14f).copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Header icon
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.triggerVerseNotification()
                        Toast.makeText(context, "Bismillah - Verse of the Day triggered!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .background(
                            if (isDark) Color(0x11FFFFFF) else Color(0x0A000000),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = "Trigger Notification",
                        tint = GoldSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tap-to-Search Glassmorphism Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (isDark) Color(0x22FFFFFF) else Color(0x11000000),
                        RoundedCornerShape(16.dp)
                    )
                    .background(if (isDark) Color(0x0AFFFFFF) else Color(0x05000000))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNavigateToSearch()
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GoldSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search Kalaam...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Continue Reading Card
        item {
            val lastBook = BibleData.getBookByIndex(lastBookIdx) ?: BibleData.books[0]
            Card(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.navigateToChapter(lastBookIdx, lastChNum)
                    onOpenBook()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("continue_reading_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF141A22) else Color(0xFFFFFFFF)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            // Subtle left green emerald border to show premium identity
                            drawLine(
                                color = EmeraldPrimary,
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 14f
                            )
                        }
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONTINUE READING",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${lastBook.urduName} (پیدائش) - باب ${lastChNum}",
                            style = getUrduTextStyle(20f).copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Last read: ${lastBook.englishName} Chapter ${lastChNum}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Verse of the Day Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1D1B16) else Color(0xFFFFFDF5)
                ),
                border = BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AAYAT OF THE DAY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GoldSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = GoldSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = vod.textUrdu,
                        style = getUrduTextStyle(18f).copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = vod.textEnglish,
                        style = getEnglishTextStyle(15f).copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Left
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${vod.refEnglish} | ${vod.refUrdu}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GoldSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    val shareText = "${vod.textUrdu} (${vod.refUrdu})\n\n${vod.textEnglish} (${vod.refEnglish})\n\nShared via Noor-e-Kalaam App"
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Verse"))
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = GoldSecondary)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section Filter Pills (Tawreet, Zaboor, Injeel)
        item {
            Text(
                text = "Kalaam Sections",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Pill
                FilterChip(
                    selected = selectedSection == null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedSection = null
                    },
                    label = { Text("Sab (All)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldPrimary,
                        selectedLabelColor = Color.White
                    )
                )

                BibleSection.values().forEach { section ->
                    FilterChip(
                        selected = selectedSection == section,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedSection = section
                        },
                        label = { Text(section.displayNameUrdu) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Grid listing of books
        item {
            Text(
                text = "Kalaam Books (${filteredBooks.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Display book list dynamically
        // Since we are inside LazyColumn, we can list items or use FlowRow/Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredBooks.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { book ->
                            Box(modifier = Modifier.weight(1f)) {
                                BookGridItem(
                                    book = book,
                                    isDark = isDark,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.navigateToChapter(book.index, 1)
                                        onOpenBook()
                                    }
                                )
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookGridItem(book: BookMetadata, isDark: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .testTag("book_card_${book.index}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1C2128) else Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Elegant visual index count badge
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = book.index.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                }

                // Section specific indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = when (book.section) {
                                BibleSection.TAWREET -> Color(0xFFE57373)
                                BibleSection.ZABOOR -> GoldSecondary
                                BibleSection.ANBIYA -> Color(0xFF64B5F6)
                                BibleSection.INJEEL -> EmeraldPrimary
                            },
                            shape = CircleShape
                        )
                )
            }

            Column {
                Text(
                    text = book.urduName,
                    style = getUrduTextStyle(18f).copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.Left
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = book.englishName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                    
                    Text(
                        text = "${book.chaptersCount} Ch",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = GoldSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

// 4. READING SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(viewModel: BibleViewModel) {
    val currentBookIdx by viewModel.currentBookIndex.collectAsState()
    val currentChNum by viewModel.currentChapterNum.collectAsState()
    val verses by viewModel.currentVerses.collectAsState()
    val translationMode by viewModel.translationMode.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()

    val currentBook = BibleData.getBookByIndex(currentBookIdx) ?: BibleData.books[0]

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // State for the verse bottom sheet
    var selectedVerseForMenu by remember { mutableStateOf<Verse?>(null) }
    var isSheetOpen by remember { mutableStateOf(false) }

    // Swipe gestures to navigation
    var swipeOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (swipeOffset > 150) {
                            // Swipe Right -> Prev Chapter
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.prevChapter()
                        } else if (swipeOffset < -150) {
                            // Swipe Left -> Next Chapter
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.nextChapter()
                        }
                        swipeOffset = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        swipeOffset += dragAmount.x
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High Fidelity Top App Bar
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${currentBook.urduName} (باب ${currentChNum})",
                            style = getUrduTextStyle(18f).copy(fontWeight = FontWeight.Bold, color = GoldSecondary)
                        )
                        Text(
                            text = "${currentBook.englishName} Chapter ${currentChNum}",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.prevChapter()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Prev Chapter")
                    }
                },
                actions = {
                    // Quick Translation Mode Toggle Button
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val nextMode = when (translationMode) {
                                "both" -> "urdu"
                                "urdu" -> "english"
                                else -> "both"
                            }
                            viewModel.updateTranslationMode(nextMode)
                            Toast.makeText(context, "Translation: ${nextMode.uppercase()}", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Toggle Translation",
                            tint = EmeraldPrimary
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.nextChapter()
                    }) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Next Chapter")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )

            // Chapter list of verses
            if (verses.isEmpty()) {
                // Shimmer or placeholder
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(verses) { verse ->
                        // State if this verse is bookmarked
                        val isBookmarked by viewModel.isBookmarkedFlow(currentBookIdx, currentChNum, verse.number)
                            .collectAsState(initial = false)

                        VerseItemRow(
                            verse = verse,
                            fontSize = fontSize,
                            translationMode = translationMode,
                            isBookmarked = isBookmarked,
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedVerseForMenu = verse
                                isSheetOpen = true
                            },
                            onBookmarkToggle = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleBookmark(
                                    currentBookIdx, currentChNum, verse.number,
                                    verse.textUrdu, verse.textEnglish
                                )
                                if (!isBookmarked) {
                                    Toast.makeText(context, "بسم اللہ الرحمن الرحیم\nآیت محفوظ ہو گئی!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Swipe hint indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .background(Color(0x7F000000), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Swipe left/right to change chapters",
                color = Color.White,
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Custom Bottom Sheet for Verse Actions
        if (isSheetOpen && selectedVerseForMenu != null) {
            val verse = selectedVerseForMenu!!
            val isBookmarked by viewModel.isBookmarkedFlow(currentBookIdx, currentChNum, verse.number)
                .collectAsState(initial = false)

            ModalBottomSheet(
                onDismissRequest = { isSheetOpen = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "${currentBook.urduName} ${currentChNum}:${verse.number} | ${currentBook.englishName} ${currentChNum}:${verse.number}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = GoldSecondary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Divider(modifier = Modifier.padding(bottom = 16.dp))

                    // Copy action
                    ListItem(
                        headlineContent = { Text("Copy Scripture") },
                        leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = EmeraldPrimary) },
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val clipText = "${verse.textUrdu} (${currentBook.urduName} ${currentChNum}:${verse.number})\n\n${verse.textEnglish} (${currentBook.englishName} ${currentChNum}:${verse.number})"
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("Scripture", clipText))
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            isSheetOpen = false
                        }
                    )

                    // Share action
                    ListItem(
                        headlineContent = { Text("Share Scripture") },
                        leadingContent = { Icon(Icons.Default.Share, contentDescription = null, tint = EmeraldPrimary) },
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val shareText = "${verse.textUrdu} (${currentBook.urduName} ${currentChNum}:${verse.number})\n\n${verse.textEnglish} (${currentBook.englishName} ${currentChNum}:${verse.number})\n\nShared via Noor-e-Kalaam App"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share scripture"))
                            isSheetOpen = false
                        }
                    )

                    // Bookmark action
                    ListItem(
                        headlineContent = { Text(if (isBookmarked) "Remove Bookmark" else "Save Bookmark") },
                        leadingContent = {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                tint = GoldSecondary
                            )
                        },
                        modifier = Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleBookmark(
                                currentBookIdx, currentChNum, verse.number,
                                verse.textUrdu, verse.textEnglish
                            )
                            if (!isBookmarked) {
                                Toast.makeText(context, "بسم اللہ الرحمن الرحیم\nآیت محفوظ ہو گئی!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Removed Bookmark", Toast.LENGTH_SHORT).show()
                            }
                            isSheetOpen = false
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerseItemRow(
    verse: Verse,
    fontSize: Float,
    translationMode: String,
    isBookmarked: Boolean,
    onLongPress: () -> Unit,
    onBookmarkToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = onLongPress,
                onClick = {}
            )
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Verse Number in Golden Circle Outline
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(1.dp, GoldSecondary, CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = verse.number.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoldSecondary
            )
        }

        // Verse Texts (Urdu and English with responsive layout sizes)
        Column(modifier = Modifier.weight(1f)) {
            if (translationMode == "both" || translationMode == "urdu") {
                Text(
                    text = verse.textUrdu,
                    style = getUrduTextStyle(fontSize).copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right,
                        textDirection = TextDirection.Rtl
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (translationMode == "both") {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (translationMode == "both" || translationMode == "english") {
                Text(
                    text = verse.textEnglish,
                    style = getEnglishTextStyle(fontSize - 2f).copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Left
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Quick Bookmark Tap Button
        IconButton(
            onClick = onBookmarkToggle,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = "Bookmark",
                tint = if (isBookmarked) GoldSecondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// 5. SEARCH SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: BibleViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    viewModel.performSearch(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                placeholder = { Text("Search scriptures (e.g., God, آسمان)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldSecondary) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.performSearch("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        if (isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else if (results.isEmpty() && query.isNotEmpty()) {
            // Search Empty State
            EmptyStateLayout(
                imageVector = Icons.Outlined.SearchOff,
                title = "Koi Natija Nahi Mila",
                descUrdu = "آپ کی تلاش کردہ آیت یا لفظ مل نہ سکا۔ برائے مہربانی کوئی اور لفظ تلاش کریں۔",
                descEnglish = "No scriptures match your search query. Please try searching for a different keyword."
            )
        } else if (results.isEmpty()) {
            // Unsearched initial state
            EmptyStateLayout(
                imageVector = Icons.Outlined.Lightbulb,
                title = "Noor ko Talash Karen",
                descUrdu = "کلامِ پاک میں برکات اور سچائی کو تلاش کریں۔ یہاں اردو یا انگریزی میں لکھ کر تلاش کریں۔",
                descEnglish = "Search and discover wisdom in the sacred scriptures offline instantly."
            )
        } else {
            // Results list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Results found: ${results.size}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(results) { res ->
                    Card(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.navigateToChapter(res.bookIndex, res.chapterNumber)
                            // Note: Main navigation switches automatically from containers
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1C2128) else Color(0xFFFFFFFF)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${res.bookUrduName} ${res.chapterNumber}:${res.verseNumber}",
                                    style = getUrduTextStyle(16f).copy(color = GoldSecondary, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${res.bookEnglishName} ${res.chapterNumber}:${res.verseNumber}",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = res.textUrdu,
                                style = getUrduTextStyle(16f).copy(textAlign = TextAlign.Right),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = res.textEnglish,
                                style = getEnglishTextStyle(14f).copy(textAlign = TextAlign.Left),
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

// 6. BOOKMARKS SCREEN
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarksScreen(viewModel: BibleViewModel, onOpenBookmark: () -> Unit) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    if (bookmarks.isEmpty()) {
        EmptyStateLayout(
            imageVector = Icons.Outlined.BookmarkBorder,
            title = "Abhi koi ayaat mehfooz nahi",
            descUrdu = "کلام پڑھتے ہوئے کسی بھی آیت کو دبا کر رکھیں اور اسے اپنے لیے ہمیشہ کے لیے محفوظ کریں۔",
            descEnglish = "No verses saved yet. Long press any verse while reading to save it here."
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Scriptures (${bookmarks.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookmarks, key = { it.id }) { bookmark ->
                    // High-Fidelity bookmark item supporting Swipe gestures or delete buttons
                    Card(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.navigateToChapter(bookmark.bookIndex, bookmark.chapterNumber)
                            onOpenBookmark()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement()
                            .testTag("bookmark_item_${bookmark.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1C2128) else Color(0xFFFFFFFF)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${bookmark.bookUrduName} ${bookmark.chapterNumber}:${bookmark.verseNumber}",
                                        style = getUrduTextStyle(16f).copy(color = GoldSecondary, fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${bookmark.bookEnglishName} ${bookmark.chapterNumber}:${bookmark.verseNumber}",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.deleteBookmark(bookmark.id)
                                        Toast.makeText(context, "Deleted bookmark", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Bookmark",
                                        tint = Color(0xFFE57373)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = bookmark.textUrdu,
                                style = getUrduTextStyle(16f).copy(textAlign = TextAlign.Right),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = bookmark.textEnglish,
                                style = getEnglishTextStyle(14f).copy(textAlign = TextAlign.Left),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

// 7. SETTINGS SCREEN
@Composable
fun SettingsScreen(viewModel: BibleViewModel) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val translationMode by viewModel.translationMode.collectAsState()

    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Application Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Dark/Light Theme Selector Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1C2128) else Color(0xFFFFFFFF)
                )
            ) {
                ListItem(
                    headlineContent = { Text("Masihi Premium Dark Theme") },
                    supportingContent = { Text("Switch between light background and eye-friendly deep dark modes") },
                    leadingContent = {
                        Icon(
                            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = GoldSecondary
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = isDark,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.toggleDarkMode()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldPrimary
                            ),
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }
                )
            }
        }

        // Font Size Adjuster Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1C2128) else Color(0xFFFFFFFF)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Scripture Font Size", fontWeight = FontWeight.Bold)
                        }
                        Text("${fontSize.toInt()} sp", fontWeight = FontWeight.Bold, color = GoldSecondary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = fontSize,
                        onValueChange = {
                            viewModel.updateFontSize(it)
                        },
                        valueRange = 14f..24f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            activeTrackColor = EmeraldPrimary,
                            thumbColor = GoldSecondary
                        ),
                        modifier = Modifier.testTag("font_size_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Text size demo box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDark) Color(0x11FFFFFF) else Color(0x06000000),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "اِبتدا میں خُدا نے آسمان اور زمین کو پَیدا کیا۔",
                            style = getUrduTextStyle(fontSize).copy(textAlign = TextAlign.Center),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Translation Selector Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1C2128) else Color(0xFFFFFFFF)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Default Translation", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Column of choices
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (translationMode == "both") EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.updateTranslationMode("both")
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Urdu Geo + KJV English (Both)", fontWeight = FontWeight.Medium)
                            RadioButton(
                                selected = translationMode == "both",
                                onClick = { viewModel.updateTranslationMode("both") },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (translationMode == "urdu") EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.updateTranslationMode("urdu")
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Urdu Geo Version (Only)", fontWeight = FontWeight.Medium)
                            RadioButton(
                                selected = translationMode == "urdu",
                                onClick = { viewModel.updateTranslationMode("urdu") },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (translationMode == "english") EmeraldPrimary.copy(alpha = 0.15f) else Color.Transparent
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.updateTranslationMode("english")
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("KJV English Translation (Only)", fontWeight = FontWeight.Medium)
                            RadioButton(
                                selected = translationMode == "english",
                                onClick = { viewModel.updateTranslationMode("english") },
                                colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                            )
                        }
                    }
                }
            }
        }

        // About / Credits Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF141F1A) else Color(0xFFF0FDFC)
                ),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Noor-e-Kalaam",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Presented by SOZO",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = "Version 1.0 (Production Ready)",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Made in Pakistan with ❤️\nDedicated for sacred spiritual study, anytime offline.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// SHARED UTILS / HELPER COMPOSABLES
@Composable
fun EmptyStateLayout(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    descUrdu: String,
    descEnglish: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GoldSecondary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GoldSecondary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = descUrdu,
            style = getUrduTextStyle(16f).copy(textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = descEnglish,
            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun CustomBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    val items = listOf(
        Triple(Screen.Home, Icons.Filled.Home, "Kalaam"),
        Triple(Screen.Reading, Icons.Filled.MenuBook, "Sabaq"),
        Triple(Screen.Search, Icons.Filled.Search, "Talash"),
        Triple(Screen.Bookmarks, Icons.Filled.Bookmark, "Mehfooz"),
        Triple(Screen.Settings, Icons.Filled.Settings, "Tarteeb")
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                selected = currentScreen == screen,
                onClick = { onNavigate(screen) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = EmeraldPrimary,
                    indicatorColor = EmeraldPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}
