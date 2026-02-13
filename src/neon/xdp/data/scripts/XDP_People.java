package neon.xdp.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;

public class XDP_People {

    public static String INVICTA = "xdp_invicta";



    public static PersonAPI getPerson(String id){
        return Global.getSector().getImportantPeople().getPerson(id);
    }

    public void xdp_createPeople() {
    }
}
