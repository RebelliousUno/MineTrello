package uno.rebellious.minetrello

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import net.minecraft.block.Block
import net.minecraft.block.BlockWallSign
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World
import org.apache.logging.log4j.Level
import uno.rebellious.minetrello.dao.TrelloDAOImpl
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit
import kotlin.math.max

class BoardHandler {
    val ticker = Observable.interval(10, TimeUnit.SECONDS)
    var boards = ArrayList<TrelloBoard>()
    private val tickerSubscription: Disposable

    init {
        tickerSubscription = ticker.subscribe {
            updateBoards()
        }
    }

    fun updateBoards() {
        MineTrello.logger?.log(Level.INFO, "Update Boards")
        boards.forEach {
            it.updateTrelloBoard()
        }
    }

    fun isValidBoard(board: List<BlockPos?>, world: World): Boolean {
        val notNullPos = board.filterNotNull()
        return (notNullPos.size == 4) && isValidBoard(notNullPos[0], notNullPos[1], notNullPos[2], notNullPos[3], world)
    }

    private fun isValidBoard(
                             position1: BlockPos,
                             position2: BlockPos,
                             position3: BlockPos,
                             position4: BlockPos,
                             world: World): Boolean {
        val validBlocks = listOf(Blocks.STONE, Blocks.REDSTONE_BLOCK)
        return isValidBoard(position1, position2, position3, position4, world, validBlocks)
    }

    private fun isValidBoard(
        position1: BlockPos,
        position2: BlockPos,
        position3: BlockPos,
        position4: BlockPos,
        world: World,
        validBlocks: List<Block>
    ): Boolean {
        val minX = listOf(position1.x, position2.x, position3.x, position4.x).min()!!
        val maxX= listOf(position1.x, position2.x, position3.x, position4.x).max()!!
        val minY= listOf(position1.y, position2.y, position3.y, position4.y).min()!!
        val maxY= listOf(position1.y, position2.y, position3.y, position4.y).max()!!
        val minZ= listOf(position1.z, position2.z, position3.z, position4.z).min()!!
        val maxZ= listOf(position1.z, position2.z, position3.z, position4.z).max()!!
        (minX..maxX).forEach {x ->
            (minY..maxY).forEach {y->
                (minZ..maxZ).forEach {z ->
                    val blockPos = BlockPos(x,y,z)
                    if (!validBlocks.contains(world.getBlockState(blockPos).block)) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun isValidBoard(block: BlockPos, world: World, facing: EnumFacing): Pair<Boolean, List<BlockPos?>> {
        val firstCol = isValidBoardCol(block, world)
        if (firstCol.first) {
            val firstRow = isValidBoardBottomRow(block, world, facing)
            val endOfRowBlock = firstRow.second
            if (firstRow.first && endOfRowBlock != null) {
                val lastCol = isValidBoardCol(endOfRowBlock, world)
                if (lastCol.first) {
                    return Pair(true, listOf(block, firstCol.second, firstRow.second, lastCol.second))
                }
            }
        }
        return Pair(false, emptyList())
    }

    fun isValidBoardBottomRow(block: BlockPos, world: World, facing: EnumFacing): Pair<Boolean, BlockPos?> {
        val blockToTheSide = block.offset(facing)
        val theBlock = world.getBlockState(blockToTheSide).block
        return if (theBlock == Blocks.REDSTONE_BLOCK) {
            Pair(true, blockToTheSide)
        } else {
            if (theBlock == Blocks.STONE)
                isValidBoardBottomRow(blockToTheSide, world, facing)
            else
                Pair(false, null)
        }
    }

    fun isValidBoardCol(block: BlockPos, world: World): Pair<Boolean, BlockPos?> {
        //Go up until meet Redstone, stone, Air
        val blockAbove = block.up()
        val theBlock = world.getBlockState(blockAbove).block
        return if (theBlock == Blocks.REDSTONE_BLOCK) {
            Pair(true, blockAbove)
        } else {
            if (theBlock == Blocks.STONE)
                isValidBoardCol(blockAbove, world)
            else
                Pair(false, null)
        }
    }

    fun findFrontFacing(corners: List<BlockPos>, world: World): EnumFacing {
        //work on axis of board...
        val validBlocks = listOf(Blocks.AIR, Blocks.WALL_SIGN)
        if (corners.size != 4) return EnumFacing.DOWN
        if (corners[0].x == corners[1].x && corners[1].x == corners[2].x && corners[2].x == corners[3].x) {
            //Is along the Z axis (North +ve Z South -ve Z)
            MineTrello.logger?.log(Level.INFO, "On X Axis")
            listOf(EnumFacing.EAST, EnumFacing.WEST)
                .forEach {
                    if (isValidBoard(corners[0].offset(it), corners[1].offset(it), corners[2].offset(it), corners[3].offset(it), world, validBlocks)) return it
                }
        } else {
            //is Along the X axis (East +ve x, West -ve X)
            MineTrello.logger?.log(Level.INFO, "On Z Axis")
            listOf(EnumFacing.NORTH, EnumFacing.SOUTH)
                .forEach {
                    if (isValidBoard(corners[0].offset(it), corners[1].offset(it), corners[2].offset(it), corners[3].offset(it), world, validBlocks)) return it
                }
        }
        return EnumFacing.DOWN // Down for negative board
    }

    fun findBoardFromSign(signPos: BlockPos, world: World, boardId: String): TrelloBoard? {
        //Check if Sign already exists
        val sign = boards.firstOrNull { it.signPos.equals(signPos) }
        if (sign == null) {
            val signNeighbours = EnumFacing.HORIZONTALS
            signNeighbours
                .filter { world.getBlockState(signPos.offset(it)).block.equals(Blocks.REDSTONE_BLOCK) }
                .map { Pair(it, isValidBoard(signPos.offset(it), world, it)) }
                .forEach {
                    val blockList = it.second.second.filterNotNull()
                    if (blockList.size == 4 && isValidBoard(blockList, world)) {
                        val frontFacing = findFrontFacing(blockList, world)
                        if (frontFacing != EnumFacing.DOWN) {
                            val trello = TrelloBoard(signPos, blockList, frontFacing, boardId, world)
                            boards.add(trello)
                            return trello
                        }
                    }
                }
        } else {
            if (isValidBoard(sign.trelloBoard, world) && findFrontFacing(sign.trelloBoard, world) == sign.facing) return sign
        }
        return null
    }

    fun disposeOfTimer() {
        if (!tickerSubscription.isDisposed) tickerSubscription.dispose()
    }
}
//World might be bad...might be better to do dimension?
class TrelloBoard(val signPos: BlockPos, val trelloBoard: List<BlockPos>, val facing: EnumFacing, val boardId: String, val world: World) {
    private var _name = ""
    var name: String
        get() = _name
        set(value) {
            _name = value
        }

    private val signPositions = HashSet<BlockPos>()
    private val oldPositions = HashSet<BlockPos>()

    private val maxY: Int = trelloBoard.asSequence().map { it.y }.max()!!
    private val minY: Int = trelloBoard.asSequence().map { it.y }.min()!!
    private val minX: Int = trelloBoard.asSequence().map { it.x }.min()!!
    private val maxX = trelloBoard.asSequence().map { it.x }.max()!!
    private val minZ = trelloBoard.asSequence().map { it.z }.min()!!
    private val maxZ = trelloBoard.asSequence().map { it.z }.max()!!

    private fun updateTitle() {
        val middleX = (maxX +  minX) / 2
        val middleZ = (maxZ + minZ) / 2
        val titlePos = BlockPos(middleX, maxY, middleZ).offset(facing)
        placeSignAt(titlePos, breakStringToLines(_name, 15), true)
    }

    private fun placeSignAt(position: BlockPos, signText: List<String>, isTitle: Boolean = false) {
        signPositions.add(position)
        if (!oldPositions.contains(position))
            world.setBlockState(position, Blocks.WALL_SIGN.defaultState.withProperty(BlockWallSign.FACING, facing))

        val tile = (world.getTileEntity(position) as? TileEntitySign)
        if (tile == null) signPositions.remove(position)
        else {
            signText
                .filterIndexed { index, _ -> index < 4 }
                .forEachIndexed { index, text -> tile.signText[index] = TextComponentString(text) }
        }
        (world.getTileEntity(position) as? TileEntitySign)?.signText?.get(0)?.style?.underlined = isTitle
        (world.getTileEntity(position) as? TileEntitySign)?.signText?.get(0)?.style?.bold = isTitle
    }

    private fun placeCardsForList(
        listId: String,
        xPos: Int,
        zPos: Int,
        facing: EnumFacing,
        spacing: Int
    ) {
        var xPos = xPos
        var zPos = zPos

        var column = 0
        val listHeight = maxY - 1 - minY
        TrelloDAOImpl().getCardsForListId(listId).subscribe { cards ->
            cards.cards.forEachIndexed { index, name ->
                val yPos = (maxY - 2 - index) + (listHeight * column)
                MineTrello.logger?.info("maxY: $maxY, index: $index, listHeight: $listHeight, column: $column, yPos: $yPos, sign: $name")
                if (yPos>= minY) placeSignAt(BlockPos(xPos, yPos, zPos).offset(this.facing), breakStringToLines(name, 15))
                else {
                    column++
                    val newYpos = (maxY - 2 - index) + (listHeight * column)
                    when (facing) {
                        EnumFacing.EAST -> {
                            zPos --
                        }
                        EnumFacing.WEST -> {
                            zPos ++
                        }
                        EnumFacing.NORTH -> {
                            xPos --
                        }
                        EnumFacing.SOUTH -> {
                            xPos ++
                        }
                        else -> {
                        }
                    }
                    placeSignAt(BlockPos(xPos, newYpos, zPos).offset(this.facing), breakStringToLines(name, 15))
                }
            }
        }
    }

    private fun breakStringToLines(line: String, maxLenth: Int): List<String> {
        val splitLine = line.split(" ")
        val result = mutableListOf(StringBuilder(), StringBuilder(), StringBuilder(), StringBuilder())
        var listLine = 0
        splitLine.forEach {
            if ((result[listLine].length + it.length + 1 ) > maxLenth) listLine++ //account for the space
            if (listLine < 4) {
                result[listLine].append(" ").append(it)
            }
        }
        return result.map { it.trim() }.map { it.toString() }
    }


    private fun placeSignsForLists(lists: List<Pair<String,String>>) {
        if (lists.isEmpty()) return
        val listHeight = maxY - 1 //List titles 1 lower than the title
        val boardWidth = max(maxX-minX, maxZ-minZ) // if the board is on that axis max-min will be 0
        val spacing = max(1, boardWidth / lists.size) // Make sure this is at least 1 (This is left Aligned)
        lists.forEachIndexed { index, list ->
            val listName = list.second
            val listId = list.first
            val xPos: Int
            val zPos: Int
            val offsetPos = (index * spacing)
            when (facing) {
                EnumFacing.EAST -> {
                    xPos = minX
                    zPos = maxZ - offsetPos
                }
                EnumFacing.WEST -> {
                    xPos = minX
                    zPos = minZ + offsetPos
                }
                EnumFacing.NORTH -> {
                    xPos = maxX - offsetPos
                    zPos = minZ
                }
                EnumFacing.SOUTH -> {
                    xPos = minX + offsetPos
                    zPos = minZ
                }
                else -> {
                    xPos = minX
                    zPos = minZ
                }
            }
            placeCardsForList(listId, xPos, zPos, facing, spacing)
            val signPos = BlockPos(xPos, listHeight, zPos).offset(facing)
            placeSignAt(signPos, breakStringToLines(listName, 15))
        }
    }

    private fun updateLists() {
        TrelloDAOImpl()
            .getListsForBoardId(boardId)
            .subscribe { lists ->
                MineTrello.logger?.log(Level.INFO, lists.listIds)
                placeSignsForLists(lists.listIds)
        }
    }

    private fun clearBoard() {
        oldPositions.removeAll(signPositions)

        oldPositions.forEach {
            world.setBlockToAir(it)
        }
        oldPositions.clear()
        oldPositions.addAll(signPositions)
        signPositions.clear()
    }

    fun updateTrelloBoard() {
        updateTitle()
        updateLists()
        clearBoard()
        //Get the trello data
        //Update signs on the board

    }



}
