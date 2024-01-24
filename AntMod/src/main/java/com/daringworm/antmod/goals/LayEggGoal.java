package com.daringworm.antmod.goals;

import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.custom.AntEgg;
import com.daringworm.antmod.entity.custom.QueenAnt;

import net.minecraft.world.entity.ai.goal.Goal;

public class LayEggGoal extends Goal {

    private static final int HUNGER_LOSS_PER_EGG = 150;
    private static final int SECONDS_BETWEEN_EGGS = 30;
    private final QueenAnt queenAnt;
    

    public LayEggGoal(QueenAnt queenAnt) {
        this.queenAnt = queenAnt;
    }

    public boolean canUse() {
        if  (!this.queenAnt.isAlive()){
            return false;
        }
        return this.queenAnt.getHunger() >= 30;
    }

    public void start() {
        super.start();
    }


    public void stop() {
        super.stop();
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }


    public void tick() {
        super.tick();
        int ticksBetweenEggs = SECONDS_BETWEEN_EGGS *20;



        if(this.queenAnt.isAlive() && this.queenAnt.getHunger() >= 30 && this.queenAnt.getThisEggTimer() >= ticksBetweenEggs) {
            AntEgg egg = ModEntityTypes.ANTEGG.get().create(this.queenAnt.level);
            egg.setColonyID(this.queenAnt.getColonyID());
            egg.moveTo(this.queenAnt.getX(), this.queenAnt.getY(), this.queenAnt.getZ(), this.queenAnt.getYRot(), 0.0F);
            this.queenAnt.level.addFreshEntity(egg);

            this.queenAnt.setThisEggTimer(0);
            this.queenAnt.setHunger(this.queenAnt.getHunger()-HUNGER_LOSS_PER_EGG);
        }

        this.queenAnt.setThisEggTimer(1+this.queenAnt.getThisEggTimer());
    }
}
