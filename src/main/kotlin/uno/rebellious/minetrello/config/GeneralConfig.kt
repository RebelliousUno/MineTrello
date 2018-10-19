package uno.rebellious.minetrello.config

import net.minecraftforge.common.config.Configuration
import org.apache.logging.log4j.Level
import uno.rebellious.minetrello.MineTrello

object GeneralConfig {

    var configData: ConfigData? = null

    fun readConfig(configuration: Configuration) {
        try {
            configuration.load()
            configData = ConfigData(
                configuration.get(
                    ConfigConstants.GROUP_GENERAL_KEY,
                    ConfigConstants.API_KEY_KEY,
                    ConfigConstants.APK_KEY_DEFAULT
                ).string,
                configuration.get(
                    ConfigConstants.GROUP_GENERAL_KEY,
                    ConfigConstants.TOKEN_KEY,
                    ConfigConstants.TOKEN_DEFAULT
                ).string,
                configuration.get(
                    ConfigConstants.GROUP_GENERAL_KEY,
                    ConfigConstants.BOARD_ID_KEY,
                    ConfigConstants.BOARD_ID_DEFAULT
                ).string
            )
            MineTrello.logger?.log(Level.INFO, configData)
        } finally {
            if (configuration.hasChanged()) configuration.save()
        }
    }
}
