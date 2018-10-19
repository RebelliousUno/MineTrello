package uno.rebellious.minetrello.config

data class ConfigData(val apiKey: String, val token: String, val boardId: String)

object ConfigConstants {
    val GROUP_GENERAL_KEY = "MineTrello"
    val API_KEY_KEY = "trello_api_key"
    val APK_KEY_DEFAULT = "Api Key"
    val TOKEN_KEY = "trello_token"
    val TOKEN_DEFAULT = "trello_token"
    val BOARD_ID_KEY = "board_id"
    val BOARD_ID_DEFAULT = "trello_board_id"
}