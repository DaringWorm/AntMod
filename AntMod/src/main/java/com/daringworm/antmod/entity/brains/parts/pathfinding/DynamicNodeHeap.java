package com.daringworm.antmod.entity.brains.parts.pathfinding;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;

public class DynamicNodeHeap {
    private final ArrayList<PathNode> list = new ArrayList<>();
    private PathNode ideal;

    public DynamicNodeHeap(PathNode idealPos, int estSpace){
        this.ideal = idealPos;
        list.ensureCapacity(estSpace);
    }

    public void add(PathNode newPos, PathNode newIdeal){
        if(newIdeal != null) {
            this.ideal = newIdeal;
        }
        list.add(newPos);
        bubbleUp(list.size() - 1);
    }

    public void add(PathNode newPos){
        list.add(newPos);
        bubbleUp(list.size() - 1);
    }

    public PathNode remove(){
        if(list.size() > 1) {
            PathNode top = list.get(0);
            list.set(0, list.remove(list.size() - 1));
            bubbleDown(0);
            return top;
        }
        else if(!list.isEmpty()){
            return list.remove(0);
        }
        return null;
    }

    public PathNode peek(){
        if(list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    public int size(){return list.size();}

    public boolean isEmpty(){return list.isEmpty();}

    private boolean isValidIdx(int idx){
        return idx >= 0 && idx < size();
    }

    private int parentIdx(int childIdx){
        return (childIdx-1)/2;
    }

    private int leftIdx(int parentIdx){
        return parentIdx * 2 + 1;
    }

    private int rightIdx(int parentIdx){
        return parentIdx * 2 + 2;
    }

    private boolean posIsBetter(PathNode toCompare, PathNode comparison){
        return toCompare.pos.distSqr(ideal.pos) < comparison.pos.distSqr(ideal.pos);
    }

    private boolean posIsBetter(int toCompareIdx, int comparisonIdx){
        PathNode toCompare = list.get(toCompareIdx);
        PathNode comparison = list.get(comparisonIdx);

        return isValidIdx(toCompareIdx)
                && isValidIdx(comparisonIdx)
                && (toCompare.weight < comparison.weight || toCompare.pos.distSqr(ideal.pos) < comparison.pos.distSqr(ideal.pos));
    }

    private void bubbleUp(int idx){
        int parentIdx = parentIdx(idx);
        if(posIsBetter(idx, parentIdx)){
            PathNode holder = list.get(idx);
            list.set(idx, list.get(parentIdx));
            list.set(parentIdx, holder);
            bubbleUp(parentIdx);
        }
    }

    private void bubbleDown(int idx){

        if(isValidIdx(rightIdx(idx))) {
            int leftIdx = leftIdx(idx);
            int rightIdx = rightIdx(idx);

            if (posIsBetter(leftIdx, idx) && posIsBetter(leftIdx, rightIdx)) {
                PathNode holder = list.get(idx);
                list.set(idx, list.get(leftIdx));
                list.set(leftIdx, holder);
                bubbleDown(leftIdx);
            } else if (posIsBetter(rightIdx, idx)) {
                PathNode holder = list.get(idx);
                list.set(idx, list.get(rightIdx));
                list.set(rightIdx, holder);
                bubbleDown(rightIdx);
            }
        }
        else if(isValidIdx(leftIdx(idx))){
            int leftIdx = leftIdx(idx);
            if(posIsBetter(leftIdx, idx)){
                PathNode holder = list.get(idx);
                list.set(idx, list.get(leftIdx));
                list.set(leftIdx, holder);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder strB = new StringBuilder();
        strB.append('[');
        for(PathNode tempPos : list){
            strB.append(tempPos);
            strB.append(", ");
        }
        strB.append(']');
        return strB.toString();
    }
}
