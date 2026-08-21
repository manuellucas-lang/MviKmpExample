package com.example.mviexample.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

private val GoogleBlue = Color(0xFF4285F4)
private val GoogleGreen = Color(0xFF34A853)
private val GoogleYellow = Color(0xFFFABB05)
private val GoogleRed = Color(0xFFE94235)
private val WordmarkLight = Color(0xFF5F6368)

private val darkLogo by lazy { buildGooglePayLogo(wordmarkColor = Color.White) }
private val lightLogo by lazy { buildGooglePayLogo(wordmarkColor = WordmarkLight) }

fun googlePayLogo(dark: Boolean): ImageVector = if (dark) darkLogo else lightLogo

private fun buildGooglePayLogo(wordmarkColor: Color): ImageVector =
    ImageVector.Builder(
        name = "GooglePayLogo",
        defaultWidth = 41.dp,
        defaultHeight = 17.dp,
        viewportWidth = 41f,
        viewportHeight = 17f,
    ).apply {
        path(
            fill = SolidColor(wordmarkColor),
            pathFillType = PathFillType.EvenOdd,
        ) {
            moveTo(19.526f, 2.635f)
            verticalLineToRelative(4.083f)
            horizontalLineToRelative(2.518f)
            curveToRelative(0.6f, 0f, 1.096f, -0.202f, 1.488f, -0.605f)
            curveToRelative(0.403f, -0.402f, 0.605f, -0.882f, 0.605f, -1.437f)
            curveToRelative(0f, -0.544f, -0.202f, -1.018f, -0.605f, -1.422f)
            curveToRelative(-0.392f, -0.413f, -0.888f, -0.62f, -1.488f, -0.62f)
            horizontalLineToRelative(-2.518f)
            close()
            moveToRelative(0f, 5.52f)
            verticalLineToRelative(4.736f)
            horizontalLineToRelative(-1.504f)
            verticalLineTo(1.198f)
            horizontalLineToRelative(3.99f)
            curveToRelative(1.013f, 0f, 1.873f, 0.337f, 2.582f, 1.012f)
            curveToRelative(0.72f, 0.675f, 1.08f, 1.497f, 1.08f, 2.466f)
            curveToRelative(0f, 0.991f, -0.36f, 1.819f, -1.08f, 2.482f)
            curveToRelative(-0.697f, 0.665f, -1.559f, 0.996f, -2.583f, 0.996f)
            horizontalLineToRelative(-2.485f)
            verticalLineToRelative(0.001f)
            close()
            moveToRelative(7.668f, 2.287f)
            curveToRelative(0f, 0.392f, 0.166f, 0.718f, 0.499f, 0.98f)
            curveToRelative(0.332f, 0.26f, 0.722f, 0.391f, 1.168f, 0.391f)
            curveToRelative(0.633f, 0f, 1.196f, -0.234f, 1.692f, -0.701f)
            curveToRelative(0.497f, -0.469f, 0.744f, -1.019f, 0.744f, -1.65f)
            curveToRelative(-0.469f, -0.37f, -1.123f, -0.555f, -1.962f, -0.555f)
            curveToRelative(-0.61f, 0f, -1.12f, 0.148f, -1.528f, 0.442f)
            curveToRelative(-0.409f, 0.294f, -0.613f, 0.657f, -0.613f, 1.093f)
            moveToRelative(1.946f, -5.815f)
            curveToRelative(1.112f, 0f, 1.989f, 0.297f, 2.633f, 0.89f)
            curveToRelative(0.642f, 0.594f, 0.964f, 1.408f, 0.964f, 2.442f)
            verticalLineToRelative(4.932f)
            horizontalLineToRelative(-1.439f)
            verticalLineToRelative(-1.11f)
            horizontalLineToRelative(-0.065f)
            curveToRelative(-0.622f, 0.914f, -1.45f, 1.372f, -2.486f, 1.372f)
            curveToRelative(-0.882f, 0f, -1.621f, -0.262f, -2.215f, -0.784f)
            curveToRelative(-0.594f, -0.523f, -0.891f, -1.176f, -0.891f, -1.96f)
            curveToRelative(0f, -0.828f, 0.313f, -1.486f, 0.94f, -1.976f)
            curveToRelative(0.627f, -0.49f, 1.463f, -0.735f, 2.51f, -0.735f)
            curveToRelative(0.892f, 0f, 1.629f, 0.163f, 2.206f, 0.49f)
            verticalLineToRelative(-0.344f)
            curveToRelative(0f, -0.522f, -0.207f, -0.966f, -0.621f, -1.33f)
            arcToRelative(2.132f, 2.132f, 0f, false, false, -1.455f, -0.547f)
            curveToRelative(-0.84f, 0f, -1.504f, 0.353f, -1.995f, 1.062f)
            lineToRelative(-1.324f, -0.834f)
            curveToRelative(0.73f, -1.045f, 1.81f, -1.568f, 3.238f, -1.568f)
            moveToRelative(11.853f, 0.262f)
            lineToRelative(-5.02f, 11.53f)
            horizontalLineTo(34.42f)
            lineToRelative(1.864f, -4.034f)
            lineToRelative(-3.302f, -7.496f)
            horizontalLineToRelative(1.635f)
            lineToRelative(2.387f, 5.749f)
            horizontalLineToRelative(0.032f)
            lineToRelative(2.322f, -5.75f)
            close()
        }
        path(fill = SolidColor(GoogleBlue)) {
            moveTo(13.448f, 7.134f)
            curveToRelative(0f, -0.473f, -0.04f, -0.93f, -0.116f, -1.366f)
            horizontalLineTo(6.988f)
            verticalLineToRelative(2.588f)
            horizontalLineToRelative(3.634f)
            arcToRelative(3.11f, 3.11f, 0f, false, true, -1.344f, 2.042f)
            verticalLineToRelative(1.68f)
            horizontalLineToRelative(2.169f)
            curveToRelative(1.27f, -1.17f, 2.001f, -2.9f, 2.001f, -4.944f)
        }
        path(fill = SolidColor(GoogleGreen)) {
            moveTo(6.988f, 13.7f)
            curveToRelative(1.816f, 0f, 3.344f, -0.595f, 4.459f, -1.621f)
            lineToRelative(-2.169f, -1.681f)
            curveToRelative(-0.603f, 0.406f, -1.38f, 0.643f, -2.29f, 0.643f)
            curveToRelative(-1.754f, 0f, -3.244f, -1.182f, -3.776f, -2.774f)
            horizontalLineTo(0.978f)
            verticalLineToRelative(1.731f)
            arcToRelative(6.728f, 6.728f, 0f, false, false, 6.01f, 3.703f)
        }
        path(fill = SolidColor(GoogleYellow)) {
            moveTo(3.212f, 8.267f)
            arcToRelative(4.034f, 4.034f, 0f, false, true, 0f, -2.572f)
            verticalLineTo(3.964f)
            horizontalLineTo(0.978f)
            arcTo(6.678f, 6.678f, 0f, false, false, 0.261f, 6.98f)
            curveToRelative(0f, 1.085f, 0.26f, 2.11f, 0.717f, 3.017f)
            lineToRelative(2.234f, -1.731f)
            close()
        }
        path(fill = SolidColor(GoogleRed)) {
            moveTo(6.988f, 2.921f)
            curveToRelative(0.992f, 0f, 1.88f, 0.34f, 2.58f, 1.008f)
            verticalLineToRelative(0.001f)
            lineToRelative(1.92f, -1.918f)
            curveTo(10.324f, 0.928f, 8.804f, 0.262f, 6.989f, 0.262f)
            arcToRelative(6.728f, 6.728f, 0f, false, false, -6.01f, 3.702f)
            lineToRelative(2.234f, 1.731f)
            curveToRelative(0.532f, -1.592f, 2.022f, -2.774f, 3.776f, -2.774f)
        }
    }.build()

@Composable
fun GooglePayMark(
    modifier: Modifier = Modifier,
    dark: Boolean? = null,
) {
    val darkBackground = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Image(
        imageVector = googlePayLogo(dark = dark ?: darkBackground),
        contentDescription = "Google Pay",
        modifier = modifier,
    )
}
