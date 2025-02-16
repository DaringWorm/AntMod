package com.daringworm.antmod.entity.brains.parts;

public class QueenBrainCells {





                                                    //EXCAVATE\\

    public static final Braincell EXCAVATE_BLOCK = new Braincell("Excavate block", AntPredicates.FOUND_INTEREST_BLOCK, new Action[]{Actions.DROP_ITEM, Actions.EXCAVATE_INTEREST_POS, Actions.WALK_TO_BLOCK});




    public static final Braincell WANDERING_FORK = new Braincell("Wandering", AntPredicates.IS_WANDERING);
    public static final Braincell SCOUTING_FORK = new Braincell("Scouting", AntPredicates.IS_SCOUTING);
    public static final Braincell FORAGING_FORK = new Braincell("Foraging", AntPredicates.IS_FORAGING);
    public static final Braincell FARMING_FORK = new Braincell("Farming", AntPredicates.IS_FARMING);
    public static final Braincell NURSING_FORK = new Braincell("Nursing", AntPredicates.IS_NURSING);
    public static final Braincell TIDYING_FORK = new Braincell("Tidying", AntPredicates.IS_TIDYING);
    public static final Braincell EXCAVATING_FORK = new Braincell("Excavating", AntPredicates.IS_EXCAVATING, new Braincell[]{EXCAVATE_BLOCK}, new Action[]{Actions.SET_EXCAVATION_POS_TO_INTEREST});
    public static final Braincell ATTACKING_FORK = new Braincell("Attacking", AntPredicates.IS_ATTACKING);
    public static final Braincell LATCHING_FORK = new Braincell("Latching", AntPredicates.IS_LATCHING);


    public static final Braincell MAIN_FORK = new Braincell("Mainfork", AntPredicates.TRUE).setSubCells(new Braincell[]{WANDERING_FORK, SCOUTING_FORK, FORAGING_FORK, FARMING_FORK, NURSING_FORK, TIDYING_FORK, EXCAVATING_FORK, ATTACKING_FORK, LATCHING_FORK});
}
