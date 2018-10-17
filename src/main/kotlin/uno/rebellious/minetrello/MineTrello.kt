package uno.rebellious.minetrello

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.Logger
import uno.rebellious.minetrello.proxy.CommonProxy


@Mod(
        modid = MineTrello.MOD_ID,
        name = MineTrello.NAME,
        version = MineTrello.VERSION,
        modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter"
)
object MineTrello {
    const val MOD_ID = "minetrello"
    const val NAME = "MineTrello"
    const val VERSION = "0.0.1"

    @Mod.Instance
    var instance: MineTrello? = null

    @SidedProxy(
            clientSide = "uno.rebellious.minetrello.proxy.ClientProxy",
            serverSide = "uno.rebellious.minetrello.proxy.ServerProxy",
            modId = MOD_ID
    )
    var proxy: CommonProxy? = null

    var logger: Logger? = null

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        logger = event.modLog
        proxy?.preInit(event)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        proxy?.postInit(event)
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        proxy?.init(event)
    }
}