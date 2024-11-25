package com.daringworm.antmod.entity.brains;

public abstract class AbstractBrain {
    int shortTermMem = 0;
    public int getMemForLevel(int level){return (shortTermMem%(10*(level+1)))/(level-1);}
    public void setMemForLevel(int level, int setter){
        int toSubtract = getMemForLevel(level)*(int)Math.pow(10,level);
        int subtracted = shortTermMem-toSubtract;
        this.shortTermMem = subtracted + (setter*(int)Math.pow(10,level));
    }
    public void tierUpMemLevel(int level){shortTermMem = shortTermMem+(int)Math.pow(10,level);}
    private boolean actionInProgress;
    public boolean isInProgress(){return this.actionInProgress;}
    public void setInProgress(boolean setter){actionInProgress = setter;}


}
