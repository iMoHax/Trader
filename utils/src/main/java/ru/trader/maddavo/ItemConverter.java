package ru.trader.maddavo;

import java.util.HashMap;
import java.util.Map;

public class ItemConverter {
    private final static Map<String, String> IDS = new HashMap<>(85, 0.9f);


    static {
        IDS.put("Explosives", "explosives");
        IDS.put("Hydrogen Fuel", "hydrogenfuel");
        IDS.put("Mineral Oil", "mineraloil");
        IDS.put("Pesticides", "pesticides");
        IDS.put("Clothing", "clothing");
        IDS.put("Consumer Technology", "consumertechnology");
        IDS.put("Domestic Appliances", "domesticappliances");
        IDS.put("Algae", "algae");
        IDS.put("Animal Meat", "animalmeat");
        IDS.put("Coffee", "coffee");
        IDS.put("Energy Drinks", "energydrinks");
        IDS.put("Fish", "fish");
        IDS.put("Food Cartridges", "foodcartridges");
        IDS.put("Fruit And Vegetables", "fruitandvegetables");
        IDS.put("Grain", "grain");
        IDS.put("Synthetic Meat", "syntheticmeat");
        IDS.put("Tea", "tea");
        IDS.put("Polymers", "polymers");
        IDS.put("Semiconductors", "semiconductors");
        IDS.put("Superconductors", "superconductors");
        IDS.put("Legal Drugs", "group.drugs");
        IDS.put("Beer", "beer");
        IDS.put("Liquor", "liquor");
        IDS.put("Narcotics", "basicnarcotics");
        IDS.put("Tobacco", "tobacco");
        IDS.put("Wine", "wine");
        IDS.put("Atmospheric Processors", "atmosphericprocessors");
        IDS.put("Crop Harvesters", "cropharvesters");
        IDS.put("Marine Equipment", "marinesupplies");
        IDS.put("Microbial Furnaces", "microbialfurnaces");
        IDS.put("Mineral Extractors", "mineralextractors");
        IDS.put("Power Generators", "powergenerators");
        IDS.put("Water Purifiers", "waterpurifiers");
        IDS.put("Agri-Medicines", "agriculturalmedicines");
        IDS.put("Basic Medicines", "basicmedicines");
        IDS.put("Combat Stabilisers", "combatstabilisers");
        IDS.put("Performance Enhancers", "performanceenhancers");
        IDS.put("Progenitor Cells", "progenitorcells");
        IDS.put("Aluminium", "aluminium");
        IDS.put("Beryllium", "beryllium");
        IDS.put("Copper", "copper");
        IDS.put("Cobalt", "cobalt");
        IDS.put("Gallium", "gallium");
        IDS.put("Gold", "gold");
        IDS.put("Indium", "indium");
        IDS.put("Lithium", "lithium");
        IDS.put("Palladium", "palladium");
        IDS.put("Platinum", "platinum");
        IDS.put("Tantalum", "tantalum");
        IDS.put("Titanium", "titanium");
        IDS.put("Silver", "silver");
        IDS.put("Uranium", "uranium");
        IDS.put("Minerals", "group.minerals");
        IDS.put("Bauxite", "bauxite");
        IDS.put("Bertrandite", "bertrandite");
        IDS.put("Coltan", "coltan");
        IDS.put("Gallite", "gallite");
        IDS.put("Indite", "indite");
        IDS.put("Lepidolite", "lepidolite");
        IDS.put("Rutile", "rutile");
        IDS.put("Uraninite", "uraninite");
        IDS.put("Imperial Slaves", "imperialslaves");
        IDS.put("Slaves", "slaves");
        IDS.put("Advanced Catalysers", "advancedcatalysers");
        IDS.put("Animal Monitors", "animalmonitors");
        IDS.put("Aquaponic Systems", "aquaponicsystems");
        IDS.put("Auto-Fabricatos", "autofabricators");
        IDS.put("Bioreducing Lichen", "bioreducinglichen");
        IDS.put("Computer Components", "computercomponents");
        IDS.put("H.E. Suits", "hazardousenvironmentsuits");
        IDS.put("Resonating Separators", "resonatingseparators");
        IDS.put("Robotics", "robotics");
        IDS.put("Land Enrichment Systems", "landenrichmentsystems");
        IDS.put("Leather", "leather");
        IDS.put("Natural Fabrics", "naturalfabrics");
        IDS.put("Synthetic Fabrics", "syntheticfabrics");
        IDS.put("Biowaste", "biowaste");
        IDS.put("Chemical Waste", "chemicalwaste");
        IDS.put("Scrap", "scrap");
        IDS.put("Battle Weapons", "battleweapons");
        IDS.put("Non-Lethal Weapons", "nonlethalweapons");
        IDS.put("Personal Weapons", "personalweapons");
        IDS.put("Reactive Armour", "reactivearmour");
    }

    public static String getItemId(String name){
        String id = IDS.get(name);
        return  id != null ? id : name;
    }
}
