/*
By Tartiflette
 */
package neon.xdp.data.scripts.util;

import com.fs.starfarer.api.Global;

public class XDP_txt {
    private static final String ML="seeker";    
    
    public static String txt(String id){
        return Global.getSettings().getString(ML, id);
    }       
}