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

    override fun getBoardForId(id: String): Single<Board> {
        val uri = "https://api.trello.com/1/boards/$id?key=${GeneralConfig.configData?.apiKey}&token=${GeneralConfig.configData?.token}"
        MineTrello.logger?.log(Level.INFO, uri)
        return Fuel.get(uri).rx_responseString()
            .map {
                Board(it.second.get())
            }
    }


    override fun getBoards(): Single<Pair<Response, Result<String, FuelError>>> {
        val uri = "https://api.trello.com/1/boards/${GeneralConfig.configData?.boardId}?key=${GeneralConfig.configData?.apiKey}&token=${GeneralConfig.configData?.token}"
        return Fuel.get(uri).rx_responseString()
//        return ObservableHttp.createGet(uri, client).toObservable()
//            .flatMap { response ->
//                response.content.map {bytes ->
//                    String(bytes)
//                } }
    }
}