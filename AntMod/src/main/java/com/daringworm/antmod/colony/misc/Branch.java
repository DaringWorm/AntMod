package com.daringworm.antmod.colony.misc;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;

public interface Branch<T> {

    BlockPos getPos();

    T getParent();

    ArrayList<T> getChildren();

    ArrayList<T> getChildrenRecursive();

    void setPos(BlockPos newPos);

    void addChild(T child);

    void removeChild(T child);

    void setChildren(ArrayList<T> newChildren);

    void setParent(T newParent);

    boolean hasChild(String iD);

    boolean hasChild(T child);

    void setValues(HashMap<String, String> vals);

    HashMap<String, String> getValues();

    void setValue(String key, String val);

    String getValue(String key);

    void removeKey(String key);

    boolean hasKey(String key);
}
