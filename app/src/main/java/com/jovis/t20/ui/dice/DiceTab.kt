package com.jovis.t20.ui.dice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.jovis.t20.ui.sheet.TWO_PANE_MIN_WIDTH
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import com.jovis.t20.ui.sheet.CollapsibleSection
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jovis.t20.rules.Attribute
import com.jovis.t20.rules.DiceFormula
import com.jovis.t20.rules.DiceRoll
import com.jovis.t20.rules.DiceRoller
import com.jovis.t20.rules.DiceTerm
import com.jovis.t20.rules.ManualSheet
import com.jovis.t20.rules.RollKind
import com.jovis.t20.rules.RollRecord
import com.jovis.t20.rules.SheetEngine
import com.jovis.t20.rules.SheetRolls
import com.jovis.t20.rules.SheetWeapon
import com.jovis.t20.rules.Skill
import com.jovis.t20.rules.TestRoll
import com.jovis.t20.ui.theme.T20Fonts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Aba de rolagens: dado animado no topo, rolagens rápidas da ficha, dados livres e histórico.
 * [animatedRollId] fica guardado fora desta aba, para a animação não repetir ao trocar de aba.
 */
@Composable
fun DiceTab(
    sheet: ManualSheet,
    rolls: List<RollRecord>,
    roller: DiceRoller,
    onRoll: (RollRecord) -> Unit,
    animatedRollId: Long,
    onAnimated: (Long) -> Unit,
) {
    val last = rolls.firstOrNull()
    val roll: (RollRecord?) -> Unit = { it?.let(onRoll) }

    // Seções abertas, guardadas como texto para sobreviver à rotação e à troca de aba
    var openNames by rememberSaveable { mutableStateOf("") }
    val open = openNames.split(",").filter { it.isNotBlank() }.toSet()
    fun toggle(section: DiceSection) {
        val name = section.name
        openNames = (if (name in open) open - name else open + name).joinToString(",")
    }

    // A cada rolagem nova, volta ao topo para mostrar o dado animado (só no layout de uma coluna,
    // porque no de duas a bandeja está sempre à vista)
    val listState = rememberLazyListState()
    LaunchedEffect(last?.id) {
        if (last != null && last.id != animatedRollId) listState.animateScrollToItem(0)
    }

    // Pedaços da aba, usados tanto no layout de uma coluna quanto no de duas
    val stage: @Composable () -> Unit = {
        DiceStage(sheet, last, animatedRollId, onAnimated) { weapon, critical ->
            roll(SheetRolls.damage(roller, sheet, weapon, critical))
        }
    }
    // Seções recolhíveis, iguais às da ficha
    val sectionsBlock: @Composable () -> Unit = {
        Column {
            val sections = listOf(
                DiceSection.DADOS_LIVRES to "d4 a d100",
                DiceSection.ATAQUES to if (sheet.weapons.isEmpty()) "Nenhum" else countText(sheet.weapons.size, "ataque", "ataques"),
                DiceSection.PERICIAS to "${sheet.trainedSkills.size} treinadas",
                DiceSection.ATRIBUTOS to "",
                DiceSection.HISTORICO to if (rolls.isEmpty()) "Vazio" else countText(rolls.size, "rolagem", "rolagens"),
            )
            sections.forEachIndexed { index, (section, summary) ->
                val stripe = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent
                Box(Modifier.background(stripe)) {
                    CollapsibleSection(
                        title = section.title,
                        summary = summary,
                        expanded = section.name in open,
                        onToggle = { toggle(section) },
                    ) {
                        when (section) {
                            DiceSection.ATAQUES -> AttackRolls(sheet, roller, roll)
                            DiceSection.PERICIAS -> SkillRolls(sheet, roller, roll)
                            DiceSection.ATRIBUTOS -> AttributeRolls(sheet, roller, roll)
                            DiceSection.DADOS_LIVRES -> FreeDice(roller) { roll(it) }
                            DiceSection.HISTORICO -> History(rolls)
                        }
                    }
                }
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= TWO_PANE_MIN_WIDTH) {
            // Tela larga (tablet, dobrável aberto): a bandeja à esquerda, sempre à vista,
            // e as seções à direita, cada coluna com sua própria rolagem
            Row(Modifier.fillMaxSize()) {
                Column(
                    Modifier.weight(0.5f).fillMaxHeight().verticalScroll(rememberScrollState())
                        .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 24.dp),
                ) { stage() }
                LazyColumn(
                    modifier = Modifier.weight(0.5f).fillMaxHeight(),
                    contentPadding = PaddingValues(start = 12.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
                ) {
                    item { sectionsBlock() }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = 720.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                item { stage() }
                item { sectionsBlock() }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

private enum class DiceSection(val title: String) {
    ATAQUES("Ataques"),
    PERICIAS("Perícias"),
    ATRIBUTOS("Atributos"),
    DADOS_LIVRES("Dados livres"),
    HISTORICO("Histórico"),
}

private fun countText(count: Int, singular: String, plural: String) = if (count == 1) "1 $singular" else "$count $plural"

// ============================== Conteúdo das seções ==============================

@Composable
private fun AttackRolls(sheet: ManualSheet, roller: DiceRoller, roll: (RollRecord?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (sheet.weapons.isEmpty()) {
            Hint("Nenhum ataque na ficha. Adicione em Editar.")
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sheet.weapons.forEach { weapon ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(weapon.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Ataque ${signed(SheetEngine.attackBonus(sheet, weapon).total)}, dano ${SheetEngine.damageText(sheet, weapon)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(onClick = { roll(SheetRolls.attack(roller, sheet, weapon)) }) { Text("Ataque") }
                    Spacer(Modifier.size(6.dp))
                    OutlinedButton(onClick = { roll(SheetRolls.damage(roller, sheet, weapon)) }) { Text("Dano") }
                }
            }
        }
    }
}

@Composable
private fun SkillRolls(sheet: ManualSheet, roller: DiceRoller, roll: (RollRecord?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Skill.entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pair.forEach { skill ->
                        val trained = SheetEngine.isTrained(sheet, skill)
                        val usable = SheetEngine.canUse(sheet, skill)
                        OutlinedButton(
                            onClick = { roll(SheetRolls.skill(roller, sheet, skill)) },
                            enabled = usable,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            border = BorderStroke(
                                1.dp,
                                if (trained) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            ),
                        ) {
                            Text(skill.displayName, modifier = Modifier.weight(1f), maxLines = 1)
                            Text(signed(SheetEngine.skill(sheet, skill).total), fontWeight = FontWeight.Bold)
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Hint("Borda vermelha: perícia treinada. Perícias só treinadas ficam desativadas se não houver treino.")
        }
    }
}

@Composable
private fun AttributeRolls(sheet: ManualSheet, roller: DiceRoller, roll: (RollRecord?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Attribute.entries.forEach { attribute ->
                OutlinedButton(
                    onClick = { roll(SheetRolls.attribute(roller, sheet, attribute)) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(attribute.abbreviation, fontSize = 12.sp)
                        Text(signed(SheetEngine.attribute(sheet, attribute)), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun History(rolls: List<RollRecord>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (rolls.isEmpty()) Hint("As rolagens desta sessão aparecem aqui.")
        Column {
            rolls.forEachIndexed { index, record ->
                val stripe = if (index % 2 == 0) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f) else Color.Transparent
                Row(
                    Modifier.fillMaxWidth().background(stripe).padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(record.label, style = MaterialTheme.typography.bodyLarge)
                        Text(record.breakdown, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        highlightOf(record)?.let { Text(it.first, style = MaterialTheme.typography.bodySmall, color = it.second()) }
                    }
                    Text(
                        "${record.total}",
                        fontFamily = T20Fonts.serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// ============================== Palco do dado ==============================

/** O dado grande: gira, quica e troca de número até parar no resultado. */
@Composable
private fun DiceStage(
    sheet: ManualSheet,
    roll: RollRecord?,
    animatedRollId: Long,
    onAnimated: (Long) -> Unit,
    onDamage: (SheetWeapon, Boolean) -> Unit,
) {
    // Anima só rolagens novas; ao voltar para a aba, o dado já aparece parado
    val animate = roll != null && roll.id != animatedRollId
    var settled by remember(roll?.id) { mutableStateOf(!animate) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(roll?.id) {
        if (roll != null && animate) onAnimated(roll.id)
    }

    val test = roll as? TestRoll
    // Todos os dados da rolagem vão para a bandeja, cada um parando no próprio valor
    val dice = when (roll) {
        is TestRoll -> listOf(TrayDie(20, roll.natural))
        is DiceRoll -> roll.results.map { (sides, value) -> TrayDie(sides, value) }
        null -> listOf(TrayDie(20, 20))
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            DiceTray(
                rollKey = roll?.id ?: 0L,
                dice = dice,
                natural20 = test?.isNatural20 == true,
                natural1 = test?.isNatural1 == true,
                animate = animate,
                onSettled = {
                    if (!settled && roll != null) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    settled = true
                },
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
            )
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (roll == null) {
                Text("Toque em uma rolagem abaixo", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                return@Column
            }
            Text(roll.label, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (settled) {
                Text(
                    "Total ${roll.total}",
                    fontFamily = T20Fonts.display,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(roll.breakdown, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                highlightOf(roll)?.let { (text, color) ->
                    Text(text, style = MaterialTheme.typography.titleSmall, color = color(), textAlign = TextAlign.Center)
                }
                // Depois de um ataque, o dano está a um toque
                weaponOf(sheet, roll)?.let { weapon ->
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (test?.isThreat == true) {
                            Button(onClick = { onDamage(weapon, true) }) { Text("Dano crítico (x${weapon.criticalMultiplier})") }
                            OutlinedButton(onClick = { onDamage(weapon, false) }) { Text("Dano normal") }
                        } else {
                            Button(onClick = { onDamage(weapon, false) }) { Text("Rolar dano") }
                        }
                    }
                }
            } else {
                Text("Rolando…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            }
        }
    }
}

/** A arma de um teste de ataque, para o palco oferecer o dano em seguida. */
private fun weaponOf(sheet: ManualSheet, roll: RollRecord): SheetWeapon? =
    if (roll is TestRoll && roll.kind == RollKind.ATTACK) sheet.weapons.firstOrNull { "Ataque: ${it.name}" == roll.label } else null


/** Destaques do resultado: 20 natural, 1 natural, ameaça de crítico e dano crítico. */
private fun highlightOf(roll: RollRecord): Pair<String, @Composable () -> Color>? = when {
    roll is TestRoll && roll.isNatural20 && roll.kind == RollKind.ATTACK ->
        "20 natural! Acerto automático e ameaça de crítico." to { MaterialTheme.colorScheme.primary }
    roll is TestRoll && roll.isNatural20 -> "20 natural! Sucesso automático." to { MaterialTheme.colorScheme.primary }
    roll is TestRoll && roll.isNatural1 -> "1 natural: falha automática." to { MaterialTheme.colorScheme.onSurfaceVariant }
    roll is TestRoll && roll.isThreat -> "Ameaça de crítico! Se acertar, role o dano crítico." to { MaterialTheme.colorScheme.primary }
    roll is DiceRoll && roll.critical -> "Crítico: dados multiplicados, bônus não." to { MaterialTheme.colorScheme.primary }
    else -> null
}

// ============================== Dados livres ==============================

@Composable
private fun FreeDice(roller: DiceRoller, onRoll: (RollRecord) -> Unit) {
    var sides by rememberSaveable { mutableStateOf(20) }
    var count by rememberSaveable { mutableStateOf(1) }
    var modifier by rememberSaveable { mutableStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(4, 6, 8, 10, 12, 20, 100).forEach { s ->
                val m = Modifier.weight(1f)
                val content: @Composable () -> Unit = { Text("d$s", fontSize = 13.sp, maxLines = 1) }
                if (s == sides) {
                    Button(onClick = { sides = s }, modifier = m, contentPadding = PaddingValues(2.dp)) { content() }
                } else {
                    OutlinedButton(onClick = { sides = s }, modifier = m, contentPadding = PaddingValues(2.dp)) { content() }
                }
            }
        }
        Stepper("Quantidade", count, 1..20) { count = it }
        Stepper("Modificador", modifier, -20..20, showSign = true) { modifier = it }
        val formula = DiceFormula(listOf(DiceTerm(count, sides)), modifier)
        Button(onClick = { onRoll(roller.roll(formula.text, formula, RollKind.FREE)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Rolar ${formula.text}")
        }
    }
}

@Composable
private fun Stepper(label: String, value: Int, range: IntRange, showSign: Boolean = false, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { onChange((value - 1).coerceIn(range)) }, enabled = value > range.first) { Text("−") }
        Text(
            if (showSign && value > 0) "+$value" else "$value",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 48.dp),
        )
        OutlinedButton(onClick = { onChange((value + 1).coerceIn(range)) }, enabled = value < range.last) { Text("+") }
    }
}

// ============================== Peças ==============================

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun signed(value: Int) = if (value >= 0) "+$value" else "$value"

// ============================== Ícones da barra ==============================

/** Ícone da aba Ficha: uma página com linhas. */
@Composable
fun SheetTabIcon(color: Color) {
    Canvas(Modifier.size(24.dp)) {
        val w = size.width; val h = size.height
        val s = Stroke(width = 2.dp.toPx())
        drawRoundRect(color, topLeft = Offset(w * 0.2f, h * 0.1f), size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.8f), style = s,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
        listOf(0.32f, 0.5f, 0.68f).forEach { y -> drawLine(color, Offset(w * 0.32f, h * y), Offset(w * 0.68f, h * y), strokeWidth = 2.dp.toPx()) }
    }
}

/** Ícone da aba Dados: um d20. */
@Composable
fun DiceTabIcon(color: Color) {
    Canvas(Modifier.size(24.dp)) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension / 2 * 0.9f
        val hex = List(6) { i ->
            val a = Math.toRadians(-90.0 + 60.0 * i)
            Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat())
        }
        val path = Path().apply { moveTo(hex[0].x, hex[0].y); hex.drop(1).forEach { lineTo(it.x, it.y) }; close() }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
        val inner = List(3) { i ->
            val a = Math.toRadians(-90.0 + 120.0 * i)
            Offset(c.x + r * 0.55f * cos(a).toFloat(), c.y + r * 0.55f * sin(a).toFloat())
        }
        val tri = Path().apply { moveTo(inner[0].x, inner[0].y); lineTo(inner[1].x, inner[1].y); lineTo(inner[2].x, inner[2].y); close() }
        drawPath(tri, color, style = Stroke(width = 1.5.dp.toPx()))
    }
}
