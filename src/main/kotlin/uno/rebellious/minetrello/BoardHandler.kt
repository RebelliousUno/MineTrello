package uno.rebellious.minetrello

import io.reactivex.Observable
import net.minecraft.block.Block
import net.minecraft.block.BlockSign
import net.minecraft.block.BlockWallSign
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.EnumFacing
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.world.World
import org.apache.logging.log4j.Level
import uno.rebellious.minetrello.dao.Board
import java.util.concurrent.TimeUnit

class BoardHandler {
    val ticker = Observable.interval(1, TimeUnit.MINUTES)
    var boards = ArrayList<TrelloBoard>()

    init {
        ticker.subscribe {
            updateBoards()
        }
    }

    fun updateBoards() {
        MineTrello.logger?.log(Level.INFO, "Update Boards")
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
}
//World might be bad...might be better to do dimension?
class TrelloBoard(val signPos: BlockPos, val trelloBoard: List<BlockPos>, val facing: EnumFacing, val boardId: String, val world: World) {
    private var _name = ""
    var name: String
        get() = _name
        set(value) {
            _name = value
            updateTitle()
        }


    private fun updateTitle() {
        val maxY = trelloBoard.asSequence().map { it.y }.max()!!
        val minX = trelloBoard.asSequence().map { it.x }.min()!!
        val maxX = trelloBoard.asSequence().map { it.x }.max()!!
        val middleX = (maxX +  minX) / 2
        val minZ = trelloBoard.asSequence().map { it.z }.min()!!
        val maxZ = trelloBoard.asSequence().map { it.z }.max()!!
        val middleZ = (maxZ + minZ) / 2

        val titlePos = BlockPos(middleX, maxY, middleZ).offset(facing)
        world.setBlockState(titlePos, Blocks.WALL_SIGN.defaultState.withProperty(BlockWallSign.FACING, facing))
        (world.getTileEntity(titlePos) as TileEntitySign).signText[0] = TextComponentString(_name)
    }


    fun updateTrelloBoard() {
        //Get the trello data
        //Update signs on the board

    }



}
