package com.example.scoremaster.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class CelebrationType {
    FOUR,
    SIX,
    WICKET,
    MILESTONE_50,
    MILESTONE_100
}

private data class Particle(
    val angle: Double,
    val speed: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun CricketCelebrationOverlay(
    type: CelebrationType,
    playerName: String? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }
    val particleProgress = remember { Animatable(0f) }

    val particles = remember {
        val colors = when (type) {
            CelebrationType.FOUR -> listOf(Color(0xFF10B981), Color(0xFF34D399), Color(0xFF6EE7B7), Color(0xFF19C37D))
            CelebrationType.SIX -> listOf(Color(0xFF3B82F6), Color(0xFF60A5FA), Color(0xFF19C37D), Color(0xFFF59E0B))
            CelebrationType.WICKET -> listOf(Color(0xFFEF4444), Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF071A2B))
            CelebrationType.MILESTONE_50, CelebrationType.MILESTONE_100 -> listOf(Color(0xFFF59E0B), Color(0xFFFBBF24), Color(0xFFFCD34D), Color(0xFFFFFFFF))
        }
        List(32) {
            Particle(
                angle = Random.nextDouble(0.0, 2.0 * Math.PI),
                speed = Random.nextFloat() * 220f + 120f,
                radius = Random.nextFloat() * 6f + 4f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    LaunchedEffect(type) {
        // Spring bouncy pop-in
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )

        // Particle expansion
        particleProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(900)
        )

        delay(300)

        // Smooth fade-out
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(350)
        )

        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(alphaAnim.value),
        contentAlignment = Alignment.Center
    ) {
        // Particle Explosion Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            particles.forEach { p ->
                val distance = p.speed * particleProgress.value
                val x = center.x + (distance * cos(p.angle)).toFloat()
                val y = center.y + (distance * sin(p.angle)).toFloat()
                val alpha = (1f - particleProgress.value).coerceIn(0f, 1f)

                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.radius * (1f - particleProgress.value * 0.5f),
                    center = Offset(x, y)
                )
            }
        }

        // Bouncing Celebration Badge
        Card(
            modifier = Modifier
                .wrapContentSize()
                .scale(scaleAnim.value)
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (type) {
                    CelebrationType.FOUR -> Color(0xFF064E3B)
                    CelebrationType.SIX -> Color(0xFF071A2B)
                    CelebrationType.WICKET -> Color(0xFF7F1D1D)
                    CelebrationType.MILESTONE_50, CelebrationType.MILESTONE_100 -> Color(0xFF78350F)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = when (type) {
                        CelebrationType.FOUR -> Color(0xFF10B981)
                        CelebrationType.SIX -> Color(0xFF19C37D)
                        CelebrationType.WICKET -> Color(0xFFEF4444)
                        CelebrationType.MILESTONE_50, CelebrationType.MILESTONE_100 -> Color(0xFFF59E0B)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (type) {
                            CelebrationType.FOUR -> "⚡ FOUR!"
                            CelebrationType.SIX -> "🔥 SIX!"
                            CelebrationType.WICKET -> "☝️ WICKET!"
                            CelebrationType.MILESTONE_50 -> "🏆 HALF CENTURY!"
                            CelebrationType.MILESTONE_100 -> "👑 CENTURY!"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = when (type) {
                        CelebrationType.FOUR -> "BOUNDARY FOUR!"
                        CelebrationType.SIX -> "MAXIMUM SIX!"
                        CelebrationType.WICKET -> "OUT!"
                        CelebrationType.MILESTONE_50 -> "50 RUNS!"
                        CelebrationType.MILESTONE_100 -> "100 RUNS!"
                    },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (!playerName.isNull_or_blank()) {
                    Text(
                        text = playerName.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
