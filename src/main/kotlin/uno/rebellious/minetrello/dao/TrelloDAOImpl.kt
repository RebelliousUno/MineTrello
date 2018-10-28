package uno.rebellious.minetrello.dao

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.rx.rx_responseString
import com.github.kittinunf.result.Result
import io.reactivex.Single
import org.apache.logging.log4j.Level
import uno.rebellious.minetrello.MineTrello
import uno.rebellious.minetrello.config.GeneralConfig

class TrelloDAOImpl : TrelloDAO {
    val baseURL = "https://api.trello.com/1"
    val keyTokenParams = listOf(Pair("key",GeneralConfig.configData?.apiKey), Pair("token",GeneralConfig.configData?.token))


    override fun getListsForBoardId(boardId: String): Single<TrelloList> {
        val uri = "${baseURL}/boards/$boardId/lists"
        return Fuel.get(uri, keyTokenParams).rx_responseString()
            .map {
                TrelloList(it.second.get())
            }
    }

    override fun getBoardForId(id: String): Single<Board> {
        val uri = "${baseURL}/boards/$id"
        return Fuel.get(uri, keyTokenParams).rx_responseString()
            .map {
                Board(it.second.get())
            }
    }

    override fun getCardsForListId(listId: String): Single<CardList> {
        val uri = "$baseURL/lists/$listId/cards"
        return Fuel.get(uri, keyTokenParams).rx_responseString()
            .map{
                CardList(it.second.get())
            }
    }

    override fun getBoards(): Single<Pair<Response, Result<String, FuelError>>> {
        val uri = "https://api.trello.com/1/boards/${GeneralConfig.configData?.boardId}?key=${GeneralConfig.configData?.apiKey}&token=${GeneralConfig.configData?.token}"
        return Fuel.get(uri).rx_responseString()
    }


}