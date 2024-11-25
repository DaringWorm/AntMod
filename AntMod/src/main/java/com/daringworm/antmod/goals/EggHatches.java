package com.daringworm.antmod.goals;

import com.daringworm.antmod.entity.ModEntityTypes;
import com.daringworm.antmod.entity.custom.AntEgg;
import com.daringworm.antmod.entity.custom.AntLarva;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

public class EggHatches extends Goal {

    private final int HUNGER_LOSS_PER_EGG = 10;
    private final int SECONDS_BETWEEN_EGGS = 120;
    private final AntEgg egg;


    public EggHatches(AntEgg egg) {
        this.egg = egg;
    }

    public boolean canUse() {
        return this.egg.isAlive();
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
    }
}
