package neon.xdp.data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class XDP_gen implements SectorGeneratorPlugin {
    public static Logger log = Global.getLogger(XDP_gen.class);

    //Generate Systems
    @Override
    public void generate(SectorAPI sector) {
        new XDP_gate().generate(sector);
    }
}

