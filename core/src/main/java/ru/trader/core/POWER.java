package ru.trader.core;

public enum POWER {
    DUVAL {
        // Exploited Systems: Imperial Slaves banned
        // Control Systems: Imperial Slaves banned
        @Override
        public boolean isIllegal(FACTION faction, Item item, POWER_STATE state) {
            if (state != null && (state.isControl() || state.isExploited())){
                String itemId = item.getName();
                return itemId != null && IMPERIAL_SLAVES.equals(itemId);
            }
            return super.isIllegal(faction, item, state);
        }
    },
    DELAINE {
        // Control Systems: All weapons/slaves/narcotics/medicals legalised
        @Override
        public boolean isLegal(FACTION faction, Item item, POWER_STATE state) {
            if (state != null && state.isControl()){
                String groupId = item.getGroup() != null ? item.getGroup().getName() : null;
                return groupId != null && (WEAPONS_GRP.equals(groupId) || SLAVES_GRP.equals(groupId) || NARCOTICS_GRP.equals(groupId) || MEDICINE_GRP.equals(groupId));
            }
            return super.isLegal(faction, item, state);
        }
    },
    LAVIGNY_DUVAL,
    PATREUS {
        // Control Systems: Imperial Slaves legalised
        @Override
        public boolean isLegal(FACTION faction, Item item, POWER_STATE state) {
            if (state != null && state.isControl()){
                String itemId = item.getName();
                return itemId != null && IMPERIAL_SLAVES.equals(itemId);
            }
            return super.isLegal(faction, item, state);
        }
    },
    MAHON,
    WINTERS {
        // Exploited Federal systems: Imperial Slaves banned
        // Exploited Alliance/Ind systems: Imperial Slaves banned
        // Control Systems: Imperial Slaves banned
        @Override
        public boolean isIllegal(FACTION faction, Item item, POWER_STATE state) {
            if (state != null && state.isControl()){
                String itemId = item.getName();
                return itemId != null && IMPERIAL_SLAVES.equals(itemId);
            } else
            if (state != null && state.isExploited()){
                String itemId = item.getName();
                return itemId != null && IMPERIAL_SLAVES.equals(itemId) && (faction != null && faction != FACTION.EMPIRE);
            }
            return super.isIllegal(faction, item, state);
        }
    },
    YONG_RUI,
    ANTAL   {
        // Exploited Systems: All Slaves, Narcotics and non-basic/agri medicines banned
        // Control Systems: All Slaves, Narcotics and non-basic/agri medicines banned
        @Override
        public boolean isIllegal(FACTION faction, Item item, POWER_STATE state) {
            if (state != null && (state.isControl() || state.isExploited())){
                String groupId = item.getGroup() != null ? item.getGroup().getName() : null;
                String itemId = item.getName();
                return groupId != null && (SLAVES_GRP.equals(groupId) || NARCOTICS_GRP.equals(groupId)
                        || MEDICINE_GRP.equals(groupId) && !BASIC_MEDICINES.equals(itemId) && !AGRI_MEDICINE.equals(itemId)
                );
            }
            return super.isIllegal(faction, item, state);
        }
    },
    HUDSON {
        // Control Systems: Imperial Slaves banned
        @Override
        public boolean isIllegal(FACTION faction, Item item, POWER_STATE state) {
            if (state != null && state.isControl()){
                String itemId = item.getName();
                return itemId != null && IMPERIAL_SLAVES.equals(itemId);
            }
            return super.isIllegal(faction, item, state);
        }
    },
    TORVAL {
        // Control Systems: Imperial Slaves legalised
        @Override
        public boolean isLegal(FACTION faction, Item item, POWER_STATE state) {
            if (state != null && state.isControl()){
                String itemId = item.getName();
                return itemId != null && IMPERIAL_SLAVES.equals(itemId);
            }
            return super.isLegal(faction, item, state);
        }
    },
    GROM,
    NONE;

    private final static String WEAPONS_GRP="weapons";
    private final static String NARCOTICS_GRP="drugs";
    private final static String SLAVES_GRP="slaves";
    private final static String MEDICINE_GRP="medicines";
    private final static String IMPERIAL_SLAVES="imperialslaves";
    private final static String BASIC_MEDICINES="basicmedicines";
    private final static String AGRI_MEDICINE="agriculturalmedicines";

    public boolean isLegal(FACTION faction, Item item, POWER_STATE state){
        return false;
    }

    public boolean isIllegal(FACTION faction, Item item, POWER_STATE state){
        return false;
    }

}
