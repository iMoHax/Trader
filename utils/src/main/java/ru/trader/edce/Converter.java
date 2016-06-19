package ru.trader.edce;

import ru.trader.edce.entities.Commodity;
import ru.trader.edce.entities.ShipyardItem;

import java.util.HashMap;
import java.util.Map;

public class Converter {

    private final static Map<Long, String> ITEM_ID = new HashMap<>(85, 0.9f);
    private final static Map<String, String> GROUP_ID = new HashMap<>(10, 0.9f);
    private final static Map<Long, String> SHIP_ID = new HashMap<>(30, 0.9f);

    static {
        ITEM_ID.put(128049204L, "explosives");
        ITEM_ID.put(128049202L, "hydrogenfuel");
        ITEM_ID.put(128049203L, "mineraloil");
        ITEM_ID.put(128672304L, "nerve_agents");
        ITEM_ID.put(128049205L, "pesticides");
        ITEM_ID.put(128672305L, "surface_stabilisers");
        ITEM_ID.put(128672303L, "synthetic_reagents");
        ITEM_ID.put(128049241L, "clothing");
        ITEM_ID.put(128049240L, "consumertechnology");
        ITEM_ID.put(128049238L, "domesticappliances");
        ITEM_ID.put(128672314L, "evacuation_shelter");
        ITEM_ID.put(128049177L, "algae");
        ITEM_ID.put(128049182L, "animalmeat");
        ITEM_ID.put(128049189L, "coffee");
        ITEM_ID.put(128049183L, "fish");
        ITEM_ID.put(128049184L, "foodcartridges");
        ITEM_ID.put(128049178L, "fruitandvegetables");
        ITEM_ID.put(128049180L, "grain");
        ITEM_ID.put(128049185L, "syntheticmeat");
        ITEM_ID.put(128049188L, "tea");
        ITEM_ID.put(128672302L, "ceramic_composites");
        ITEM_ID.put(128672701L, "meta_alloys");
        ITEM_ID.put(128049197L, "polymers");
        ITEM_ID.put(128049199L, "semiconductors");
        ITEM_ID.put(128049200L, "superconductors");
        ITEM_ID.put(128049214L, "beer");
        ITEM_ID.put(128672306L, "bootleg_liquor");
        ITEM_ID.put(128049216L, "liquor");
        ITEM_ID.put(128049212L, "basicnarcotics");
        ITEM_ID.put(128049213L, "tobacco");
        ITEM_ID.put(128049215L, "wine");
        ITEM_ID.put(128064028L, "atmosphericprocessors");
        ITEM_ID.put(128672309L, "building_fabricators");
        ITEM_ID.put(128049222L, "cropharvesters");
        ITEM_ID.put(128672307L, "geological_equipment");
        ITEM_ID.put(128049223L, "marinesupplies");
        ITEM_ID.put(128049220L, "microbialfurnaces");
        ITEM_ID.put(128049221L, "mineralextractors");
        ITEM_ID.put(128049217L, "powergenerators");
        ITEM_ID.put(128672313L, "skimer_components");
        ITEM_ID.put(128672308L, "thermal_cooling_units");
        ITEM_ID.put(128049218L, "waterpurifiers");
        ITEM_ID.put(128049208L, "agriculturalmedicines");
        ITEM_ID.put(128049210L, "basicmedicines");
        ITEM_ID.put(128049670L, "combatstabilisers");
        ITEM_ID.put(128049209L, "performanceenhancers");
        ITEM_ID.put(128049669L, "progenitorcells");
        ITEM_ID.put(128049176L, "aluminium");
        ITEM_ID.put(128049168L, "beryllium");
        ITEM_ID.put(128672300L, "bismuth");
        ITEM_ID.put(128049162L, "cobalt");
        ITEM_ID.put(128049175L, "copper");
        ITEM_ID.put(128049170L, "gallium");
        ITEM_ID.put(128049154L, "gold");
        ITEM_ID.put(128049169L, "indium");
        ITEM_ID.put(128672298L, "lanthanum");
        ITEM_ID.put(128049173L, "lithium");
        ITEM_ID.put(128671118L, "osmium");
        ITEM_ID.put(128049153L, "palladium");
        ITEM_ID.put(128049152L, "platinum");
        ITEM_ID.put(128049155L, "silver");
        ITEM_ID.put(128049171L, "tantalum");
        ITEM_ID.put(128672299L, "thallium");
        ITEM_ID.put(128672301L, "thorium");
        ITEM_ID.put(128049174L, "titanium");
        ITEM_ID.put(128049172L, "uranium");
        ITEM_ID.put(128049165L, "bauxite");
        ITEM_ID.put(128049156L, "bertrandite");
        ITEM_ID.put(128049159L, "coltan");
        ITEM_ID.put(128672294L, "cryolite");
        ITEM_ID.put(128049158L, "gallite");
        ITEM_ID.put(128672295L, "goslarite");
        ITEM_ID.put(128049157L, "indite");
        ITEM_ID.put(128049161L, "lepidolite");
        ITEM_ID.put(128668550L, "painite");
        ITEM_ID.put(128672297L, "pyrophyllite");
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
        ITEM_ID.put(128049232L, "landenrichmentsystems");
        ITEM_ID.put(128672310L, "mu_tom_imager");
        ITEM_ID.put(128049671L, "resonatingseparators");
        ITEM_ID.put(128049227L, "robotics");
        ITEM_ID.put(128672311L, "structural_regulators");
        ITEM_ID.put(128049190L, "leather");
        ITEM_ID.put(128049191L, "naturalfabrics");
        ITEM_ID.put(128049193L, "syntheticfabrics");
        ITEM_ID.put(128049244L, "biowaste");
        ITEM_ID.put(128049246L, "chemicalwaste");
        ITEM_ID.put(128049248L, "scrap");
        ITEM_ID.put(128049234L, "battleweapons");
        ITEM_ID.put(128672312L, "landmines");
        ITEM_ID.put(128049236L, "nonlethalweapons");
        ITEM_ID.put(128049233L, "personalweapons");
        ITEM_ID.put(128049235L, "reactivearmour");

        ITEM_ID.put(128673850L, "hydrogen_peroxide");
        ITEM_ID.put(128673851L, "liquid_oxygen");
        ITEM_ID.put(128049166L, "water");
        ITEM_ID.put(128682048L, "survival_equipment");
        ITEM_ID.put(128673856L, "cmm_composite");
        ITEM_ID.put(128673855L, "insulating_membrane");
        ITEM_ID.put(128673861L, "emergency_power_cells");
        ITEM_ID.put(128673866L, "exhaust_manifold");
        ITEM_ID.put(128673860L, "hn_shock_mount");
        ITEM_ID.put(128682046L, "advanced_medicines");
        ITEM_ID.put(128673845L, "praseodymium");
        ITEM_ID.put(128673847L, "samarium");
        ITEM_ID.put(128673848L, "low_temperature_diamond");
        ITEM_ID.put(128673873L, "micro_controllers");
        ITEM_ID.put(128682044L, "conductive_fabrics");
        ITEM_ID.put(128673853L, "lithium_hydroxide");
        ITEM_ID.put(128673854L, "methane_clathrate");
        ITEM_ID.put(128682045L, "military_grade_fabrics");
        ITEM_ID.put(128673868L, "heatsink_interlink");
        ITEM_ID.put(128673869L, "magnetic_emitter_coil");
        ITEM_ID.put(128673862L, "power_converter");
        ITEM_ID.put(128673863L, "power_grid_assembly");
        ITEM_ID.put(128673846L, "bromellite");
        ITEM_ID.put(128673852L, "methanol_monohydrate_crystals");
        ITEM_ID.put(128673875L, "diagnostic_sensor");
        ITEM_ID.put(128682047L, "medical_diagnostic_equipment");
        ITEM_ID.put(128673867L, "reinforced_mounting_plate");
        ITEM_ID.put(128673859L, "articulation_motors");
        ITEM_ID.put(128673870L, "modular_terminals");
        ITEM_ID.put(128673864L, "power_transfer_conduits");
        ITEM_ID.put(128673865L, "radiation_baffle");
        ITEM_ID.put(128673874L, "ion_distributor");
        ITEM_ID.put(128673871L, "nanobreakers");
        ITEM_ID.put(128673872L, "telemetry_suite");
        ITEM_ID.put(128672776L, "jadeite");
        ITEM_ID.put(128672296L, "moissanite");
        ITEM_ID.put(128672775L, "taaffeite");
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


        SHIP_ID.put(128049249L,"sidewinder");
        SHIP_ID.put(128049261L, "hauler");
        SHIP_ID.put(128049255L, "eagle");
        SHIP_ID.put(128049267L, "adder");
        SHIP_ID.put(128672138L, "imperial_eagle");
        SHIP_ID.put(128049273L, "viper");
        SHIP_ID.put(128049279L, "cobraMk3");
        SHIP_ID.put(128672255L, "viperMkIV");
        SHIP_ID.put(128672262L, "cobraMkIV");
        SHIP_ID.put(128671217L, "diamondback_scout");
        SHIP_ID.put(128049285L, "type6");
        SHIP_ID.put(128672269L, "keelback");
        SHIP_ID.put(128671831L, "diamondback_explorer");
        SHIP_ID.put(128049309L, "vulture");
        SHIP_ID.put(128672276L, "asp_scout");
        SHIP_ID.put(128049303L, "asp");
        SHIP_ID.put(128049297L, "type7");
        SHIP_ID.put(128049315L, "imperial_clipper");
        SHIP_ID.put(128671223L, "imperial_courier");
        SHIP_ID.put(128049321L, "federal_dropship");
        SHIP_ID.put(128672145L, "federal_assault_ship");
        SHIP_ID.put(128672152L, "federal_gunship");
        SHIP_ID.put(128049327L, "orca");
        SHIP_ID.put(128049351L, "fer_de_lance");
        SHIP_ID.put(128049339L, "python");
        SHIP_ID.put(128049333L, "type9");
        SHIP_ID.put(128049363L, "anaconda");
        SHIP_ID.put(128049369L, "federation_corvette");
        SHIP_ID.put(128049375L, "cutter");

    }

    public static String getItemId(Commodity commodity){
        String id = ITEM_ID.get(commodity.getId());
        if (id == null){
            id = commodity.getName().toLowerCase().replace(" ","_");
        }
        return id;
    }

    public static String getGroupId(String edName){
        return GROUP_ID.get(edName);
    }

    public static String getShipId(ShipyardItem ship){
        String id = SHIP_ID.get(ship.getId());
        if (id == null){
            id = ship.getName().toLowerCase().replace(" ","_");
        }
        return id;
    }

}
