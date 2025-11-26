package com.example.smartfit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * MATERIAL DESIGN 3 THEME - SmartFit
 *
 * This file sets up the app's theme using Material Design 3 (Material You).
 *
 * KEY CONCEPTS:
 *
 * COLOR SCHEME:
 * - Defines all colors used in the app
 * - Automatically switches between light and dark
 * - Uses ColorScheme object from Material 3
 *
 * TYPOGRAPHY:
 * - Defines text styles (headings, body, labels)
 * - Consistent text sizing across app
 *
 * SHAPES:
 * - Defines corner radii for components
 * - Material 3 uses rounded corners
 *
 * DYNAMIC COLOR (Android 12+):
 * - Adapts to user's wallpaper colors
 * - Creates personalized experience
 * - Falls back to custom colors on older Android
 *
 * DARK MODE:
 * - Automatically detected from system settings
 * - Can be overridden by user preference
 * - Reduces eye strain, saves battery (OLED screens)
 */

/**
 * LIGHT COLOR SCHEME
 *
 * Used when system is in light mode (or user preference)
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors - main brand color
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    // Secondary colors - accent
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    // Tertiary colors - supporting
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    // Error colors
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    // Background
    background = Background,
    onBackground = OnBackground,

    // Surface (cards, sheets, etc.)
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,

    // Outline (borders, dividers)
    outline = Outline,
    outlineVariant = OutlineVariant
)

/**
 * DARK COLOR SCHEME
 *
 * Used when system is in dark mode
 * Colors are adjusted for better visibility on dark backgrounds
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,

    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,

    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,

    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,

    background = BackgroundDark,
    onBackground = OnBackgroundDark,

    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,

    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

/**
 * SMARTFIT THEME - Main theme composable
 *
 * Wraps your entire app to apply theme consistently
 *
 * @param darkTheme - Whether to use dark theme (auto-detected by default)
 * @param dynamicColor - Whether to use Android 12+ dynamic colors
 * @param content - Your app's UI content
 *
 * HOW IT WORKS:
 * 1. Checks if dynamic color is available (Android 12+)
 * 2. If yes and enabled, uses system-generated colors from wallpaper
 * 3. If no, uses custom light/dark color schemes
 * 4. Sets up system bar colors (status bar, navigation bar)
 * 5. Applies MaterialTheme with colors, typography, shapes
 *
 * USAGE IN MAIN ACTIVITY:
 * ```kotlin
 * setContent {
 *     SmartFitTheme {
 *         // Your app content here
 *         SmartFitNavigation()
 *     }
 * }
 * ```
 *
 * FORCING DARK MODE (for testing):
 * ```kotlin
 * SmartFitTheme(darkTheme = true) {
 *     // Always dark
 * }
 * ```
 *
 * DISABLING DYNAMIC COLOR:
 * ```kotlin
 * SmartFitTheme(dynamicColor = false) {
 *     // Always use custom colors
 * }
 * ```
 */
@Composable
fun SmartFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // Auto-detect from system
    dynamicColor: Boolean = true,  // Use dynamic color on Android 12+
    content: @Composable () -> Unit
) {
    /**
     * DYNAMIC COLOR SUPPORT (Android 12+)
     *
     * Material You feature that adapts app colors to user's wallpaper
     * Creates personalized, cohesive system-wide appearance
     *
     * Requires:
     * - Android 12 (API 31) or higher
     * - User hasn't disabled Material You in system settings
     */
    val colorScheme = when {
        // Android 12+ with dynamic color enabled
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                dynamicDarkColorScheme(context)  // System-generated dark colors
            } else {
                dynamicLightColorScheme(context)  // System-generated light colors
            }
        }

        // Dark mode with custom colors
        darkTheme -> DarkColorScheme

        // Light mode with custom colors
        else -> LightColorScheme
    }

    /**
     * SYSTEM BAR STYLING
     *
     * Updates status bar and navigation bar colors to match theme
     * Uses edge-to-edge display for immersive experience
     *
     * WHY THIS IS IMPORTANT:
     * - Makes app feel polished and integrated
     * - No jarring color mismatches
     * - Modern Android aesthetic
     */
    val view = LocalView.current
    if (!view.isInEditMode) {  // Don't run in Android Studio preview
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color to match background
            window.statusBarColor = colorScheme.background.toArgb()
            // Set navigation bar color to match background
            window.navigationBarColor = colorScheme.background.toArgb()

            // Set status bar icon colors (dark icons on light background, light icons on dark background)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme  // Dark icons in light mode
                isAppearanceLightNavigationBars = !darkTheme  // Dark nav bar icons in light mode
            }
        }
    }

    /**
     * MATERIAL THEME
     *
     * Applies the color scheme to all Material components
     * Components automatically use correct colors based on their role
     *
     * EXAMPLES:
     * - Button() automatically uses primary color
     * - Card() automatically uses surface color
     * - Text() automatically uses onSurface color
     * - You can override with MaterialTheme.colorScheme.anyColor
     */
    MaterialTheme(
        colorScheme = colorScheme,
        // typography = Typography,  // TODO: Define in Type.kt for custom text styles
        content = content
    )
}

/**
 * ACCESSING THEME COLORS IN COMPOSABLES:
 *
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     // Access colors
 *     val primaryColor = MaterialTheme.colorScheme.primary
 *     val backgroundColor = MaterialTheme.colorScheme.background
 *
 *     Surface(
 *         color = backgroundColor  // Use theme background
 *     ) {
 *         Button(
 *             colors = ButtonDefaults.buttonColors(
 *                 containerColor = primaryColor  // Use theme primary
 *             )
 *         ) {
 *             Text("Click Me")
 *         }
 *     }
 * }
 * ```
 *
 * ACCESSING TYPOGRAPHY:
 * ```kotlin
 * Text(
 *     text = "Heading",
 *     style = MaterialTheme.typography.headlineLarge
 * )
 * Text(
 *     text = "Body text",
 *     style = MaterialTheme.typography.bodyMedium
 * )
 * ```
 *
 * THEMING BEST PRACTICES:
 * ✓ Always use MaterialTheme.colorScheme.* instead of hardcoded colors
 * ✓ Use semantic color names (primary, surface) not specific colors (green, white)
 * ✓ Test in both light and dark modes
 * ✓ Ensure sufficient contrast for accessibility
 * ✓ Use typography scale for consistent text sizing
 * ✓ Let Material components handle colors automatically when possible
 *
 * SUPPORTING USER THEME PREFERENCE:
 *
 * You could extend this to support user-chosen theme:
 * ```kotlin
 * // In ViewModel
 * val themePreference = userPreferencesRepository.themePreference.collectAsState()
 *
 * // In MainActivity
 * SmartFitTheme(
 *     darkTheme = when(themePreference.value) {
 *         ThemePreference.LIGHT -> false
 *         ThemePreference.DARK -> true
 *         ThemePreference.SYSTEM -> isSystemInDarkTheme()
 *     }
 * ) {
 *     // App content
 * }
 * ```
 */
