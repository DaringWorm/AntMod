package com.daringworm.antmod.colony.misc;

import com.daringworm.antmod.colony.ColonyGenerator;
import com.daringworm.antmod.goals.AntUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Random;

public final class ColonyGenUtils {

    public static ArrayList<PosSpherePair> generateRoomBlueprint(double height, int size, BlockPos center, Random rand){
        rand = new Random(Math.abs(center.getX()*center.getY()));
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();
        ArrayList<BlockPos> posList = new ArrayList<>();
        ArrayList<PosSpherePair> returnList = new ArrayList<>();
        posList.add(center);

        for(int i = size; i>0; i--){
            int xOff = rand.nextInt((int)(height*1.5));
            int zOff = rand.nextInt((int)(height*1.5));
            xOff = (rand.nextBoolean()) ? xOff : -xOff;
            zOff = (rand.nextBoolean()) ? zOff : -zOff;

            posList.add(new BlockPos(cx+xOff,cy,cz+zOff));
        }
        for(BlockPos tempPos : posList){
            returnList.add(new PosSpherePair(tempPos,height));
        }
        return returnList;
    }

    public static ArrayList<PosSpherePair> generatePassageBlueprint(PosPair pPath, double width, boolean wontReplaceAir){
        Random rand = new Random((long) pPath.top.getX() *pPath.top.getY()*pPath.top.getZ());
        ArrayList<PosSpherePair> returnList = new ArrayList<>();
        BlockPos start = pPath.top;
        BlockPos end = pPath.bottom;

        int sX = start.getX();
        int sZ = start.getZ();
        int eX = end.getX();
        int eZ = end.getZ();

        int steps = Math.abs(eX-sX) + Math.abs(eZ - sZ);
        boolean xOrZ;
        BlockPos lastPos = start;
        int howLongX = 0;
        int howLongZ = 0;
        float yOffC = 0;

        for(int s = steps; s > 0; s--){
            int xOff = 0;
            int yOff = 0;
            int zOff = 0;
            float yFOff = (float)(end.getY()-lastPos.getY())/(float)s;
            yOffC = yOffC + yFOff;
            if(Math.abs(yOffC) > 1f) {
                yOff = (yFOff > 0) ? 1 : -1;
                yOffC = (yFOff > 0) ? yOffC - 1: yOffC + 1;
            }

            howLongX = eX-lastPos.getX();
            howLongZ = eZ-lastPos.getZ();
            xOrZ = nextBool(Math.abs(howLongX),Math.abs(howLongZ),rand);
            if((xOrZ && howLongX != 0) || howLongZ == 0){xOff = (howLongX > 0) ? 1 : -1;}
            else{zOff = (howLongZ > 0) ? 1: -1;}

            lastPos = new BlockPos(lastPos.getX()+xOff, lastPos.getY()+yOff, lastPos.getZ()+zOff);
            PosSpherePair sphere = new PosSpherePair(lastPos, width, true);
            returnList.add(sphere.wontReplaceAir(wontReplaceAir));
        }
        return returnList;
    }

    public static boolean nextBool(int yes, int no, Random rand){
        int total = yes+no;
        int chosen = (total>0)? rand.nextInt(total) : 0;
        return chosen <= yes;
    }
}
