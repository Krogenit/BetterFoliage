@file:Suppress("NOTHING_TO_INLINE")
package mods.betterfoliage.util

import mods.betterfoliage.BetterFoliageMod
import net.minecraft.block.BlockState
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import java.lang.Math.*
import kotlin.reflect.KProperty

const val PI2 = 2.0 * PI

/** Strip the given prefix off the start of the string, if present */
inline fun String.stripStart(str: String, ignoreCase: Boolean = true) = if (startsWith(str, ignoreCase)) substring(str.length) else this
inline fun String.stripEnd(str: String, ignoreCase: Boolean = true) = if (endsWith(str, ignoreCase)) substring(0, length - str.length) else this

/** Strip the given prefix off the start of the resource path, if present */
inline fun ResourceLocation.stripStart(str: String) = ResourceLocation(namespace, path.stripStart(str))
inline fun ResourceLocation.stripEnd(str: String) = ResourceLocation(namespace, path.stripEnd(str))

val String.quoted: String get() = "\"$this\""

/**
 * Property-level delegate backed by a [ThreadLocal].
 *
 * @param[init] Lambda to get initial value
 */
class ThreadLocalDelegate<T>(init: () -> T) {
    var tlVal = ThreadLocal.withInitial(init)
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = tlVal.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { tlVal.set(value) }
}

fun <T> ThreadLocal<T?>.getWithDefault(factory: ()->T): T {
    get()?.let { return it }
    return factory().apply { set(this) }
}

/** Call the supplied lambda and return its result, or the given default value if an exception is thrown. */
fun <T> tryDefault(default: T, work: ()->T) = try { work() } catch (e: Throwable) { default }

/**
 * Return this [Double] value if it lies between the two limits. If outside, return the
 * minimum/maximum value correspondingly.
 */
fun Double.minmax(minVal: Double, maxVal: Double) = min(max(this, minVal), maxVal)

/**
 * Return this [Int] value if it lies between the two limits. If outside, return the
 * minimum/maximum value correspondingly.
 */
fun Int.minmax(minVal: Int, maxVal: Int) = min(max(this, minVal), maxVal)

fun nextPowerOf2(x: Int): Int {
    return 1 shl (if (x == 0) 0 else 32 - Integer.numberOfLeadingZeros(x - 1))
}

@Suppress("LeakingThis")
abstract class HasLogger {
    val logger = BetterFoliageMod.logger(this)
    val detailLogger = BetterFoliageMod.detailLogger(this)
}

fun getBlockModel(state: BlockState) = Minecraft.getInstance().blockRenderer.blockModelShaper.getBlockModel(state)
/**
 * Check if the Chunk containing the given [BlockPos] is loaded.
 * Works for both [World] and [ChunkCache] (vanilla and OptiFine) instances.
 */
//fun IWorldReader.isBlockLoaded(pos: BlockPos) = when {
//    this is World -> isBlockLoaded(pos, false)
//    this is RenderChunkCache -> isworld.isBlockLoaded(pos, false)
//    Refs.OptifineChunkCache.isInstance(this) -> (Refs.CCOFChunkCache.get(this) as ChunkCache).world.isBlockLoaded(pos, false)
//    else -> false
//}

//fun textComponent(msg: String, color: Formatting = Formatting.GRAY): LiteralText {
//    val style = Style().apply { this.color = color }
//    return LiteralText(msg).apply { this.style = style }
//}