package uno.rebellious.minetrello.dao

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.rx.rx_responseString
import com.github.kittinunf.result.Result
import io.reactivex.Single

import uno.rebellious.minetrello.config.GeneralConfig

class TrelloDAOImpl : TrelloDAO {
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