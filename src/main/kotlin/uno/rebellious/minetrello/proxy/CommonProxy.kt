package uno.rebellious.minetrello.proxy

import net.minecraft.command.CommandHandler
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Level
import uno.rebellious.minetrello.MineTrello
import uno.rebellious.minetrello.commands.TrelloCommandHandler
import uno.rebellious.minetrello.config.GeneralConfig
import java.io.File


@Mod.EventBusSubscriber
open class CommonProxy {
    companion object {
        lateinit var config: Configuration
    }

    fun preInit(event: FMLPreInitializationEvent) {
        val configDir = event.modConfigurationDirectory
        config = Configuration(File(configDir.path, "MineTrell.cfg"))
        try {
            config.load()
            GeneralConfig.readConfig(config)
        } catch(e: Exception) {
            MineTrello.logger?.log(Level.ERROR, "Error Loading Config File", e)
        } finally {
            if (config.hasChanged()) {
                config.save()
            }
        }
    }

    fun postInit(event: FMLPostInitializationEvent) {
        if (config.hasChanged()) config.save()
    }

    fun init(event: FMLInitializationEvent) {

    }
}
