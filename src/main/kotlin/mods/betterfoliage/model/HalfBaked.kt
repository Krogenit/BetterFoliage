package mods.betterfoliage.model

import mods.betterfoliage.chunk.BlockCtx
import mods.betterfoliage.render.pipeline.RenderCtxBase
import mods.betterfoliage.render.pipeline.WrappedLayerPredicate
import mods.betterfoliage.render.pipeline.layerPredicate
import mods.betterfoliage.resource.discovery.ModelBakingContext
import mods.betterfoliage.resource.discovery.ModelBakingKey
import mods.betterfoliage.util.Double3
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.directionsAndNull
import mods.betterfoliage.util.mapArray
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.RenderTypeLookup
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.model.IBakedModel
import net.minecraft.client.renderer.model.SimpleBakedModel
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexFormatElement
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder
import java.util.Random

/**
 * Hybrid baked quad implementation, carrying both baked and unbaked information.
 * Used to do advanced vertex lighting without unbaking vertex data at lighting time.
 */
data class HalfBakedQuad(
    val raw: Quad,
    val baked: BakedQuad
)

open class HalfBakedSimpleModelWrapper(baseModel: SimpleBakedModel): IBakedModel by baseModel, SpecialRenderModel {
    val baseQuads = baseModel.unbakeQuads()

    override fun prepare(ctx: BlockCtx, random: Random) = Unit

    override fun renderLayer(ctx: RenderCtxBase, data: Any, layer: RenderType) {
        // if the passed data is a BlockState, render on the same layer(s) as that block
        val testState = (data as? BlockState) ?: ctx.state

        // this could get called for more layers than the underlying model is on
        // ignore extra decoration layers
        val shouldRender = when(val predicate = testState.block.layerPredicate) {
            is WrappedLayerPredicate -> predicate.original.test(layer)
            else -> RenderTypeLookup.canRenderInLayer(testState, layer)
        }
        if (shouldRender) ctx.renderQuads(baseQuads)
    }
}

open class HalfBakedSpecialWrapper(val baseModel: SpecialRenderModel): SpecialRenderModel by baseModel {
}

abstract class HalfBakedWrapperKey : ModelBakingKey, HasLogger() {
    override fun bake(ctx: ModelBakingContext): IBakedModel? {
        val baseModel = super.bake(ctx)
        val halfBaked = when(baseModel) {
            is SimpleBakedModel -> HalfBakedSimpleModelWrapper(baseModel)
            else -> null
        }
        return if (halfBaked == null) baseModel else bake(ctx, halfBaked)
    }

    abstract fun bake(ctx: ModelBakingContext, wrapped: SpecialRenderModel): SpecialRenderModel
}

fun List<Quad>.bake(applyDiffuseLighting: Boolean) = map { quad ->
    if (quad.sprite == null) throw IllegalStateException("Quad must have a texture assigned before baking")
    val builder = BakedQuadBuilder(quad.sprite)
    builder.setApplyDiffuseLighting(applyDiffuseLighting)
    builder.setQuadOrientation(quad.face())
    builder.setQuadTint(quad.colorIndex)
    quad.verts.forEach { vertex ->
        DefaultVertexFormats.BLOCK.elements.forEachIndexed { idx, element ->
            when {
                element.usage == VertexFormatElement.Usage.POSITION -> builder.put(idx,
                    (vertex.xyz.x + 0.5).toFloat(),
                    (vertex.xyz.y + 0.5).toFloat(),
                    (vertex.xyz.z + 0.5).toFloat(),
                    1.0f
                )
                // don't fill lightmap UV coords
                element.usage == VertexFormatElement.Usage.UV && element.type == VertexFormatElement.Type.FLOAT -> builder.put(idx,
                    quad.sprite.u0 + (quad.sprite.u1 - quad.sprite.u0) * (vertex.uv.u + 0.5).toFloat(),
                    quad.sprite.v0 + (quad.sprite.v1 - quad.sprite.v0) * (vertex.uv.v + 0.5).toFloat(),
                    0.0f, 1.0f
                )
                element.usage == VertexFormatElement.Usage.COLOR -> builder.put(idx,
                    (vertex.color.red and 255).toFloat() / 255.0f,
                    (vertex.color.green and 255).toFloat() / 255.0f,
                    (vertex.color.blue and 255).toFloat() / 255.0f,
                    (vertex.color.alpha and 255).toFloat() / 255.0f
                )
                element.usage == VertexFormatElement.Usage.NORMAL -> builder.put(idx,
                    (vertex.normal ?: quad.normal).x.toFloat(),
                    (vertex.normal ?: quad.normal).y.toFloat(),
                    (vertex.normal ?: quad.normal).z.toFloat(),
                    0.0f
                )
                else -> builder.put(idx)
            }
        }
    }
    HalfBakedQuad(quad, builder.build())
}

fun Array<List<Quad>>.bake(applyDiffuseLighting: Boolean) = mapArray { it.bake(applyDiffuseLighting) }

fun BakedQuad.unbake(): HalfBakedQuad {
    val size = DefaultVertexFormats.BLOCK.integerSize
    val verts = Array(4) { vIdx ->
        val x = java.lang.Float.intBitsToFloat(vertices[vIdx * size + 0]) - 0.5f
        val y = java.lang.Float.intBitsToFloat(vertices[vIdx * size + 1]) - 0.5f
        val z = java.lang.Float.intBitsToFloat(vertices[vIdx * size + 2]) - 0.5f
        val color = vertices[vIdx * size + 3]
        val u = java.lang.Float.intBitsToFloat(vertices[vIdx * size + 4])
        val v = java.lang.Float.intBitsToFloat(vertices[vIdx * size + 5])
        Vertex(Double3(x, y, z), UV(u.toDouble(), v.toDouble()), Color(color))
    }
    val unbaked = Quad(
        verts[0], verts[1], verts[2], verts[3],
        colorIndex = if (isTinted) tintIndex else -1,
        face = direction
    )
    return HalfBakedQuad(unbaked, this)
}

fun SimpleBakedModel.unbakeQuads() = directionsAndNull.flatMap { face ->
    getQuads(null, face, Random()).map { it.unbake() }
}

