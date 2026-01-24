package ml.mypals.lucidity.datagen.lang;

import ml.mypals.lucidity.Lucidity;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.HolderLookup;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LucidityLanguageProviderManager {
    private LinkedHashMap<String,Map<String,String>> translations = new LinkedHashMap<>();
    private CompletableFuture<HolderLookup.Provider> registries;
    public LucidityLanguageProviderManager(CompletableFuture<HolderLookup.Provider> registriesFuture){
        this.registries = registriesFuture;
    }
    public LucidityLanguageProviderManager declareProvider(String language){
        translations.put(language,new HashMap<>());
        return this;
    }
    public void addTranslation(String key, String... translationsArray) {
        if (translationsArray.length != translations.size()) {
            throw new IllegalArgumentException("Translations array length must match languages count");
        }

        int i = 0;
        for (Map.Entry<String, Map<String, String>> entry : translations.entrySet()) {
            entry.getValue().put(key, translationsArray[i++]);
        }
    }
    public void submit(FabricDataGenerator.Pack pack) {
        translations.forEach((lang, entries) ->
            pack.addProvider((FabricDataGenerator.Pack.Factory<LucidityLanguageProvider>) (dataOutput)-> {
                LucidityLanguageProvider provider = new LucidityLanguageProvider(dataOutput,lang,this.registries);
                entries.forEach(provider::addTranslation);
                Lucidity.LOGGER.info("Submitted lang provider for " + lang);
                return provider;
            })
        );
    }
}
