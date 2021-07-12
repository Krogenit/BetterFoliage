package mods.betterfoliage

import mods.betterfoliage.chunk.ChunkOverlayManager
import mods.betterfoliage.config.BlockConfig
import mods.betterfoliage.integration.OptifineCustomColors
import mods.betterfoliage.integration.ShadersModIntegration
import mods.betterfoliage.render.block.vanilla.RoundLogOverlayLayer
import mods.betterfoliage.render.block.vanilla.StandardCactusDiscovery
import mods.betterfoliage.render.block.vanilla.StandardCactusModel
import mods.betterfoliage.render.block.vanilla.StandardDirtDiscovery
import mods.betterfoliage.render.block.vanilla.StandardDirtModel
import mods.betterfoliage.render.block.vanilla.StandardGrassDiscovery
import mods.betterfoliage.render.block.vanilla.StandardGrassModel
import mods.betterfoliage.render.block.vanilla.StandardLeafDiscovery
import mods.betterfoliage.render.block.vanilla.StandardLeafModel
import mods.betterfoliage.render.block.vanilla.StandardLilypadDiscovery
import mods.betterfoliage.render.block.vanilla.StandardLilypadModel
import mods.betterfoliage.render.block.vanilla.StandardMyceliumDiscovery
import mods.betterfoliage.render.block.vanilla.StandardMyceliumModel
import mods.betterfoliage.render.block.vanilla.StandardNetherrackDiscovery
import mods.betterfoliage.render.block.vanilla.StandardNetherrackModel
import mods.betterfoliage.render.block.vanilla.StandardRoundLogDiscovery
import mods.betterfoliage.render.block.vanilla.StandardRoundLogModel
import mods.betterfoliage.render.block.vanilla.StandardSandDiscovery
import mods.betterfoliage.render.block.vanilla.StandardSandModel
import mods.betterfoliage.render.lighting.AoSideHelper
import mods.betterfoliage.render.particle.LeafParticleRegistry
import mods.betterfoliage.render.particle.LeafWindTracker
import mods.betterfoliage.render.particle.RisingSoulParticle
import mods.betterfoliage.resource.discovery.BakeWrapperManager
import mods.betterfoliage.resource.discovery.BlockTypeCache
import mods.betterfoliage.resource.discovery.RuleBasedDiscovery
import mods.betterfoliage.resource.generated.GeneratedTexturePack
import mods.betterfoliage.util.resourceManager
import net.minecraft.block.BlockState

/**
 * Object responsible for initializing (and holding a reference to) all the infrastructure of the mod
 * except for the call hooks.
 */
object BetterFoliage {
    /** Resource pack holding generated assets */
    val generatedPack = GeneratedTexturePack("bf_gen", "Better Foliage generated assets")

    /** List of recognized [BlockState]s */
    var blockTypes = BlockTypeCache()

    val blockConfig = BlockConfig()

    val standardModelSupport = RuleBasedDiscovery().apply {
        discoverers["cactus"] = StandardCactusDiscovery
        discoverers["dirt"] = StandardDirtDiscovery
        discoverers["grass"] = StandardGrassDiscovery
        discoverers["leaf"] = StandardLeafDiscovery
        discoverers["lilypad"] = StandardLilypadDiscovery
        discoverers["mycelium"] = StandardMyceliumDiscovery
        discoverers["netherrack"] = StandardNetherrackDiscovery
        discoverers["round-log"] = StandardRoundLogDiscovery
        discoverers["sand"] = StandardSandDiscovery
    }
    val modelManager = BakeWrapperManager().apply {
        discoverers.add(standardModelSupport)
    }

    fun init() {
        // discoverers
        BetterFoliageMod.bus.register(modelManager)
        BetterFoliageMod.bus.register(LeafParticleRegistry)
        resourceManager.registerReloadListener(LeafParticleRegistry)

        ChunkOverlayManager.layers.add(RoundLogOverlayLayer)

        listOf(
            StandardLeafDiscovery,
            StandardSandDiscovery,
            StandardRoundLogDiscovery,
        ).forEach {
        }

        // init singletons
        val singletons = listOf(
            AoSideHelper,
            ChunkOverlayManager,
            LeafWindTracker
        )

        val modelSingletons = listOf(
            StandardLeafModel.Companion,
            StandardGrassModel.Companion,
            StandardDirtModel.Companion,
            StandardMyceliumModel.Companion,
            StandardSandModel.Companion,
            StandardLilypadModel.Companion,
            StandardCactusModel.Companion,
            StandardNetherrackModel.Companion,
            StandardRoundLogModel.Companion,
            RisingSoulParticle.Companion
        )

        // init mod integrations
        val integrations = listOf(
            ShadersModIntegration,
            OptifineCustomColors
        )
    }
}

