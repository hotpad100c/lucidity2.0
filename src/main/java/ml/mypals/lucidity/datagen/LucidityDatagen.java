package ml.mypals.lucidity.datagen;

import ml.mypals.lucidity.Lucidity;
import ml.mypals.lucidity.datagen.lang.LucidityLanguageGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;

public class LucidityDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        LucidityLanguageGenerator lucidityLanguageGenerator = new LucidityLanguageGenerator(fabricDataGenerator.getRegistries());
        Lucidity.LOGGER.info("Generating lang data...");
        lucidityLanguageGenerator.generateTranslations(pack);
    }
}
