package com.magicteamdev0.viralclicker.ui.game.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

// ═══════════════════════════════════════════════
//  CYBERPUNK NAV ICONS — Neon wireframe icons
// ═══════════════════════════════════════════════

enum class NavIconType {
    GAME, SHOP, VAULT, FIRE, PARTY, STAR, LOCK, GEAR,
    TAP, LIKE, GLOVE, BRAIN, ATOM,
    BOT, CHART, FACTORY, ORBIT,
    COIN, GEM, LIGHTNING, TV
}

@Composable
fun CyberpunkNavIcon(
    type: NavIconType,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val glowIntensity by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(260),
        label = "nav_glow_${type.name}"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.38f,
        animationSpec = tween(200),
        label = "nav_alpha_${type.name}"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f * 0.82f
        val c = color.copy(alpha = iconAlpha)

        when (type) {
            NavIconType.GAME  -> drawPlayTriangle(cx, cy, r, c, glowIntensity)
            NavIconType.SHOP  -> drawShopCart(cx, cy, r, c, glowIntensity)
            NavIconType.VAULT -> drawTrophy(cx, cy, r, c, glowIntensity)
            NavIconType.FIRE  -> drawFire(cx, cy, r, c, glowIntensity)
            NavIconType.PARTY -> drawPartyPopper(cx, cy, r, c, glowIntensity)
            NavIconType.STAR  -> drawStar(cx, cy, r, c, glowIntensity)
            NavIconType.LOCK  -> drawLock(cx, cy, r, c, glowIntensity)
            NavIconType.GEAR  -> drawGear(cx, cy, r, c, glowIntensity)
            
            NavIconType.TAP   -> drawTap(cx, cy, r, c, glowIntensity)
            NavIconType.LIKE  -> drawLike(cx, cy, r, c, glowIntensity)
            NavIconType.GLOVE -> drawGlove(cx, cy, r, c, glowIntensity)
            NavIconType.BRAIN -> drawBrain(cx, cy, r, c, glowIntensity)
            NavIconType.ATOM  -> drawAtom(cx, cy, r, c, glowIntensity)
            
            NavIconType.BOT   -> drawBot(cx, cy, r, c, glowIntensity)
            NavIconType.CHART -> drawChart(cx, cy, r, c, glowIntensity)
            NavIconType.FACTORY -> drawFactory(cx, cy, r, c, glowIntensity)
            NavIconType.ORBIT -> drawOrbit(cx, cy, r, c, glowIntensity)
            
            NavIconType.COIN  -> drawCoin(cx, cy, r, c, glowIntensity)
            NavIconType.GEM   -> drawGem(cx, cy, r, c, glowIntensity)
            NavIconType.LIGHTNING -> drawLightning(cx, cy, r, c, glowIntensity)
            NavIconType.TV    -> drawTV(cx, cy, r, c, glowIntensity)
        }
    }
}

// ─────────────────────────────────────────────
//  Helper: multi-pass neon stroke bloom
// ─────────────────────────────────────────────

private fun DrawScope.neonPath(
    path: Path,
    color: Color,
    glow: Float,
    sw: Float,
    cap: StrokeCap = StrokeCap.Round,
    join: StrokeJoin = StrokeJoin.Round
) {
    if (glow > 0.01f) {
        drawPath(path, color.copy(alpha = glow * 0.09f), style = Stroke(sw * 8f, cap = cap, join = join))
        drawPath(path, color.copy(alpha = glow * 0.22f), style = Stroke(sw * 4f, cap = cap, join = join))
        drawPath(path, color.copy(alpha = glow * 0.48f), style = Stroke(sw * 2f, cap = cap, join = join))
    }
    drawPath(path, color, style = Stroke(sw, cap = cap, join = join))
}

private fun DrawScope.neonCircle(center: Offset, radius: Float, color: Color, glow: Float) {
    if (glow > 0.01f) {
        drawCircle(color.copy(alpha = glow * 0.30f), radius * 2.8f, center)
        drawCircle(color.copy(alpha = glow * 0.55f), radius * 1.8f, center)
    }
    drawCircle(color, radius, center)
}

// ─────────────────────────────────────────────
//  GAME — right-pointing play triangle
// ─────────────────────────────────────────────

private fun DrawScope.drawPlayTriangle(
    cx: Float, cy: Float, r: Float, color: Color, glow: Float
) {
    val sw = r * 0.09f
    val triPath = Path().apply {
        moveTo(cx + r * 0.58f, cy)
        lineTo(cx - r * 0.36f, cy - r * 0.54f)
        lineTo(cx - r * 0.36f, cy + r * 0.54f)
        close()
    }
    neonPath(triPath, color, glow, sw)

    // Apex spark
    neonCircle(Offset(cx + r * 0.58f, cy), r * 0.065f, color, glow * 0.7f)
}

// ─────────────────────────────────────────────
//  SHOP — wireframe shopping cart
// ─────────────────────────────────────────────

private fun DrawScope.drawShopCart(
    cx: Float, cy: Float, r: Float, color: Color, glow: Float
) {
    val sw = r * 0.09f

    // Basket body (slightly wider at base = trapezoid)
    val basketPath = Path().apply {
        val bLeft  = cx - r * 0.42f
        val bRight = cx + r * 0.42f
        val bTop   = cy - r * 0.04f
        val bBot   = cy + r * 0.45f
        moveTo(bLeft, bTop)
        lineTo(bRight, bTop)
        lineTo(bRight + r * 0.06f, bBot)
        lineTo(bLeft  - r * 0.06f, bBot)
        close()
    }

    // Handle: L-shape from top-left corner up and across
    val handlePath = Path().apply {
        moveTo(cx - r * 0.42f, cy - r * 0.04f)   // left basket edge
        lineTo(cx - r * 0.60f, cy - r * 0.04f)   // extend left
        lineTo(cx - r * 0.60f, cy - r * 0.54f)   // up
        lineTo(cx + r * 0.10f, cy - r * 0.54f)   // across
        lineTo(cx + r * 0.42f, cy - r * 0.04f)   // angled down to basket
    }

    neonPath(basketPath, color, glow, sw)
    neonPath(handlePath, color, glow, sw)

    // Wheels
    val wheelY = cy + r * 0.56f
    val wR = r * 0.092f
    neonCircle(Offset(cx - r * 0.22f, wheelY), wR, color, glow)
    neonCircle(Offset(cx + r * 0.22f, wheelY), wR, color, glow)
}

// ─────────────────────────────────────────────
//  VAULT — wireframe trophy cup
// ─────────────────────────────────────────────

private fun DrawScope.drawTrophy(
    cx: Float, cy: Float, r: Float, color: Color, glow: Float
) {
    val sw = r * 0.09f

    // Cup + handles + stem + base in one path (multiple sub-paths = disconnected wireframe)
    val cupPath = Path().apply {
        val cL = cx - r * 0.42f
        val cR = cx + r * 0.42f
        val cTop = cy - r * 0.56f
        val hOut = r * 0.22f   // handle extension outward
        val hTop = cTop + r * 0.08f
        val hBot = cTop + r * 0.36f

        // ── Cup body ──
        // Left side
        moveTo(cL, cTop)
        lineTo(cL, cy + r * 0.12f)
        // Curved bottom
        quadraticTo(cL,  cy + r * 0.38f, cx, cy + r * 0.44f)
        quadraticTo(cR,  cy + r * 0.38f, cR, cy + r * 0.12f)
        // Right side
        lineTo(cR, cTop)
        // Top rim
        lineTo(cL, cTop)

        // ── Left handle ──
        moveTo(cL, hTop)
        lineTo(cL - hOut, hTop)
        lineTo(cL - hOut, hBot)
        lineTo(cL, hBot)

        // ── Right handle ──
        moveTo(cR, hTop)
        lineTo(cR + hOut, hTop)
        lineTo(cR + hOut, hBot)
        lineTo(cR, hBot)

        // ── Stem ──
        val sL = cx - r * 0.13f
        val sR = cx + r * 0.13f
        moveTo(sL, cy + r * 0.44f)
        lineTo(sL, cy + r * 0.65f)
        lineTo(sR, cy + r * 0.65f)
        lineTo(sR, cy + r * 0.44f)

        // ── Base ──
        moveTo(cx - r * 0.38f, cy + r * 0.65f)
        lineTo(cx + r * 0.38f, cy + r * 0.65f)
    }

    neonPath(cupPath, color, glow, sw)

    // Star at top center (tiny cross)
    if (glow > 0.01f) {
        val starPath = Path().apply {
            moveTo(cx, cy - r * 0.56f - r * 0.14f)
            lineTo(cx, cy - r * 0.56f + r * 0.14f)
            moveTo(cx - r * 0.10f, cy - r * 0.56f)
            lineTo(cx + r * 0.10f, cy - r * 0.56f)
        }
        neonPath(starPath, color, glow * 0.6f, sw * 0.7f)
    }
}

// ─────────────────────────────────────────────
//  FIRE — neon flame
// ─────────────────────────────────────────────

private fun DrawScope.drawFire(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val flamePath = Path().apply {
        moveTo(cx, cy + r * 0.6f)
        quadraticTo(cx - r * 0.5f, cy + r * 0.5f, cx - r * 0.4f, cy)
        quadraticTo(cx - r * 0.2f, cy - r * 0.3f, cx, cy - r * 0.6f)
        quadraticTo(cx + r * 0.1f, cy - r * 0.2f, cx + r * 0.2f, cy - r * 0.1f)
        quadraticTo(cx + r * 0.5f, cy + r * 0.2f, cx + r * 0.4f, cy + r * 0.5f)
        quadraticTo(cx + r * 0.2f, cy + r * 0.6f, cx, cy + r * 0.6f)
    }
    neonPath(flamePath, color, glow, sw)
    
    val innerFlame = Path().apply {
        moveTo(cx, cy + r * 0.4f)
        quadraticTo(cx - r * 0.2f, cy + r * 0.3f, cx - r * 0.1f, cy + r * 0.1f)
        quadraticTo(cx, cy - r * 0.1f, cx + r * 0.1f, cy + r * 0.1f)
        quadraticTo(cx + r * 0.2f, cy + r * 0.3f, cx, cy + r * 0.4f)
    }
    neonPath(innerFlame, color, glow * 0.8f, sw)
}

// ─────────────────────────────────────────────
//  PARTY — neon party popper
// ─────────────────────────────────────────────

private fun DrawScope.drawPartyPopper(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val conePath = Path().apply {
        moveTo(cx - r * 0.4f, cy + r * 0.5f)
        lineTo(cx + r * 0.1f, cy)
        lineTo(cx - r * 0.1f, cy - r * 0.2f)
        close()
    }
    neonPath(conePath, color, glow, sw)
    
    // Confetti lines shooting out
    val c1 = Path().apply { moveTo(cx, cy - r * 0.3f); lineTo(cx + r * 0.3f, cy - r * 0.6f) }
    val c2 = Path().apply { moveTo(cx + r * 0.2f, cy - r * 0.1f); lineTo(cx + r * 0.5f, cy - r * 0.3f) }
    val c3 = Path().apply { moveTo(cx + r * 0.3f, cy + r * 0.1f); lineTo(cx + r * 0.6f, cy) }
    
    neonPath(c1, color, glow, sw)
    neonPath(c2, color, glow, sw)
    neonPath(c3, color, glow, sw)
}

// ─────────────────────────────────────────────
//  STAR — 5-point neon star
// ─────────────────────────────────────────────

private fun DrawScope.drawStar(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val starPath = Path().apply {
        val outR = r * 0.6f
        val inR = r * 0.25f
        for (i in 0 until 10) {
            val angle = Math.PI / 2 + i * Math.PI / 5
            val curR = if (i % 2 == 0) outR else inR
            val px = cx + (curR * Math.cos(angle)).toFloat()
            val py = cy - (curR * Math.sin(angle)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    neonPath(starPath, color, glow, sw)
}

// ─────────────────────────────────────────────
//  LOCK — neon padlock
// ─────────────────────────────────────────────

private fun DrawScope.drawLock(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    // Body
    val bodyPath = Path().apply {
        val w = r * 0.4f
        val h = r * 0.35f
        moveTo(cx - w, cy - r * 0.05f)
        lineTo(cx + w, cy - r * 0.05f)
        lineTo(cx + w, cy + h)
        lineTo(cx - w, cy + h)
        close()
    }
    // Shackle
    val shacklePath = Path().apply {
        val w = r * 0.25f
        moveTo(cx - w, cy - r * 0.05f)
        lineTo(cx - w, cy - r * 0.25f)
        quadraticTo(cx - w, cy - r * 0.5f, cx, cy - r * 0.5f)
        quadraticTo(cx + w, cy - r * 0.5f, cx + w, cy - r * 0.25f)
        lineTo(cx + w, cy - r * 0.05f)
    }
    // Keyhole
    val keyholePath = Path().apply {
        moveTo(cx, cy + r * 0.05f)
        lineTo(cx, cy + r * 0.2f)
    }
    
    neonPath(bodyPath, color, glow, sw)
    neonPath(shacklePath, color, glow, sw)
    neonPath(keyholePath, color, glow * 0.8f, sw * 1.5f)
}

// ─────────────────────────────────────────────
//  GEAR — neon settings icon
// ─────────────────────────────────────────────

private fun DrawScope.drawGear(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val gearPath = Path().apply {
        val teeth = 8
        val outR = r * 0.5f
        val inR = r * 0.35f
        for (i in 0 until teeth * 2) {
            val angle = i * Math.PI / teeth
            val curR = if (i % 2 == 0) outR else inR
            val nextR = if (i % 2 == 0) outR else inR
            val px1 = cx + (curR * Math.cos(angle)).toFloat()
            val py1 = cy - (curR * Math.sin(angle)).toFloat()
            val nextAngle = (i + 0.5) * Math.PI / teeth
            val px2 = cx + (nextR * Math.cos(nextAngle)).toFloat()
            val py2 = cy - (nextR * Math.sin(nextAngle)).toFloat()
            if (i == 0) moveTo(px1, py1) else lineTo(px1, py1)
            lineTo(px2, py2)
        }
        close()
    }
    neonPath(gearPath, color, glow, sw)
    neonCircle(Offset(cx, cy), r * 0.15f, color, glow)
}

// ─────────────────────────────────────────────
//  NEW SHOP ICONS
// ─────────────────────────────────────────────

private fun DrawScope.drawTap(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val path = Path().apply {
        moveTo(cx, cy + r * 0.5f)
        lineTo(cx, cy - r * 0.4f)
        quadraticTo(cx, cy - r * 0.6f, cx + r * 0.2f, cy - r * 0.4f)
        lineTo(cx + r * 0.2f, cy + r * 0.2f)
        // additional fingers
        lineTo(cx + r * 0.4f, cy + r * 0.2f)
        lineTo(cx + r * 0.4f, cy + r * 0.5f)
        lineTo(cx - r * 0.3f, cy + r * 0.5f)
        close()
    }
    neonPath(path, color, glow, sw)
    neonCircle(Offset(cx, cy - r * 0.8f), r * 0.1f, color, glow)
}

private fun DrawScope.drawLike(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val path = Path().apply {
        moveTo(cx - r * 0.4f, cy + r * 0.4f)
        lineTo(cx - r * 0.4f, cy - r * 0.1f)
        lineTo(cx - r * 0.1f, cy - r * 0.1f)
        lineTo(cx - r * 0.1f, cy - r * 0.7f)
        quadraticTo(cx + r * 0.2f, cy - r * 0.7f, cx + r * 0.2f, cy - r * 0.2f)
        lineTo(cx + r * 0.5f, cy - r * 0.2f)
        lineTo(cx + r * 0.5f, cy + r * 0.4f)
        close()
    }
    neonPath(path, color, glow, sw)
    neonPath(Path().apply { moveTo(cx - r * 0.1f, cy + r * 0.4f); lineTo(cx - r * 0.1f, cy - r * 0.1f) }, color, glow, sw)
}

private fun DrawScope.drawGlove(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val path = Path().apply {
        moveTo(cx - r * 0.3f, cy + r * 0.5f)
        lineTo(cx + r * 0.3f, cy + r * 0.5f)
        lineTo(cx + r * 0.4f, cy - r * 0.2f)
        quadraticTo(cx, cy - r * 0.6f, cx - r * 0.4f, cy - r * 0.2f)
        close()
    }
    neonPath(path, color, glow, sw)
    // Cuff
    neonPath(Path().apply {
        moveTo(cx - r * 0.4f, cy + r * 0.5f)
        lineTo(cx + r * 0.4f, cy + r * 0.5f)
        lineTo(cx + r * 0.4f, cy + r * 0.7f)
        lineTo(cx - r * 0.4f, cy + r * 0.7f)
        close()
    }, color, glow, sw)
}

private fun DrawScope.drawBrain(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val path = Path().apply {
        moveTo(cx - r * 0.5f, cy)
        quadraticTo(cx - r * 0.6f, cy - r * 0.5f, cx, cy - r * 0.6f)
        quadraticTo(cx + r * 0.6f, cy - r * 0.5f, cx + r * 0.5f, cy)
        quadraticTo(cx + r * 0.6f, cy + r * 0.4f, cx, cy + r * 0.5f)
        quadraticTo(cx - r * 0.6f, cy + r * 0.4f, cx - r * 0.5f, cy)
    }
    neonPath(path, color, glow, sw)
    // Hemisphere split
    neonPath(Path().apply {
        moveTo(cx, cy - r * 0.6f)
        quadraticTo(cx + r * 0.1f, cy, cx, cy + r * 0.5f)
    }, color, glow, sw)
}

private fun DrawScope.drawAtom(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val orbit1 = Path().apply { addOval(androidx.compose.ui.geometry.Rect(cx - r * 0.6f, cy - r * 0.2f, cx + r * 0.6f, cy + r * 0.2f)) }
    
    // Quick rotation for orbits using rotate modifier or draw locally
    // Since Path doesn't have an easy rotate without Matrix, let's draw ellipses roughly manually or use drawOval
    drawOval(color, topLeft = Offset(cx - r * 0.6f, cy - r * 0.2f), size = Size(r * 1.2f, r * 0.4f), style = Stroke(sw))
    if (glow > 0) drawOval(color.copy(alpha=glow*0.3f), topLeft = Offset(cx - r * 0.6f, cy - r * 0.2f), size = Size(r * 1.2f, r * 0.4f), style = Stroke(sw*3))
    
    // We'll just draw a central nucleus and outer particles
    neonCircle(Offset(cx, cy), r * 0.15f, color, glow)
    neonCircle(Offset(cx - r * 0.6f, cy), r * 0.08f, color, glow)
    neonCircle(Offset(cx + r * 0.6f, cy), r * 0.08f, color, glow)
    neonCircle(Offset(cx, cy - r * 0.5f), r * 0.08f, color, glow)
    neonCircle(Offset(cx, cy + r * 0.5f), r * 0.08f, color, glow)
}

private fun DrawScope.drawBot(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val head = Path().apply {
        moveTo(cx - r * 0.4f, cy - r * 0.2f)
        lineTo(cx + r * 0.4f, cy - r * 0.2f)
        lineTo(cx + r * 0.4f, cy + r * 0.4f)
        lineTo(cx - r * 0.4f, cy + r * 0.4f)
        close()
    }
    neonPath(head, color, glow, sw)
    // Eyes
    neonPath(Path().apply { moveTo(cx - r * 0.2f, cy); lineTo(cx - r * 0.1f, cy) }, color, glow, sw * 1.5f)
    neonPath(Path().apply { moveTo(cx + r * 0.1f, cy); lineTo(cx + r * 0.2f, cy) }, color, glow, sw * 1.5f)
    // Antenna
    neonPath(Path().apply { moveTo(cx, cy - r * 0.2f); lineTo(cx, cy - r * 0.5f) }, color, glow, sw)
    neonCircle(Offset(cx, cy - r * 0.6f), r * 0.1f, color, glow)
}

private fun DrawScope.drawChart(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    // Axes
    neonPath(Path().apply {
        moveTo(cx - r * 0.5f, cy - r * 0.5f)
        lineTo(cx - r * 0.5f, cy + r * 0.5f)
        lineTo(cx + r * 0.5f, cy + r * 0.5f)
    }, color, glow, sw)
    // Bars
    neonPath(Path().apply { moveTo(cx - r * 0.3f, cy + r * 0.5f); lineTo(cx - r * 0.3f, cy + r * 0.1f) }, color, glow, sw * 3)
    neonPath(Path().apply { moveTo(cx, cy + r * 0.5f); lineTo(cx, cy - r * 0.2f) }, color, glow, sw * 3)
    neonPath(Path().apply { moveTo(cx + r * 0.3f, cy + r * 0.5f); lineTo(cx + r * 0.3f, cy - r * 0.5f) }, color, glow, sw * 3)
}

private fun DrawScope.drawFactory(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val path = Path().apply {
        moveTo(cx - r * 0.5f, cy + r * 0.5f)
        lineTo(cx - r * 0.5f, cy - r * 0.1f)
        lineTo(cx - r * 0.1f, cy - r * 0.4f)
        lineTo(cx - r * 0.1f, cy - r * 0.1f)
        lineTo(cx + r * 0.3f, cy - r * 0.4f)
        lineTo(cx + r * 0.3f, cy - r * 0.1f)
        lineTo(cx + r * 0.5f, cy - r * 0.1f)
        lineTo(cx + r * 0.5f, cy + r * 0.5f)
        close()
    }
    neonPath(path, color, glow, sw)
    // Smoke stacks
    neonPath(Path().apply { moveTo(cx + r * 0.35f, cy - r * 0.1f); lineTo(cx + r * 0.35f, cy - r * 0.6f) }, color, glow, sw)
    neonPath(Path().apply { moveTo(cx + r * 0.45f, cy - r * 0.1f); lineTo(cx + r * 0.45f, cy - r * 0.5f) }, color, glow, sw)
}

private fun DrawScope.drawOrbit(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    neonCircle(Offset(cx, cy), r * 0.2f, color, glow)
    val ring1 = Path().apply { addOval(androidx.compose.ui.geometry.Rect(cx - r * 0.7f, cy - r * 0.3f, cx + r * 0.7f, cy + r * 0.3f)) }
    neonPath(ring1, color, glow, sw)
    // planet features
    neonCircle(Offset(cx - r * 0.5f, cy + r * 0.2f), r * 0.1f, color, glow)
    neonCircle(Offset(cx + r * 0.3f, cy - r * 0.25f), r * 0.05f, color, glow)
}

private fun DrawScope.drawCoin(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    neonCircle(Offset(cx, cy), r * 0.6f, color, glow)
    neonCircle(Offset(cx, cy), r * 0.4f, color, glow * 0.5f)
    // Inner symbol
    neonPath(Path().apply {
        moveTo(cx, cy - r * 0.2f)
        lineTo(cx, cy + r * 0.2f)
        moveTo(cx - r * 0.1f, cy - r * 0.1f)
        lineTo(cx + r * 0.1f, cy - r * 0.1f)
        moveTo(cx - r * 0.1f, cy + r * 0.1f)
        lineTo(cx + r * 0.1f, cy + r * 0.1f)
    }, color, glow, sw)
}

private fun DrawScope.drawGem(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val path = Path().apply {
        moveTo(cx - r * 0.3f, cy - r * 0.4f)
        lineTo(cx + r * 0.3f, cy - r * 0.4f)
        lineTo(cx + r * 0.6f, cy - r * 0.1f)
        lineTo(cx, cy + r * 0.6f)
        lineTo(cx - r * 0.6f, cy - r * 0.1f)
        close()
    }
    neonPath(path, color, glow, sw)
    // Inner facets
    neonPath(Path().apply {
        moveTo(cx - r * 0.3f, cy - r * 0.4f); lineTo(cx - r * 0.2f, cy - r * 0.1f); lineTo(cx, cy + r * 0.6f)
        moveTo(cx + r * 0.3f, cy - r * 0.4f); lineTo(cx + r * 0.2f, cy - r * 0.1f); lineTo(cx, cy + r * 0.6f)
        moveTo(cx - r * 0.6f, cy - r * 0.1f); lineTo(cx + r * 0.6f, cy - r * 0.1f)
    }, color, glow, sw * 0.7f)
}

private fun DrawScope.drawLightning(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val path = Path().apply {
        moveTo(cx + r * 0.1f, cy - r * 0.6f)
        lineTo(cx - r * 0.3f, cy + r * 0.1f)
        lineTo(cx + r * 0.1f, cy + r * 0.1f)
        lineTo(cx - r * 0.1f, cy + r * 0.6f)
        lineTo(cx + r * 0.4f, cy - r * 0.1f)
        lineTo(cx, cy - r * 0.1f)
        close()
    }
    neonPath(path, color, glow, sw)
}

private fun DrawScope.drawTV(cx: Float, cy: Float, r: Float, color: Color, glow: Float) {
    val sw = r * 0.09f
    val box = Path().apply {
        moveTo(cx - r * 0.5f, cy - r * 0.3f)
        lineTo(cx + r * 0.5f, cy - r * 0.3f)
        lineTo(cx + r * 0.5f, cy + r * 0.4f)
        lineTo(cx - r * 0.5f, cy + r * 0.4f)
        close()
    }
    neonPath(box, color, glow, sw)
    // Antenna
    neonPath(Path().apply {
        moveTo(cx - r * 0.2f, cy - r * 0.6f); lineTo(cx, cy - r * 0.3f); lineTo(cx + r * 0.3f, cy - r * 0.6f)
    }, color, glow, sw)
    // Screen play button
    neonPath(Path().apply {
        moveTo(cx - r * 0.1f, cy - r * 0.1f)
        lineTo(cx + r * 0.2f, cy + r * 0.05f)
        lineTo(cx - r * 0.1f, cy + r * 0.2f)
        close()
    }, color, glow, sw)
}
