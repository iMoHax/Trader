package ru.trader.maddavo;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;
import ru.trader.core.*;
import ru.trader.store.simple.Store;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;


public class PricesImportTest extends Assert {

    private static final String Strings =
                    "# <item name> <sell> <buy> <demand units><level> <stock units><level> <timestamp>\n" +
                    "#     Item Name              Sell Cr  Buy Cr     Demand      Stock  Timestamp\n" +
                    "\n" +
                    "@ 51 AQUILAE/Thirsk Station\n" +
                    "   + Chemicals\n" +
                    "      Hydrogen Fuel               106     111          ?   995704M  2015-02-28 06:15:53  # EObded06f0_EliteOCR_0.5.2.3\n" +
                    "      Mineral Oil                 127     143          ?   415528M  2015-02-28 06:15:53  # EObded06f0_EliteOCR_0.5.2.3\n" +
                    "      Pesticides                  362       0    790443H         -  2015-02-28 06:15:53  # EObded06f0_EliteOCR_0.5.2.3\n" +
                    "   + Minerals\n" +
                    "      Bauxite                      58      70          ?  2587092M  2015-02-28 06:25:16  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      Bertrandite                2295    2345          ?   137390M  2015-02-28 06:25:16  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      Coltan                     1225    1268          ?   217931M  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      Gallite                    1701    1738          ?   380162M  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      Indite                     2001    2044          ?   151604M  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      Lepidolite                  475     500          ?   447705M  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      Rutile                      234     252          ?   786446M  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      Uraninite                   669     694          ?   501917H  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "   + Technology\n" +
                    "      Bioreducing Lichen         1236       0   2654132H         -  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      H.E. Suits                  426       0   1727091H         -  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "   + Waste\n" +
                    "      Biowaste                     15      20          ?    32001M  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "   + Weapons\n" +
                    "      Non-Lethal Weapons         2191       0      1757H         -  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      Personal Weapons           4836       0     11393H         -  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "      Reactive Armour            2467       0      4778H         -  2015-02-28 06:25:21  # EOa679fe7b_EliteOCR_0.5.3\n" +
                    "@ BONDE/Aksyonov Platform\n" +
                    "   + Chemicals\n" +
                    "      Explosives                 209     224          ?      2680?  2015-01-23 14:26:43\n" +
                    "      Hydrogen Fuel              107     112          ?      3239?  2015-01-23 14:26:43\n" +
                    "      Mineral Oil                195       0      4734?          -  2015-01-23 14:26:43\n" +
                    "   + Consumer Items\n" +
                    "      Clothing                   319       0       426?          -  2015-01-23 14:26:43\n" +
                    "      Consumer Technology       6871       0        32?          -  2015-01-23 14:26:43\n" +
                    "      Domestic Appliances        533       0       223?          -  2015-01-23 14:26:43\n" +
                    "   + Foods\n" +
                    "      Animal Meat               1388       0        49?          -  2015-01-23 14:26:43\n" +
                    "      Coffee                    1388       0        72?          -  2015-01-23 14:26:43\n" +
                    "      Fish                       430       0       260?          -  2015-01-23 14:26:43\n" +
                    "      Food Cartridges            143       0       229?          -  2015-01-23 14:26:43\n" +
                    "      Fruit And Vegetables       319       0       261?          -  2015-01-23 14:26:43\n" +
                    "      Grain                      210       0       712?          -  2015-01-23 14:26:43\n" +
                    "      Synthetic Meat             255       0       168?          -  2015-01-23 14:26:43\n" +
                    "      Tea                       1570       0        54?          -  2015-01-23 14:26:43\n" +
                    "   + Industrial Materials\n" +
                    "      Polymers                    63       0          ?          -  2015-01-23 14:26:46\n" +
                    "      Semiconductors             767     788          ?      1441?  2015-01-23 14:26:46\n" +
                    "      Superconductors           6485    6556          ?      2937?  2015-01-23 14:26:46\n" +
                    "   + Legal Drugs\n" +
                    "      Beer                       177       0       675?          -  2015-01-23 14:26:46\n" +
                    "      Liquor                     862       0       393?          -  2015-01-23 14:26:46\n" +
                    "      Tobacco                   4758       0       131?          -  2015-01-23 14:26:46\n" +
                    "      Wine                       255       0       211?          -  2015-01-23 14:26:46\n" +
                    "   + Machinery\n" +
                    "      Microbial Furnaces         202       0     14989?          -  2015-01-23 14:26:46\n" +
                    "      Power Generators           533       0      1019?          -  2015-01-23 14:26:46\n" +
                    "      Water Purifiers            304       0       180?          -  2015-01-23 14:26:46\n" +
                    "   + Medicines\n" +
                    "      Basic Medicines            319     200       175?        30L  2015-01-23 14:26:46\n" +
                    "      Performance Enhancers     6871       0        75?          -  2015-01-23 14:26:46\n" +
                    "      Progenitor Cells          6871       0         9?          -  2015-01-23 14:26:46\n" +
                    "   + Metals\n" +
                    "      Aluminium                  236     252          ?      3626?  2015-01-23 14:26:46\n" +
                    "      Beryllium                 8010    8096          ?       250?  2015-01-23 14:26:48\n" +
                    "      Cobalt                     580     604          ?      1769?  2015-01-23 14:26:48\n" +
                    "      Copper                     373     389          ?     25340?  2015-01-23 14:26:48\n" +
                    "      Gallium                   4947    5001          ?      3603?  2015-01-23 14:26:48\n" +
                    "      Gold                      9289    9289          ?       306?  2015-01-23 14:26:48\n" +
                    "      Lithium                   1413    1448          ?       905?  2015-01-23 14:26:48\n" +
                    "      Silver                    4589    4640          ?       381?  2015-01-23 14:26:48\n" +
                    "      Tantalum                  3740    3783          ?       446?  2015-01-23 14:26:48\n" +
                    "      Titanium                   884     907          ?     13008?  2015-01-23 14:26:48\n" +
                    "      Uranium                   2467    2496          ?       599?  2015-01-23 14:26:48\n" +
                    "   + Minerals\n" +
                    "      Bauxite                    268       0     26144?          -  2015-01-23 14:26:48\n" +
                    "      Bertrandite               2467       0       424?          -  2015-01-23 14:26:48\n" +
                    "      Coltan                    1708       0      2466?          -  2015-01-23 14:26:48\n" +
                    "      Gallite                   2127       0      8867?          -  2015-01-23 14:26:48\n" +
                    "      Indite                    2634       0     12385?          -  2015-01-23 14:26:48\n" +
                    "      Lepidolite                 622       0      1946?          -  2015-01-23 14:26:48\n" +
                    "      Rutile                     493       0     31584?          -  2015-01-23 14:26:50\n" +
                    "      Uraninite                 1177       0     25404?          -  2015-01-23 14:26:50\n" +
                    "   + Technology\n" +
                    "      Advanced Catalysers       2809       0      2555?          -  2015-01-23 14:26:50\n" +
                    "      H.E. Suits                 278       0       995?          -  2015-01-23 14:26:50\n" +
                    "      Resonating Separators     5807       0       219?          -  2015-01-23 14:26:50\n" +
                    "   + Textiles\n" +
                    "      Synthetic Fabrics           90     118          ?      7438?  2015-01-23 14:26:50\n" +
                    "   + Waste\n" +
                    "      Biowaste                    15      20          ?       422?  2015-01-23 14:26:50\n" +
                    "      Chemical Waste             109       0      1761?          -  2015-01-23 14:26:50\n" +
                    "      Scrap                       71       0       736?          -  2015-01-23 14:26:50\n" +
                    "   + Weapons\n" +
                    "      Non-Lethal Weapons        1891       0        52?          -  2015-01-23 14:26:50\n" +
                    "      Reactive Armour           2145       0        39?          -  2015-01-23 14:26:50\n" +
                    "\n" +
                    "@ BONITOU/Hughes-Fulford Terminal\n" +
                    "   + Chemicals\n" +
                    "      Hydrogen Fuel              126       0    132112L          -  2014-12-30 17:33:30\n" +
                    "      Mineral Oil                106     120          ?     12759M  2014-12-30 17:33:30\n" +
                    "   + Consumer Items\n" +
                    "      Clothing                   466       0    879226H          -  2014-12-30 17:33:30\n" +
                    "      Consumer Technology       7041       0     77568M          -  2014-12-30 17:33:30\n" +
                    "      Domestic Appliances        713       0    276516M          -  2014-12-30 17:33:30\n" +
                    "   + Foods\n" +
                    "      Algae                       39      53          ?    124331M  2014-12-30 17:33:30\n" +
                    "      Animal Meat               1143    1184          ?       373M  2014-12-30 17:33:30\n" +
                    "      Coffee                    1158    1199          ?       354M  2014-12-30 17:33:30\n" +
                    "      Fish                       328     347          ?     13231M  2014-12-30 17:33:30\n" +
                    "      Fruit And Vegetables       221     239          ?       688M  2014-12-30 17:33:30\n" +
                    "      Grain                      121     136          ?      1140M  2014-12-30 17:33:30\n" +
                    "      Tea                       1308    1354          ?       352M  2014-12-30 17:33:30\n" +
                    "";

    private static final String[] lines = Strings.split("\\n");

    private Market createMarket(){
        InputStream is = getClass().getResourceAsStream("/test_world.xml");
        Market world;
        try {
            world = Store.loadFromFile(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new AssertionError(e);
        }
        return world;
    }

    @Test
    public void testImport() throws Exception {
        Market market = createMarket();
        OffersHandler handler = new OffersHandler(market, true);
        for (String line : lines) {
            handler.parse(line);
        }

        assertEquals(6, market.getVendors().size());

        Vendor station =  market.get("Bonde").get("Aksyonov Platform");
        assertNotNull(station);
        Item item = market.getItem("fish");
        Offer offer = station.getBuy(item);
        assertNotNull(offer);
        assertEquals(430, offer.getPrice(), 0.0);
        assertEquals(260, offer.getCount());
        offer = station.getSell(item);
        assertNull(offer);

        item = market.getItem("syntheticfabrics");
        offer = station.getBuy(item);
        assertNotNull(offer);
        assertEquals(90, offer.getPrice(), 0.0);
        assertEquals(0, offer.getCount());
        offer = station.getSell(item);
        assertEquals(118, offer.getPrice(), 0.0);
        assertEquals(7438, offer.getCount());

        item = market.getItem("polymers");
        offer = station.getBuy(item);
        assertNotNull(offer);
        assertEquals(63, offer.getPrice(), 0.0);
        assertEquals(0, offer.getCount());
        offer = station.getSell(item);
        assertNull(offer);

        //check add
        item = market.getItem("basicmedicines");
        offer = station.getBuy(item);
        assertNotNull(offer);
        assertEquals(319, offer.getPrice(), 0.0);
        assertEquals(175, offer.getCount());
        offer = station.getSell(item);
        assertEquals(200, offer.getPrice(), 0.0);
        assertEquals(30, offer.getCount());

        //Check remove
        item = market.getItem("indium");
        offer = station.getBuy(item);
        assertNull(offer);
        offer = station.getSell(item);
        assertNull(offer);

        //Check removed market items only
        item = market.getItem("Power Plant C4");
        offer = station.getBuy(item);
        assertNull(offer);
        offer = station.getSell(item);
        assertNotNull(offer);

        item = market.getItem("Sidewinder");
        offer = station.getBuy(item);
        assertNull(offer);
        offer = station.getSell(item);
        assertNotNull(offer);
    }
}
