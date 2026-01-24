package ml.mypals.lucidity.utils;

import fi.dy.masa.malilib.config.options.ConfigStringList;

import java.util.List;

public class LucidityConfigHelper {

    public static void addToConfigList(ConfigStringList target, String string) {
        addToConfigList(target, string, true);
    }
    public static void addToConfigList(ConfigStringList target, String string,boolean markDirty){
        List<String> newValues = target.getStrings();
        newValues.add(string);
        target.setStrings(newValues);
        if(markDirty)target.setModified();
    }

    public static void removeFromConfigList(ConfigStringList target, String string) {
        removeFromConfigList(target, string, true);
    }
    public static void removeFromConfigList(ConfigStringList target, String string,boolean markDirty){
        List<String> newValues = target.getStrings();
        newValues.remove(string);
        target.setStrings(newValues);
        if(markDirty) target.setModified();
    }
    public static void setInConfigList(ConfigStringList target,int id, String string) {
        removeFromConfigList(target, string, true);
    }
    public static void setInConfigList(ConfigStringList target,int id, String string,boolean markDirty){
        List<String> newValues = target.getStrings();
        newValues.set(id, string);
        target.setStrings(newValues);
        if(markDirty) target.setModified();
    }
}
