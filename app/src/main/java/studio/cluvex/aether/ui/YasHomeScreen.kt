package studio.cluvex.aether.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import studio.cluvex.aether.R
import studio.cluvex.aether.ai.AiSession
import studio.cluvex.aether.ai.AiTopic
import studio.cluvex.aether.core.IpEndpoint
import studio.cluvex.aether.data.AppLanguage
import studio.cluvex.aether.data.LanguagePrefs
import studio.cluvex.aether.data.VisualPrefs
import studio.cluvex.aether.model.ConnectionPreset
import studio.cluvex.aether.model.ConnectionProfile
import studio.cluvex.aether.model.ConnectionState
import studio.cluvex.aether.model.Protocol
import studio.cluvex.aether.model.TransportBackend
import studio.cluvex.aether.model.isBusy
import studio.cluvex.aether.model.isConnected
import studio.cluvex.aether.ui.ai.AiHostContext
import studio.cluvex.aether.ui.ai.LocalAiHost
import studio.cluvex.aether.ui.components.HomeSettingsHost
import studio.cluvex.aether.ui.components.LocalHomeAnimationsEnabled
import studio.cluvex.aether.ui.components.LocalHomeVisible
import studio.cluvex.aether.ui.components.PresetSelector
import studio.cluvex.aether.ui.components.YasConnectButton
import studio.cluvex.aether.ui.components.YasStatusCard
import studio.cluvex.aether.ui.settings.RowDivider
import studio.cluvex.aether.ui.settings.SettingsGroup
import studio.cluvex.aether.ui.settings.SettingsHost
import studio.cluvex.aether.ui.settings.SettingsNavRow
import studio.cluvex.aether.ui.settings.SettingsRoute
import studio.cluvex.aether.ui.theme.YasBackground
import studio.cluvex.aether.ui.theme.YasBackgroundSecondary
import studio.cluvex.aether.ui.theme.YasCardBackground
import studio.cluvex.aether.ui.theme.YasCardBorder
import studio.cluvex.aether.ui.theme.YasCyan
import studio.cluvex.aether.ui.theme.YasViolet

/**
 * YAS VPN Home Screen — Strict Visual Replication of the approved reference design.
 *
 * Implements:
 * 1. Branded top header with 3D faceted crystal Y logo, "YAS VPN", "More Freedom. More You.",
 *    capsule language switch `[ FA | EN ]`, and circular menu trigger.
 * 2. Hero Centerpiece with 3D faceted crystal polyhedron, luminous orbital rings, particle field,
 *    dark canyon terrain, and interactive concentric power button.
 * 3. 3-Column Translucent Status Card (Disconnected | Latency | Your IP).
 * 4. Connection Mode selector with 6 vertical cards, 3D luminous icons, cyan/violet gradient selection,
 *    and top-right checkmark badge.
 * 5. Transport Protocol selector with 5 pill chips (Smart, MASQUE, WireGuard, Gool, MASQUE ×2).
 * 6. Privacy Banner with low-poly wireframe mountain illustration and "Your privacy our priority".
 * 7. Fixed Bottom Navigation Dock (Home, Modes, Tools, Settings) with active indicator pill.
 * 8. Comprehensive Drawer Navigation retaining all preset, AI, security, and networking tools.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YasHomeScreen(
    state: ConnectionState,
    profile: ConnectionProfile,
    connectedSince: Long?,
    ipInfo: IpEndpoint?,
    ipLoading: Boolean,
    onProfileChange: (ConnectionProfile) -> Unit,
    onToggleConnection: () -> Unit,
    presets: List<ConnectionPreset> = emptyList(),
    activePresetId: String? = null,
    onSelectPreset: (String) -> Unit = {},
    onSavePresetAs: (String) -> Unit = {},
    onDeletePreset: (String) -> Unit = {},
    onRenamePreset: (String, String) -> Unit = { _, _ -> },
    onImportPresets: () -> Unit = {},
    onExportPresets: () -> Unit = {},
    presetBusy: Boolean = false,
    modifier: Modifier = Modifier,
) {
    var route by remember { mutableStateOf<SettingsRoute?>(null) }
    val aiSettings by AiSession.settings.collectAsState()
    val context = LocalContext.current
    val homeAnimationsEnabled by VisualPrefs.homeAnimationsFlow(context).collectAsState()

    CompositionLocalProvider(
        LocalHomeAnimationsEnabled provides homeAnimationsEnabled,
        LocalAiHost provides AiHostContext(
            profile,
            state,
            aiSettings.showHints,
            openAiSettings = { route = SettingsRoute.AI },
            openConnection = { route = SettingsRoute.CONNECTION },
            openChat = { route = SettingsRoute.AI_CHAT },
        ),
    ) {
        HomeSettingsHost(route == null, modifier.fillMaxSize(), home = {
            YasHomeSurface(
                state = state,
                profile = profile,
                connectedSince = connectedSince,
                ipInfo = ipInfo,
                ipLoading = ipLoading,
                onProfileChange = onProfileChange,
                onToggleConnection = onToggleConnection,
                presets = presets,
                activePresetId = activePresetId,
                onSelectPreset = onSelectPreset,
                onSavePresetAs = onSavePresetAs,
                onDeletePreset = onDeletePreset,
                onRenamePreset = onRenamePreset,
                onImportPresets = onImportPresets,
                onExportPresets = onExportPresets,
                presetBusy = presetBusy,
                onOpenSettings = { route = it },
            )
        }, settings = {
            route?.let { activeRoute ->
                SettingsHost(
                    start = activeRoute,
                    state = state,
                    profile = profile,
                    onProfileChange = onProfileChange,
                    onClose = { route = null },
                )
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YasHomeSurface(
    state: ConnectionState,
    profile: ConnectionProfile,
    connectedSince: Long?,
    ipInfo: IpEndpoint?,
    ipLoading: Boolean,
    onProfileChange: (ConnectionProfile) -> Unit,
    onToggleConnection: () -> Unit,
    presets: List<ConnectionPreset>,
    activePresetId: String?,
    onSelectPreset: (String) -> Unit,
    onSavePresetAs: (String) -> Unit,
    onDeletePreset: (String) -> Unit,
    onRenamePreset: (String, String) -> Unit,
    onImportPresets: () -> Unit,
    onExportPresets: () -> Unit,
    presetBusy: Boolean,
    onOpenSettings: (SettingsRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val isPersian = remember { LanguagePrefs.isPersian(context) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = YasBackgroundSecondary,
                modifier = Modifier.fillMaxWidth(0.85f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                ) {
                    // Header with Luminous Y Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.yas_logo),
                            contentDescription = "YAS Logo",
                            modifier = Modifier.size(46.dp),
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "YAS ",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(
                                    text = "VPN",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Light,
                                    color = Color.White.copy(alpha = 0.9f),
                                )
                            }
                            Text(
                                text = "More Freedom. More You.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8A99BA),
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Presets Management (Drawer keeps preset workflow accessible)
                    if (presets.isNotEmpty()) {
                        Text(
                            text = "Connection Presets",
                            color = YasCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        PresetSelector(
                            presets = presets,
                            activeId = activePresetId,
                            locked = state.isConnected || state.isBusy || presetBusy || !LocalHomeVisible.current,
                            onSelect = onSelectPreset,
                            onSaveAsNew = onSavePresetAs,
                            onDelete = onDeletePreset,
                            onRename = onRenamePreset,
                            onImport = onImportPresets,
                            onExport = onExportPresets,
                        )
                        Spacer(Modifier.height(20.dp))
                    }

                    val navigateTo: (SettingsRoute) -> Unit = { targetRoute ->
                        onOpenSettings(targetRoute)
                        drawerScope.launch { drawerState.close() }
                    }

                    // Navigation Groups
                    SettingsGroup {
                        SettingsNavRow(
                            aiTopic = AiTopic.SETTINGS,
                            title = stringResource(R.string.settings_title),
                            summary = stringResource(R.string.settings_subtitle),
                            icon = Icons.Rounded.Settings,
                            onClick = { navigateTo(SettingsRoute.HOME) },
                        )
                        RowDivider()
                        SettingsNavRow(
                            aiTopic = AiTopic.BACKEND,
                            title = stringResource(R.string.cat_connection),
                            summary = stringResource(R.string.cat_connection_desc),
                            icon = Icons.Rounded.Layers,
                            onClick = { navigateTo(SettingsRoute.CONNECTION) },
                        )
                        RowDivider()
                        SettingsNavRow(
                            aiTopic = AiTopic.PROTOCOL,
                            title = stringResource(R.string.advanced),
                            summary = stringResource(R.string.advanced_open),
                            icon = Icons.Rounded.Tune,
                            onClick = { navigateTo(SettingsRoute.QUICK) },
                        )
                        RowDivider()
                        SettingsNavRow(
                            aiTopic = AiTopic.CUSTOM_CONFIGS,
                            title = stringResource(R.string.cc_title),
                            summary = stringResource(R.string.cc_nav_desc),
                            icon = Icons.Rounded.Route,
                            onClick = { navigateTo(SettingsRoute.CUSTOM_CONFIGS) },
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    SettingsGroup {
                        SettingsNavRow(
                            aiTopic = AiTopic.AI_CHAT,
                            title = stringResource(R.string.ai_title),
                            summary = stringResource(R.string.ai_subtitle),
                            icon = Icons.Rounded.AutoAwesome,
                            onClick = { navigateTo(SettingsRoute.AI) },
                        )
                        RowDivider()
                        SettingsNavRow(
                            aiTopic = AiTopic.DIAGNOSTICS,
                            title = stringResource(R.string.diag_title),
                            summary = stringResource(R.string.diag_subtitle),
                            icon = Icons.Rounded.BugReport,
                            onClick = { navigateTo(SettingsRoute.DIAGNOSTICS) },
                        )
                        RowDivider()
                        SettingsNavRow(
                            aiTopic = AiTopic.SHARE,
                            title = stringResource(R.string.share_title),
                            summary = stringResource(R.string.share_subtitle),
                            icon = Icons.Rounded.Wifi,
                            onClick = { navigateTo(SettingsRoute.SHARE) },
                        )
                        RowDivider()
                        SettingsNavRow(
                            aiTopic = AiTopic.ABOUT,
                            title = stringResource(R.string.about_title),
                            summary = stringResource(R.string.about_subtitle),
                            icon = Icons.Rounded.Info,
                            onClick = { navigateTo(SettingsRoute.ABOUT) },
                        )
                    }
                }
            }
        },
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(YasBackground),
        ) {
            // Main Vertical Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .statusBarsPadding()
                    .padding(bottom = 80.dp), // Space for fixed bottom navigation dock
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    // Left: Logo and Brand Identity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.yas_logo),
                            contentDescription = "YAS Logo",
                            modifier = Modifier.size(46.dp),
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "YAS ",
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                )
                                Text(
                                    text = "VPN",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = 0.5.sp,
                                )
                            }
                            Text(
                                text = "More Freedom. More You.",
                                color = Color(0xFF8A99BA),
                                fontSize = 11.sp,
                            )
                        }
                    }

                    // Right: Language Switcher Pill + Circular Hamburger Menu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Capsule [ FA | EN ]
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFF10172D))
                                .border(1.dp, Color(0xFF222F50), RoundedCornerShape(22.dp))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // FA
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isPersian) Color(0xFF4564F5) else Color.Transparent)
                                    .clickable {
                                        if (!isPersian) {
                                            LanguagePrefs.write(context, AppLanguage.PERSIAN)
                                            LanguagePrefs.findActivity(context)?.recreate()
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "FA",
                                    color = if (isPersian) Color.White else Color(0xFF7A89A8),
                                    fontWeight = if (isPersian) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                )
                            }

                            // EN
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (!isPersian) Color(0xFF4564F5) else Color.Transparent)
                                    .clickable {
                                        if (isPersian) {
                                            LanguagePrefs.write(context, AppLanguage.ENGLISH)
                                            LanguagePrefs.findActivity(context)?.recreate()
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "EN",
                                    color = if (!isPersian) Color.White else Color(0xFF7A89A8),
                                    fontWeight = if (!isPersian) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                )
                            }
                        }

                        // Hamburger Circular Button
                        IconButton(
                            onClick = { drawerScope.launch { drawerState.open() } },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10172D))
                                .border(1.dp, Color(0xFF222F50), CircleShape)
                                .testTag("home_menu_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                // Hero Centerpiece & Power Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(852f / 730f),
                    contentAlignment = Alignment.Center,
                ) {
                    // Artwork: Crystalline Polyhedron, Floating Y, Orbital Rings, Rock Canyon & Glow
                    Image(
                        painter = painterResource(R.drawable.yas_hero_full),
                        contentDescription = "Centerpiece Crystal and Power Button",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxSize(),
                    )

                    // Interactive Power Button Area Overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        YasConnectButton(
                            state = state,
                            onClick = onToggleConnection,
                            modifier = Modifier.size(136.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Translucent 3-Column Status Card
                YasStatusCard(
                    state = state,
                    ipInfo = ipInfo,
                    ipLoading = ipLoading,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(Modifier.height(14.dp))

                // Connection Mode Section Card
                ConnectionModeCard(
                    profile = profile,
                    onProfileChange = onProfileChange,
                    onOpenAdvanced = { onOpenSettings(SettingsRoute.CONNECTION) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(Modifier.height(14.dp))

                // Transport Protocol Section Card
                TransportProtocolCard(
                    profile = profile,
                    onProfileChange = onProfileChange,
                    onOpenDetails = { onOpenSettings(SettingsRoute.QUICK) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(Modifier.height(14.dp))

                // Privacy Banner with Low-Poly Mountain Illustration
                PrivacyBanner(
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(Modifier.height(20.dp))
            }

            // Fixed Bottom Navigation Dock
            BottomNavigationDock(
                onNavigate = { routeTarget ->
                    if (routeTarget != null) onOpenSettings(routeTarget)
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/**
 * 6-Card Connection Mode Section with 3D Luminous Icons and Selection Indicator.
 */
@Composable
private fun ConnectionModeCard(
    profile: ConnectionProfile,
    onProfileChange: (ConnectionProfile) -> Unit,
    onOpenAdvanced: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xD9090F22),
                        Color(0xEE060A18),
                    ),
                ),
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF202C4D),
                        Color(0xFF10172C),
                    ),
                ),
                RoundedCornerShape(22.dp),
            )
            .padding(vertical = 16.dp),
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_yas_target),
                        contentDescription = null,
                        tint = YasCyan,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Connection Mode",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenAdvanced() },
                ) {
                    Text(
                        text = "Advanced",
                        color = Color(0xFF7E8EA8),
                        fontSize = 13.sp,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Advanced",
                        tint = Color(0xFF7E8EA8),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 6 Mode Tiles
            val modes = listOf(
                ModeItem("Aether", R.drawable.ic_mode_aether, TransportBackend.AETHER),
                ModeItem("Aether →\nPsiphon", R.drawable.ic_mode_psiphon, TransportBackend.AETHER_PSIPHON),
                ModeItem("Tor", R.drawable.ic_mode_tor, TransportBackend.TOR),
                ModeItem("Aether →\nTor", R.drawable.ic_mode_aether_tor, TransportBackend.AETHER_TOR),
                ModeItem("Tor →\nPsiphon", R.drawable.ic_mode_tor_psiphon, TransportBackend.TOR_PSIPHON),
                ModeItem("Tor →\nAether", R.drawable.ic_mode_tor_aether, TransportBackend.TOR_AETHER),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                modes.forEach { item ->
                    val isSelected = profile.backend == item.backend

                    Box(
                        modifier = Modifier
                            .width(86.dp)
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFF0F1832) else Color(0xFF070B17),
                            )
                            .border(
                                width = if (isSelected) 1.8.dp else 1.dp,
                                brush = if (isSelected) {
                                    Brush.linearGradient(listOf(YasCyan, YasViolet))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF1A2644), Color(0xFF0E1628)))
                                },
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable {
                                onProfileChange(profile.copy(backend = item.backend))
                            }
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        // Top-right selection badge
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF6A44FF)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Image(
                                painter = painterResource(item.iconRes),
                                contentDescription = item.label,
                                modifier = Modifier.size(46.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = item.label,
                                color = if (isSelected) Color.White else Color(0xFF8E9BB4),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class ModeItem(
    val label: String,
    val iconRes: Int,
    val backend: TransportBackend,
)

/**
 * 5-Pill Transport Protocol Selector (Smart, MASQUE, WireGuard, Gool, MASQUE ×2).
 */
@Composable
private fun TransportProtocolCard(
    profile: ConnectionProfile,
    onProfileChange: (ConnectionProfile) -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xD9090F22),
                        Color(0xEE060A18),
                    ),
                ),
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF202C4D),
                        Color(0xFF10172C),
                    ),
                ),
                RoundedCornerShape(22.dp),
            )
            .padding(vertical = 16.dp),
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_yas_branch),
                        contentDescription = null,
                        tint = YasViolet,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Transport Protocol",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenDetails() },
                ) {
                    Text(
                        text = "Details",
                        color = Color(0xFF7E8EA8),
                        fontSize = 13.sp,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Details",
                        tint = Color(0xFF7E8EA8),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val protocols = listOf(
                ProtocolItem("Smart", R.drawable.ic_proto_smart, Protocol.AUTO),
                ProtocolItem("MASQUE", R.drawable.ic_proto_masque, Protocol.MASQUE),
                ProtocolItem("WireGuard", R.drawable.ic_proto_wireguard, Protocol.WIREGUARD),
                ProtocolItem("Gool", R.drawable.ic_proto_gool, Protocol.GOOL),
                ProtocolItem("MASQUE ×2", R.drawable.ic_proto_mim, Protocol.MIM),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                protocols.forEach { item ->
                    val isSelected = profile.protocol == item.protocol

                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) {
                                    Brush.linearGradient(
                                        listOf(Color(0x3300E5FF), Color(0x338B5CF6)),
                                    )
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF0A1022), Color(0xFF070B18)))
                                },
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                brush = if (isSelected) {
                                    Brush.linearGradient(listOf(YasCyan, YasViolet))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF1C2846), Color(0xFF10182C)))
                                },
                                shape = RoundedCornerShape(20.dp),
                            )
                            .clickable {
                                onProfileChange(profile.copy(protocol = item.protocol))
                            }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            painter = painterResource(item.iconRes),
                            contentDescription = item.label,
                            tint = if (isSelected) Color.White else Color(0xFF8E9BB4),
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = item.label,
                            color = if (isSelected) Color.White else Color(0xFF8E9BB4),
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

private data class ProtocolItem(
    val label: String,
    val iconRes: Int,
    val protocol: Protocol,
)

/**
 * Privacy Banner featuring Low-Poly Neon Wireframe Mountain Range.
 */
@Composable
private fun PrivacyBanner(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xD9090F22),
                        Color(0xEE060A18),
                    ),
                ),
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF202C4D),
                        Color(0xFF10172C),
                    ),
                ),
                RoundedCornerShape(22.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left Globe Icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0C142A))
                    .border(1.dp, Color(0xFF1D294B), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yas_globe),
                    contentDescription = null,
                    tint = YasCyan,
                    modifier = Modifier.size(18.dp),
                )
            }

            // Middle Description
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 6.dp),
            ) {
                Text(
                    text = "Select a mode and protocol",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "to get started",
                    color = Color(0xFF7A89A8),
                    fontSize = 11.sp,
                )
            }

            // Mountain Wireframe Graphic
            Image(
                painter = painterResource(R.drawable.yas_mountain_wireframe),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(38.dp)
                    .width(96.dp),
            )

            // Right Tagline with Gradient Underline
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 6.dp),
            ) {
                Text(
                    text = "Your privacy",
                    color = Color(0xFF8A99BA),
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                )
                Text(
                    text = "our priority",
                    color = Color(0xFFA8B8DC),
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .width(62.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            Brush.horizontalGradient(listOf(YasViolet, YasCyan)),
                        ),
                )
            }
        }
    }
}

/**
 * Fixed Bottom Navigation Dock matching reference composition.
 */
@Composable
private fun BottomNavigationDock(
    onNavigate: (SettingsRoute?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xF0050814))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF141F38), Color(0xFF080D1A)),
                ),
                shape = RoundedCornerShape(0.dp),
            )
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Home (Active)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate(null) }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yas_nav_home),
                    contentDescription = "Home",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Home",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(listOf(YasCyan, YasViolet)),
                        ),
                )
            }

            // Modes
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate(SettingsRoute.CONNECTION) }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yas_nav_modes),
                    contentDescription = "Modes",
                    tint = Color(0xFF6E7D9E),
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Modes",
                    color = Color(0xFF6E7D9E),
                    fontSize = 11.sp,
                )
                Spacer(Modifier.height(5.5.dp))
            }

            // Tools
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate(SettingsRoute.DIAGNOSTICS) }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yas_nav_tools),
                    contentDescription = "Tools",
                    tint = Color(0xFF6E7D9E),
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Tools",
                    color = Color(0xFF6E7D9E),
                    fontSize = 11.sp,
                )
                Spacer(Modifier.height(5.5.dp))
            }

            // Settings
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onNavigate(SettingsRoute.HOME) }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_yas_nav_settings),
                    contentDescription = "Settings",
                    tint = Color(0xFF6E7D9E),
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Settings",
                    color = Color(0xFF6E7D9E),
                    fontSize = 11.sp,
                )
                Spacer(Modifier.height(5.5.dp))
            }
        }
    }
}
