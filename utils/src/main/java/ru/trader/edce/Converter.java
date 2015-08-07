package ru.trader.edce;

import java.util.HashMap;
import java.util.Map;

public class Converter {

    private final static Map<Long, String> ITEM_ID = new HashMap<>(85, 0.9f);
    private final static Map<String, String> GROUP_ID = new HashMap<>(10, 0.9f);

    static {
        ITEM_ID.put(128049204L, "explosives");
        ITEM_ID.put(128049202L, "hydrogenfuel");
        ITEM_ID.put(128049203L, "mineraloil");
        ITEM_ID.put(128049205L, "pesticides");
        ITEM_ID.put(128049241L, "clothing");
        ITEM_ID.put(128049240L, "consumertechnology");
        ITEM_ID.put(128049238L, "domesticappliances");
        ITEM_ID.put(128049177L, "algae");
        ITEM_ID.put(128049182L, "animalmeat");
        ITEM_ID.put(128049189L, "coffee");
        ITEM_ID.put(128049183L, "fish");
        ITEM_ID.put(128049184L, "foodcartridges");
        ITEM_ID.put(128049178L, "fruitandvegetables");
        ITEM_ID.put(128049180L, "grain");
        ITEM_ID.put(128049185L, "syntheticmeat");
        ITEM_ID.put(128049188L, "tea");
        ITEM_ID.put(128049197L, "polymers");
        ITEM_ID.put(128049199L, "semiconductors");
        ITEM_ID.put(128049200L, "superconductors");
        ITEM_ID.put(128049214L, "beer");
        ITEM_ID.put(128049216L, "liquor");
        ITEM_ID.put(128049212L, "basicnarcotics");
        ITEM_ID.put(128049213L, "tobacco");
        ITEM_ID.put(128049215L, "wine");
        ITEM_ID.put(128064028L, "atmosphericprocessors");
        ITEM_ID.put(128049222L, "cropharvesters");
        ITEM_ID.put(128049223L, "marinesupplies");
        ITEM_ID.put(128049220L, "microbialfurnaces");
        ITEM_ID.put(128049221L, "mineralextractors");
        ITEM_ID.put(128049217L, "powergenerators");
        ITEM_ID.put(128049218L, "waterpurifiers");
        ITEM_ID.put(128049208L, "agriculturalmedicines");
        ITEM_ID.put(128049210L, "basicmedicines");
        ITEM_ID.put(128049670L, "combatstabilisers");
        ITEM_ID.put(128049209L, "performanceenhancers");
        ITEM_ID.put(128049669L, "progenitorcells");
        ITEM_ID.put(128049176L, "aluminium");
        ITEM_ID.put(128049168L, "beryllium");
        ITEM_ID.put(128049162L, "cobalt");
        ITEM_ID.put(128049175L, "copper");
        ITEM_ID.put(128049170L, "gallium");
        ITEM_ID.put(128049154L, "gold");
        ITEM_ID.put(128049169L, "indium");
        ITEM_ID.put(128049173L, "lithium");
        ITEM_ID.put(128671118L, "osmium");
        ITEM_ID.put(128049153L, "palladium");
        ITEM_ID.put(128049152L, "platinum");
        ITEM_ID.put(128049155L, "silver");
        ITEM_ID.put(128049171L, "tantalum");
        ITEM_ID.put(128049174L, "titanium");
        ITEM_ID.put(128049172L, "uranium");
        ITEM_ID.put(128049165L, "bauxite");
        ITEM_ID.put(128049156L, "bertrandite");
        ITEM_ID.put(128049159L, "coltan");
        ITEM_ID.put(128049158L, "gallite");
        ITEM_ID.put(128049157L, "indite");
        ITEM_ID.put(128049161L, "lepidolite");
        ITEM_ID.put(128668550L, "painite");
        ITEM_ID.put(128049163L, "rutile");
        ITEM_ID.put(128049160L, "uraninite");
        ITEM_ID.put(128667728L, "imperialslaves");
        ITEM_ID.put(128066403L, "");  // Drones
        ITEM_ID.put(128671443L, "sap8corecontainer");
        ITEM_ID.put(128049231L, "advancedcatalysers");
        ITEM_ID.put(128049229L, "animalmonitors");
        ITEM_ID.put(128049230L, "aquaponicsystems");
        ITEM_ID.put(128049228L, "autofabricators");
        ITEM_ID.put(128049672L, "bioreducinglichen");
        ITEM_ID.put(128049225L, "computercomponents");
        ITEM_ID.put(128049226L, "hazardousenvironmentsuits");
        ITEM_ID.put(128049671L, "resonatingseparators");
        ITEM_ID.put(128049227L, "robotics");
        ITEM_ID.put(128049232L, "landenrichmentsystems");
        ITEM_ID.put(128049190L, "leather");
        ITEM_ID.put(128049191L, "naturalfabrics");
        ITEM_ID.put(128049193L, "syntheticfabrics");
        ITEM_ID.put(128049244L, "biowaste");
        ITEM_ID.put(128049246L, "chemicalwaste");
        ITEM_ID.put(128049248L, "scrap");
        ITEM_ID.put(128049234L, "battleweapons");
        ITEM_ID.put(128049236L, "nonlethalweapons");
        ITEM_ID.put(128049233L, "personalweapons");
        ITEM_ID.put(128049235L, "reactivearmour");

        GROUP_ID.put("Chemicals", "chemicals");
        GROUP_ID.put("Consumer Items", "consumer_items");
        GROUP_ID.put("Foods", "foods");
        GROUP_ID.put("Industrial Materials", "engineered_ceramics"); // Polymers
        GROUP_ID.put("Machinery", "machinery");
        GROUP_ID.put("Medicines", "medicines");
        GROUP_ID.put("Metals", "metals");
        GROUP_ID.put("Minerals", "minerals");
        GROUP_ID.put("Narcotics", "drugs");
        GROUP_ID.put("NonMarketable", "");  // Drones
        GROUP_ID.put("Salvage", "salvage"); // S A P8 Core Container
        GROUP_ID.put("Slaves", "slaves");
        GROUP_ID.put("Technology", "technology"); // Aquaponic Systems
        GROUP_ID.put("Textiles", "textiles");
        GROUP_ID.put("Waste ", "waste");
        GROUP_ID.put("Weapons", "weapons");

    }

    public static String getItemId(long edId){
        return ITEM_ID.get(edId);
    }

    public static String getGroupId(String edName){
        return GROUP_ID.get(edName);
    }

}
