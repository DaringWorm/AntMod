package com.daringworm.antmod.colony.misc;

import java.util.UUID;

public class PlayerPopularity {
    public int popularity;
    public String pID;

    public PlayerPopularity(String id, int pPopularity){
        this.pID = id;
        if(pPopularity>200){this.popularity = 200;} else if(pPopularity < -200){this.popularity = -200;} else if(pPopularity > -200 && pPopularity < 200){this.popularity = pPopularity;}
    }

    public void modifyPopularity(int change){
        if((this.popularity + change)<-200){this.popularity = -200;}
        else if((this.popularity + change)>200){this.popularity = 200;}
        else if((this.popularity + change)>-200 && (this.popularity + change) < 200){this.popularity = this.popularity+change;}
    }
}

