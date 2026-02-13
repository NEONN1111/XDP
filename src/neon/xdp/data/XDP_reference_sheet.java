package neon.xdp.data;

import com.fs.starfarer.api.util.WeightedRandomPicker;

public class XDP_reference_sheet {

    // Static initialization
    public static final WeightedRandomPicker<String> meatsoundlist = new WeightedRandomPicker<>();

    // commodity/special items


    public static final String XDP_AISWITCHAUTOMATED = "XDP_aiswitch_auto";
    public static final String XDP_AISWITCHMANUAL = "XDP_aiswitch_manual";


    // Constructor (init block from Kotlin)
    public XDP_reference_sheet() {
        // Empty constructor as the init block only contained static initialization
    }
}