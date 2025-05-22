package com.daringworm.antmod.entity.brains.parts;

public final class WorkerBrainCells {
                                        //WANDERING\\

                                         //SCOUTING\\

    private static final Braincell SEES_ITEM = new Braincell("Sees items", AntPredicates.SEES_ITEMS, new Action[]{Actions.SET_WORKING_STAGE_FORAGE});
    private static final Braincell SEES_ENTITY = new Braincell("Sees passive target", AntPredicates.SEES_PASSIVE_TARGET, new Action[]{Actions.GET_PASSIVE_TARGET, Actions.SET_WORKING_STAGE_FORAGE, Actions.SPAWN_CLOUD_FOR_CURRENT_WS});
    private static final Braincell SEES_SNIPPABLE_BLOCK = new Braincell("Sees snippable block", AntPredicates.SEES_SNIPPABLE_BLOCK, new Action[]{Actions.SET_WORKING_STAGE_FORAGE, Actions.SPAWN_CLOUD_FOR_CURRENT_WS});
    private static final Braincell UNDERGROUND_W_COLONY = new Braincell("Has colony", AntPredicates.HAS_COLONY, new Action[]{Actions.GO_ABOVEGROUND});
    private static final Braincell UNDERGROUND_WO_COLONY = new Braincell("Doesn't have colony", AntPredicates.TRUE,new Action[]{Actions.WANDER});
    private static final Braincell SCOUTING_UNDERGROUND = new Braincell("Underground", AntPredicates.IS_UNDERGROUND, new Braincell[]{UNDERGROUND_W_COLONY, UNDERGROUND_WO_COLONY});
    private static final Braincell SCOUTING_ABOVE_GROUND = new Braincell("Above ground", AntPredicates.IS_UNDERGROUND.opposite(), new Braincell[]{SEES_SNIPPABLE_BLOCK}, new Action[]{Actions.WANDER});

                                         //FORAGING\\

    private static final Braincell NO_CONTAINER_AND_ITEM = new Braincell("No container", AntPredicates.HAS_CONTAINER.opposite(), new Action[]{Actions.FIND_OR_PLACE_NEW_CONTAINER});
    private static final Braincell HAS_CONTAINER_AND_ITEM = new Braincell("Has container", AntPredicates.HAS_CONTAINER, new Action[]{Actions.PLACE_ITEM_IN_CONTAINER, Actions.SET_WORKING_STAGE_SCOUT});
    private static final Braincell IS_NEAR_HOMEPOS_W_ITEM = new Braincell("Go to home", AntPredicates.NEAR_HOMEPOS, new Braincell[]{NO_CONTAINER_AND_ITEM,HAS_CONTAINER_AND_ITEM}, new Action[]{Actions.WALK_TO_HOMEPOS});
    private static final Braincell IS_FAR_FROM_HOME_W_COLONY = new Braincell("Go underground", AntPredicates.TRUE, new Action[]{Actions.GO_UNDERGROUND});
    private static final Braincell HAS_COLONY_AND_ITEM = new Braincell("Has colony", AntPredicates.HAS_COLONY, new Braincell[]{IS_NEAR_HOMEPOS_W_ITEM,IS_FAR_FROM_HOME_W_COLONY});
    private static final Braincell HAS_NO_COLONY_AND_ITEM = new Braincell("No colony", AntPredicates.HAS_COLONY.opposite(), new Braincell[]{IS_NEAR_HOMEPOS_W_ITEM});
    private static final Braincell HAS_ITEM = new Braincell("Has item", AntPredicates.HAS_ITEM, new Braincell[]{HAS_COLONY_AND_ITEM, HAS_NO_COLONY_AND_ITEM});
    private static final Braincell NEAR_PASSIVE_TARGET = new Braincell("Near target", AntPredicates.CAN_REACH_PASSIVE_TARGET, new Action[]{Actions.ATTACK_PASSIVE_TARGET});
    private static final Braincell HAS_PASSIVE_TARGET = new Braincell("Has passive target", AntPredicates.HAS_PASSIVE_TARGET, new Braincell[]{NEAR_PASSIVE_TARGET}, new Action[]{Actions.WALK_TO_PASSIVE_TARGET});
    private static final Braincell IN_RANGE_OF_SNIPPABLE_POS = new Braincell("In range", AntPredicates.IN_RANGE_OF_INTEREST_BLOCK, new Action[]{Actions.BREAK_INTEREST_BLOCK});
    private static final Braincell HAS_SNIPPABLE_INTEREST = new Braincell("Has snippable pos", AntPredicates.SHOULD_SNIP_INTEREST.and(AntPredicates.SEES_ITEMS.opposite()), new Braincell[]{IN_RANGE_OF_SNIPPABLE_POS}, new Action[]{Actions.WALK_TO_BLOCK});
    private static final Braincell HAS_NOTHING_AND_SEES_ITEM = new Braincell("Sees item", AntPredicates.SEES_ITEMS, new Action[]{Actions.PICKUP_ITEM});
    private static final Braincell HAS_NOTHING_AND_SEES_NOTHING = new Braincell("Sees nothing", AntPredicates.TRUE, new Action[]{Actions.SET_WORKING_STAGE_SCOUT});
    private static final Braincell FORAGING_HAS_NOTHING = new Braincell("Has nothing", AntPredicates.TRUE, new Braincell[]{HAS_NOTHING_AND_SEES_ITEM,HAS_NOTHING_AND_SEES_NOTHING});

                                         //FARMING\\

    private static final Braincell SWITCH_TO_FORAGING = new Braincell("Revert to foraging", AntPredicates.TRUE, new Action[]{Actions.SET_WORKING_STAGE_FORAGE});
    private static final Braincell EXTRACT_LEAVES = new Braincell("Extract leaves", AntPredicates.HAS_CONTAINER_AS_INTEREST.and(AntPredicates.HAS_ITEM.opposite()), new Action[]{Actions.EXTRACT_LEAVES, Actions.SET_INTEREST_TO_FUNGUS_POS});
    private static final Braincell PLACE_LEAVES = new Braincell("Place leaves", AntPredicates.IN_RANGE_OF_INTEREST_BLOCK, new Action[]{Actions.PLACE_HELD_BLOCK_AT_INTEREST});
    private static final Braincell HAS_LEAVES = new Braincell("Has leaves", AntPredicates.HAS_CONTAINER_AS_INTEREST.opposite().and(AntPredicates.IS_HOLDING_LEAVES), new Braincell[]{PLACE_LEAVES}, new Action[]{Actions.WALK_TO_BLOCK});
    private static final Braincell EAT_FUNGUS = new Braincell("Eat fungus", AntPredicates.IN_RANGE_OF_FUNGUS_POS, new Action[]{Actions.EAT_FUNGUS});
    private static final Braincell HUNGRY_FOR_FUNGUS = new Braincell("Walk to fungus", AntPredicates.IS_HUNGRY.and(AntPredicates.HAS_FUNGUS_POS), new Braincell[]{EAT_FUNGUS}, new Action[]{Actions.SET_INTEREST_TO_FUNGUS_POS ,Actions.WALK_TO_BLOCK});
    
                                         //ATTACK\\

    private static final Braincell DAMAGE_TARGET = new Braincell("Damage target", AntPredicates.CAN_REACH_TARGET, new Action[]{Actions.DROP_ITEM, Actions.ATTACK_HOSTILE_TARGET});
    private static final Braincell WALK_TO_TARGET = new Braincell("Walk to target", AntPredicates.TARGET_EXISTS, new Braincell[]{/*DAMAGE_TARGET*/}, new Action[]{Actions.DROP_ITEM, Actions.LATCH_ON});




                                        //EXCAVATE\\

    public static final Braincell EXCAVATE_BLOCK = new Braincell("Excavate block", AntPredicates.FOUND_INTEREST_BLOCK, new Action[]{Actions.DROP_ITEM, Actions.EXCAVATE_INTEREST_POS, Actions.WALK_TO_BLOCK});



                                          //MAIN\\

    public static final Braincell WANDERING_FORK = new Braincell("Wandering", AntPredicates.IS_WANDERING, new Action[]{Actions.WANDER});
    public static final Braincell SCOUTING_FORK = new Braincell("Scouting", AntPredicates.IS_SCOUTING, new Braincell[]{SEES_ITEM, SEES_ENTITY, SCOUTING_UNDERGROUND, SCOUTING_ABOVE_GROUND});
    public static final Braincell FORAGING_FORK = new Braincell("Foraging", AntPredicates.IS_FORAGING, new Braincell[]{HAS_ITEM, HAS_PASSIVE_TARGET, HAS_SNIPPABLE_INTEREST, FORAGING_HAS_NOTHING});
    public static final Braincell FARMING_FORK = new Braincell("Farming", AntPredicates.IS_FARMING, new Braincell[]{EXTRACT_LEAVES, HAS_LEAVES, HUNGRY_FOR_FUNGUS, SWITCH_TO_FORAGING});
    public static final Braincell NURSING_FORK = new Braincell("Nursing", AntPredicates.IS_NURSING);
    public static final Braincell TIDYING_FORK = new Braincell("Tidying", AntPredicates.IS_TIDYING);
    public static final Braincell EXCAVATING_FORK = new Braincell("Excavating", AntPredicates.IS_EXCAVATING, new Braincell[]{EXCAVATE_BLOCK, SWITCH_TO_FORAGING}, new Action[]{Actions.SET_EXCAVATION_POS_TO_INTEREST});
    public static final Braincell ATTACKING_FORK = new Braincell("Attacking", AntPredicates.IS_ATTACKING, new Braincell[]{WALK_TO_TARGET, SWITCH_TO_FORAGING});
    public static final Braincell LATCHING_FORK = new Braincell("Latching", AntPredicates.IS_LATCHING);


    public static final Braincell MAIN_FORK = new Braincell("Mainfork", AntPredicates.TRUE).setActions(new Action[]{Actions.TEST_PATHFINDING});//.setSubCells(new Braincell[]{WANDERING_FORK, SCOUTING_FORK, FORAGING_FORK, FARMING_FORK, NURSING_FORK, TIDYING_FORK, EXCAVATING_FORK, ATTACKING_FORK, LATCHING_FORK});
}
