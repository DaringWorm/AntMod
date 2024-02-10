package com.daringworm.antmod.entity.brains.parts;

import java.util.ArrayList;
import java.util.Arrays;

public final class WorkerBrainCells {


    public static Braincell ATTACK_TARGET = new Braincell(Actions.WALK_TO_TARGET, Actions.WALK_TO_TARGET, AntPredicates.CAN_REACH_TARGET, Actions.ATTACK_ENTITY, "Attack Target");
    public static Braincell SET_TARGET = new Braincell(Actions.SET_AGGRESSOR_AS_TARGET, "Set attack target");
    public static Braincell ERROR_ALERT = new Braincell(Actions.ERROR_MSG_ACTION, "Error detected");
    public static Braincell WALK_TO_INTEREST_BLOCK = new Braincell(Actions.WALK_TO_BLOCK, "Walk to the interest Pos");
    public static Braincell BREAK_INTEREST_BLOCK = new Braincell(Actions.WALK_TO_BLOCK,Actions.WALK_TO_BLOCK, AntPredicates.IN_RANGE_OF_INTEREST_BLOCK,Actions.BREAK_INTEREST_BLOCK, "Break Interest Pos");
    public static Braincell FIND_FORAGING_POS = new Braincell(Actions.FIND_INTEREST_BLOCK, "Find Interest Pos");
    public static Braincell GO_PICKUP_ITEM = new Braincell(Actions.SELECT_ITEM_TO_TARGET,Actions.WALK_TO_TARGET, AntPredicates.NAV_DONE, Actions.PICKUP_ITEM, "Go pickup an Item");
    public static Braincell PLACE_ITEM_IN_CONTAINER = new Braincell(Actions.SET_CONTAINER_POS_TO_INTEREST,Actions.WALK_TO_BLOCK, AntPredicates.IN_RANGE_OF_INTEREST_BLOCK,Actions.PLACE_ITEM_IN_CONTAINER, "Place an Item in a Container");
    public static Braincell EXCAVATE_COLONY = new Braincell(Actions.SET_EXCAVATION_POS_TO_INTEREST, Actions.WALK_TO_BLOCK, AntPredicates.NAV_DONE, Actions.EXCAVATE_INTEREST_POS, "Excavate colony");
    public static Braincell LATCH_ON = new Braincell(Actions.LATCH_ON, "Latch on to target");
    public static Braincell SCOUT = new Braincell(Actions.SCOUT, "Scouting for foliage").addShouldChoosePredicate(AntPredicates.IS_SCOUTING);
    public static Braincell GO_UNDERGROUND = new Braincell(Actions.GO_UNDERGROUND, "Go underground");
    public static Braincell GO_ABOVEGROUND = new Braincell(Actions.GO_ABOVEGROUND, "Go aboveground");


    public static BrainFork TARGET_MANAGER_FORK = new BrainFork(AntPredicates.HAS_SELECTED_TARGET).add(ATTACK_TARGET).add(SET_TARGET);
    public static BrainFork PLACE_ITEMS_IN_CONTAINER_FORK = new BrainFork(AntPredicates.FOUND_CONTAINER).add(PLACE_ITEM_IN_CONTAINER).add(GO_UNDERGROUND);
    public static BrainFork MINE_INTEREST_BLOCK_FORK = new BrainFork(AntPredicates.SEES_ITEMS).add(GO_PICKUP_ITEM).add(BREAK_INTEREST_BLOCK);
    public static BrainFork FORAGING_FORK = new BrainFork(AntPredicates.HAS_ITEM, AntPredicates.IS_FORAGING).add(PLACE_ITEMS_IN_CONTAINER_FORK).add(MINE_INTEREST_BLOCK_FORK);
    public static BrainFork FUNGUS_FORK = new BrainFork(AntPredicates.TRUE, AntPredicates.FALSE);
    public static BrainFork NURSING_FORK = new BrainFork(AntPredicates.TRUE, AntPredicates.FALSE);
    public static BrainFork TIDYING_FORK = new BrainFork(AntPredicates.TRUE, AntPredicates.FALSE);
    public static BrainFork AGGRO_MANAGER_FORK = new BrainFork(AntPredicates.CAN_REACH_TARGET, AntPredicates.TARGET_EXISTS).add(LATCH_ON).add(TARGET_MANAGER_FORK);


    private static final ArrayList<BrainFork> mA = new ArrayList<>(Arrays.asList(AGGRO_MANAGER_FORK,SCOUT,FORAGING_FORK,FUNGUS_FORK,NURSING_FORK,TIDYING_FORK));
    public static BrainFork MASTER_FORK = new BrainFork(AntPredicates.TRUE).addAll(mA).addKey("Master Fork");
}
