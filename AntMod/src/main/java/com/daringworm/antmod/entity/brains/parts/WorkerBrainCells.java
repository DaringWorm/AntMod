package com.daringworm.antmod.entity.brains.parts;

public final class WorkerBrainCells {


    public static Braincell ATTACK_TARGET = new Braincell(Actions.WALK_TO_TARGET, Actions.WALK_TO_TARGET, AntPredicates.CAN_REACH_TARGET, Actions.ATTACK_ENTITY, "Attack Target");
    public static Braincell SET_TARGET = new Braincell(Actions.SET_AGGRESSOR_AS_TARGET, "Set attack target");
    public static Braincell ERROR_ALERT = new Braincell(Actions.ERROR_MSG_ACTION, "Error detected");
    public static Braincell WALK_TO_INTEREST_BLOCK = new Braincell(Actions.WALK_TO_BLOCK, "Walk to the interest Pos");
    public static Braincell BREAK_INTEREST_BLOCK = new Braincell(Actions.WALK_TO_BLOCK,Actions.WALK_TO_BLOCK, AntPredicates.IN_RANGE_OF_INTEREST_BLOCK,Actions.BREAK_INTEREST_BLOCK, "Break Interest Pos");
    public static Braincell FIND_FORAGING_POS = new Braincell(Actions.FIND_INTEREST_BLOCK, "Find Interest Pos");
    public static Braincell GO_PICKUP_ITEM = new Braincell(Actions.SELECT_ITEM_TO_TARGET,Actions.WALK_TO_TARGET, AntPredicates.CAN_REACH_PASSIVE_TARGET, Actions.PICKUP_ITEM, "Go pickup an Item");
    public static Braincell PLACE_ITEM_IN_CONTAINER = new Braincell(Actions.SET_CONTAINER_POS_TO_INTEREST,Actions.WALK_TO_BLOCK, AntPredicates.IN_RANGE_OF_INTEREST_BLOCK,Actions.PLACE_ITEM_IN_CONTAINER, "Place an Item in a Container");
    public static Braincell EXCAVATE_COLONY = new Braincell(Actions.SET_EXCAVATION_POS_TO_INTEREST, Actions.WALK_TO_BLOCK, AntPredicates.NAV_DONE, Actions.EXCAVATE_INTEREST_POS, "Excavate colony");
    public static Braincell LATCH_ON = new Braincell(Actions.LATCH_ON, "Latch on to target");
    public static Braincell SCOUT = new Braincell(Actions.SCOUT, "Scouting for foliage");
    public static Braincell GO_UNDERGROUND = new Braincell(Actions.GO_UNDERGROUND, "Go underground");
    public static Braincell GO_ABOVEGROUND = new Braincell(Actions.GO_ABOVEGROUND, "Go aboveground");


    public static BrainFork TARGET_MANAGER_FORK = new BrainFork(1, ATTACK_TARGET, SET_TARGET, AntPredicates.HAS_SELECTED_TARGET);
    public static BrainFork PLACE_ITEMS_IN_CONTAINER_FORK = new BrainFork(1,PLACE_ITEM_IN_CONTAINER,GO_UNDERGROUND,AntPredicates.FOUND_CONTAINER);
    public static BrainFork MINE_INTEREST_BLOCK_FORK = new BrainFork(1, GO_PICKUP_ITEM, BREAK_INTEREST_BLOCK, AntPredicates.SEES_ITEMS);
    public static BrainFork FORAGING_FORK = new BrainFork(1,PLACE_ITEMS_IN_CONTAINER_FORK,MINE_INTEREST_BLOCK_FORK,AntPredicates.HAS_ITEM);



    public static BrainFork AGGRO_MANAGER_FORK = new BrainFork(1, LATCH_ON, TARGET_MANAGER_FORK, AntPredicates.CAN_REACH_TARGET);
    public static BrainFork MASTER_EXCAVATING_FORK = new BrainFork(0, EXCAVATE_COLONY, AGGRO_MANAGER_FORK, AntPredicates.IS_EXCAVATING);
    public static BrainFork MASTER_TIDYING_FORK = new BrainFork(0, ERROR_ALERT, MASTER_EXCAVATING_FORK, AntPredicates.IS_TIDYING);
    public static BrainFork MASTER_NURSING_FORK = new BrainFork(0, ERROR_ALERT, MASTER_TIDYING_FORK, AntPredicates.IS_NURSING);
    public static BrainFork MASTER_FUNGUS_FARMING_FORK = new BrainFork(0, ERROR_ALERT, MASTER_NURSING_FORK, AntPredicates.IS_FARMING);
    public static BrainFork MASTER_FORAGE_FORK = new BrainFork(0, FORAGING_FORK, MASTER_FUNGUS_FARMING_FORK, AntPredicates.IS_FORAGING);
    public static BrainFork MASTER_PASSIVE_FORK = new BrainFork(0, SCOUT, MASTER_FORAGE_FORK, AntPredicates.IS_SCOUTING);


}
