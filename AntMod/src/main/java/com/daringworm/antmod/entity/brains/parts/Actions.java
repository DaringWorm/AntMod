package com.daringworm.antmod.entity.brains.parts;

public class Actions {
    public static final Action ATTACK_ENTITY = new Action.AttackEntityAction();
    public static final Action WALK_TO_TARGET = new Action.MoveToEntityAction();
    public static final Action SET_AGGRESSOR_AS_TARGET = new Action.SetAggressorAsTargetAction();
    public static final Action ERROR_MSG_ACTION = new Action.ErrorAlertAction();
    public static final Action WALK_TO_BLOCK = new Action.MoveToBlockAction();
    public static final Action BREAK_INTEREST_BLOCK = new Action.BreakBlockAction();
    public static final Action FIND_INTEREST_BLOCK = new Action.FindSnippableBlockAction();
    public static final Action SELECT_ITEM_TO_TARGET = new Action.SelectItemToPickupAction();
    public static final Action PICKUP_ITEM = new Action.PickupItemAction();
    public static final Action SET_CONTAINER_TO_INTEREST = new Action.FindEmptyContainerAction();
    public static final Action PLACE_ITEM_IN_CONTAINER = new Action.PlaceItemInContainerAction();
    public static final Action SET_CONTAINER_POS_TO_INTEREST = new Action.SetContainerAsInterestAction();
    public static final Action SET_EXCAVATION_POS_TO_INTEREST = new Action.SetExcavationBlockAsInterestAction();
    public static final Action EXCAVATE_INTEREST_POS = new Action.ExcavateColonyBlockAction();
    public static final Action LATCH_ON = new Action.LatchOnTargetAction();
    public static final Action SCOUT = new Action.ScoutAction();
    public static final Action GO_UNDERGROUND = new Action.GoUndergroundAction();
    public static final Action GO_ABOVEGROUND = new Action.GoAbovegroundAction();

}
