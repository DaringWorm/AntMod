package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.block.ModBlocks;
import com.daringworm.antmod.util.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class AntSphere implements Iterable<BlockPos>{
    public BlockPos centerPos;
    public double radius;
    private boolean checkSky = false;
    private boolean checkAir = false;

    public AntSphere(BlockPos center, double sphereRadius){
        this.centerPos = center;
        this.radius = sphereRadius;
    }

    public AntSphere(BlockPos center, double sphereRadius, boolean runSkyCheck){
        this.centerPos = center;
        this.radius = sphereRadius;
        this.checkSky = runSkyCheck;
    }

    public ArrayList<BlockPos> getBlockPoses(){
        ArrayList<BlockPos> returnList = new ArrayList<>();

        int intR = (int)(this.radius + 2);

        for(BlockPos tempPos : BlockPos.betweenClosed(this.centerPos.offset(intR, intR, intR), this.centerPos.offset(-intR, -intR, -intR))){
            if(tempPos.closerThan(this.centerPos, this.radius)){
                returnList.add(tempPos.offset(this.centerPos).immutable());
            }
        }

        return returnList;
    }

    public AntSphere wontReplaceAir(boolean bool){
        this.checkAir = bool;
        return this;
    }

    public AntSphere willCheckSky(boolean bool){
        this.checkSky = bool;
        return this;
    }

    public ArrayList<BlockPos> getBlockPoses(Level pLevel){
        ArrayList<BlockPos> returnList = new ArrayList<>();
        for(BlockPos tempPos : this.getBlockPoses()){
            if(AntUtils.getDist(tempPos, centerPos) <= radius && pLevel.getBlockState(tempPos).getBlock() != ModBlocks.ANT_AIR.get()){
                if(pLevel.getBlockState(tempPos).getBlock() == Blocks.AIR){
                    pLevel.setBlock(tempPos,ModBlocks.ANT_AIR.get().defaultBlockState(),2);
                }
                else {
                    returnList.add(tempPos);
                }
            }
        }

        return returnList;
    }

    public void setSphere(ServerLevel pLevel, Block innerBlock, Block outerBlock, double wallThickness){
        AntSphere outerShell = new AntSphere(this.centerPos,this.radius+wallThickness);
        ArrayList<BlockPos> totalList = outerShell.getBlockPoses();
        ArrayList<BlockPos> innerList = this.getBlockPoses();
        totalList.removeAll(innerList);
        for(BlockPos pos : totalList){
            if((!checkSky || !pLevel.canSeeSky(pos)) && (!checkAir || pLevel.getBlockState(pos).getBlock() != Blocks.AIR)) {
                BlockState pState = pLevel.getBlockState(pos);
                if (pState.getBlock() != innerBlock && pState.getBlock() != outerBlock) {
                    pLevel.setBlock(pos, outerBlock.defaultBlockState(), 2);
                }
            }
        }
        for(BlockPos pos : innerList){
            pLevel.setBlock(pos, innerBlock.defaultBlockState(),2);
        }
    }

    public void setSphereCarver(ChunkAccess chunkAccess, BlockState innerState, BlockState outerState, double wallThickness){
        double offset = radius+wallThickness;
        for(BlockPos tempPos : BlockPos.betweenClosed(this.centerPos.offset(offset, offset, offset), this.centerPos.offset(-offset,-offset,-offset))) {
            if ((AntUtils.isPosInChunk(tempPos, chunkAccess.getPos())) && (AntUtils.getDist(tempPos, centerPos) <= radius + wallThickness)) {
                if (chunkAccess.getBlockState(tempPos) != innerState /*&& (!checkSky || canSeeSky(chunkAccess, tempPos))*/) {
                    if (AntUtils.getDist(tempPos, this.centerPos) <= this.radius) {
                        chunkAccess.setBlockState(tempPos, innerState, false);
                    } else if (chunkAccess.getBlockState(tempPos) != outerState &&
                            (!chunkAccess.getBlockState(tempPos).isAir() || !canSeeSky(chunkAccess, tempPos))) {
                        chunkAccess.setBlockState(tempPos, outerState, false);
                    }
                }
            }
        }
    }


    private boolean canSeeSky(ChunkAccess access, BlockPos pos){
        for(int i = pos.getY()+1; i< access.getHeight(); i++){
            if(access.getBlockState(pos.atY(i)) != Blocks.AIR.defaultBlockState()){return false;}
        }
        return true;
    }


    private static class SphereIterator implements Iterator<BlockPos>{
        private final AntSphere sphere;
        private final int intR;
        private Iterator<BlockPos> rawIterator;
        private BlockPos next;

        SphereIterator(AntSphere sphere){
            this.sphere = sphere;
            this.intR = (int)(sphere.radius + 2);

            BlockPos positiveOffset = sphere.centerPos.offset(intR, intR, intR);
            BlockPos negativeOffset = sphere.centerPos.offset(-intR, -intR, -intR);

            this.rawIterator = BlockPos.betweenClosed(negativeOffset, positiveOffset).iterator();

            this.next = getNext();
        }

        private BlockPos getNext(){
            while(this.rawIterator.hasNext()){
                BlockPos tempNext = this.rawIterator.next();
                if(tempNext.closerThan(this.sphere.centerPos, this.sphere.radius)){
                    return tempNext.immutable();
                }
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public BlockPos next() {
            if(this.rawIterator.hasNext()){
                BlockPos oldNext = this.next;
                this.next = getNext();
                return oldNext;
            }
            else{
                throw new NoSuchElementException("Attempted to access a nonexistent index in an iterator originating in AntSphere");
            }
        }
    }

    @NotNull
    @Override
    public Iterator<BlockPos> iterator() {
        return new SphereIterator(this);
    }
}


