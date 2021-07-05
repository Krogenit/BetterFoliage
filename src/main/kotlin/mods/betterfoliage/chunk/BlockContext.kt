package mods.betterfoliage.chunk

import mods.betterfoliage.util.Int3
import mods.betterfoliage.util.offset
import mods.betterfoliage.util.plus
import mods.betterfoliage.util.semiRandom
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.chunk.ChunkRenderCache
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockDisplayReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.biome.Biome
import net.minecraft.world.level.ColorResolver

/**
 * Represents the block being rendered. Has properties and methods to query the neighborhood of the block in
 * block-relative coordinates.
 */
interface BlockCtx {
    val world: IBlockDisplayReader
    val pos: BlockPos

    val seed: Long get() = state.getSeed(pos)

    fun offset(dir: Direction) = offset(dir.offset)
    fun offset(offset: Int3): BlockCtx

    val state: BlockState get() = world.getBlockState(pos)
    fun state(offset: Int3) = world.getBlockState(pos + offset)
    fun state(dir: Direction) = state(dir.offset)

    fun isAir(offset: Int3) = (pos + offset).let { world.getBlockState(it).isAir(world, it) }
    fun isAir(dir: Direction) = isAir(dir.offset)

    val biome: Biome? get() =
        (world as? IWorldReader)?.getBiome(pos) ?:
        (world as? ChunkRenderCache)?.level?.getBiome(pos)

    val isFullBlock: Boolean get() = state.isCollisionShapeFullBlock(world, pos)

    fun isNeighborSturdy(dir: Direction) = offset(dir).let { it.state.isFaceSturdy(it.world, it.pos, dir.opposite) }

    fun shouldSideBeRendered(side: Direction) = Block.shouldRenderFace(state, world, pos, side)

    /** Get a semi-random value based on the block coordinate and the given seed. */
    fun semiRandom(seed: Int) = pos.semiRandom(seed)

    /** Get an array of semi-random values based on the block coordinate. */
    fun semiRandomArray(num: Int): Array<Int> = Array(num) { semiRandom(it) }

    fun color(resolver: ColorResolver) = world.getBlockTint(pos, resolver)
}

class BasicBlockCtx(
    override val world: IBlockDisplayReader,
    override val pos: BlockPos
) : BlockCtx {
    override val state: BlockState = world.getBlockState(pos)
    override fun offset(offset: Int3) = BasicBlockCtx(world, pos + offset)
}
