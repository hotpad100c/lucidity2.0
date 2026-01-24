package ml.mypals.lucidity.datagen.lang;

import ml.mypals.lucidity.Lucidity;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LucidityLanguageProvider extends FabricLanguageProvider {

    private final Map<String, String> translations = new HashMap<>();


    protected LucidityLanguageProvider(FabricDataOutput dataOutput, String languageCode, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, languageCode, registryLookup);
    }

    public void addTranslation(String key, String value) {
        translations.put(key, value);
    }
    @Override
    public void generateTranslations(HolderLookup.Provider provider, TranslationBuilder translationBuilder) {
        Lucidity.LOGGER.info("Filling " + translations.size() +" translations...");
        translations.forEach(translationBuilder::add);
    }
}