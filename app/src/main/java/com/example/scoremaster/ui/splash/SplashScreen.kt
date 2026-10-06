package com.example.scoremaster.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoremaster.ui.theme.DeepNavy
import com.example.scoremaster.ui.theme.FreshElectricGreen
import com.example.scoremaster.ui.theme.PureWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "splashIconSpin")

    val iconRotationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "iconRotationY"
    )

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F324D),
                        DeepNavy,
                        Color(0xFF04101D)
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            // 3D Animated Scale Icon with Electric Green Glow Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                }
            ) {
                // Ambient Outer Glow Ring
                Surface(
                    color = FreshElectricGreen.copy(alpha = 0.18f),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer {
                            scaleX = glowPulse
                            scaleY = glowPulse
                        }
                ) {}

                // Inner Badge
                Surface(
                    color = DeepNavy,
                    shape = CircleShape,
                    border = BorderStroke(2.dp, FreshElectricGreen),
                    shadowElevation = 12.dp
                ) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .graphicsLayer {
                                this.rotationY = iconRotationY
                                this.cameraDistance = 14f * density
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsCricket,
                            contentDescription = "ScoreMaster Logo",
                            tint = FreshElectricGreen,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // App Title
            Text(
                text = "ScoreMaster",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PureWhite,
                letterSpacing = 1.2.sp
            )

            // Tagline in Electric Green
            Text(
                text = "CRICKET SCORING & ANALYTICS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = FreshElectricGreen,
                letterSpacing = 2.2.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Loading Circular Progress
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = FreshElectricGreen,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Version Badge
            Surface(
                color = FreshElectricGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, FreshElectricGreen.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "v1.0 • ICC DLS Engine",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = FreshElectricGreen,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}
