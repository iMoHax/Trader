package ru.trader.analysis;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;
import ru.trader.core.Place;
import ru.trader.store.simple.Store;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class PowerPlayAnalyzatorTest extends Assert {
    private final static Logger LOG = LoggerFactory.getLogger(RouteSearcherTest.class);

    private Market world;
    private Place lhs3262;
    private Place aulin;
    private Place morgor;
    private Place lhs417;

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("/world.xml");
        world = Store.loadFromFile(is);
        lhs3262 = world.get("LHS 3262");
        aulin = world.get("Aulin");
        morgor = world.get("Morgor");
        lhs417 = world.get("LHS 417");

    }

    @Test
    public void intersectTest() throws Exception {
        Collection<Place> starSystems = world.get();
        Collection<Place> controllingLhs3262 = starSystems.stream().filter(p -> p.getDistance(lhs3262) <= 15).collect(Collectors.toList());
        Collection<Place> controllingAulin = starSystems.stream().filter(p -> p.getDistance(aulin) <= 15).collect(Collectors.toList());
        Collection<Place> controllingMorgor = starSystems.stream().filter(p -> p.getDistance(morgor) <= 15).collect(Collectors.toList());
        Collection<Place> expectedIntersect = controllingLhs3262.stream().filter(controllingAulin::contains).collect(Collectors.toList());
        Collection<Place> expected3Intersect = controllingLhs3262.stream().filter(controllingAulin::contains).filter(controllingMorgor::contains).collect(Collectors.toList());

        PowerPlayAnalyzator analyzator = new PowerPlayAnalyzator(world);
        Collection<Place> centers = new ArrayList<>();
        centers.add(lhs3262);

        Collection<PowerPlayAnalyzator.IntersectData> intersects = analyzator.getIntersects(starSystems, centers, 15);
        LOG.info("Test intersects by LHS 3262, found {}", intersects.size());
        for (PowerPlayAnalyzator.IntersectData intersect : intersects) {
            assertTrue(lhs3262.getDistance(intersect.getStarSystem()) <= 15);
        }
        assertTrue(intersects.containsAll(controllingLhs3262));
        assertEquals(controllingLhs3262.size(), intersects.size());

        centers.add(aulin);
        intersects = analyzator.getIntersects(starSystems, centers, 15);
        LOG.info("Test intersects by LHS 3262 and Aulin, found {}", intersects.size());
        for (PowerPlayAnalyzator.IntersectData intersect : intersects) {
            assertTrue(lhs3262.getDistance(intersect.getStarSystem()) <= 15);
            assertTrue(aulin.getDistance(intersect.getStarSystem()) <= 15);
        }
        assertTrue(intersects.containsAll(expectedIntersect));
        assertEquals(expectedIntersect.size(), intersects.size());

        centers.add(morgor);
        intersects = PowerPlayAnalyzator.getIntersects(starSystems, centers, 15);
        LOG.info("Test intersects by LHS 3262 and Aulin and Morgor, found {}", intersects.size());
        for (PowerPlayAnalyzator.IntersectData intersect : intersects) {
            assertTrue(lhs3262.getDistance(intersect.getStarSystem()) <= 15);
            assertTrue(aulin.getDistance(intersect.getStarSystem()) <= 15);
            assertTrue(morgor.getDistance(intersect.getStarSystem()) <= 15);
        }
        assertTrue(intersects.containsAll(expected3Intersect));
        assertEquals(expected3Intersect.size(), intersects.size());

        intersects = PowerPlayAnalyzator.getIntersects(starSystems, centers, 12);
        LOG.info("Test intersects by LHS 3262 and Aulin and Morgor, 12 radius,  found {}", intersects.size());
        for (PowerPlayAnalyzator.IntersectData intersect : intersects) {
            assertTrue(lhs3262.getDistance(intersect.getStarSystem()) <= 12);
            assertTrue(aulin.getDistance(intersect.getStarSystem()) <= 12);
            assertTrue(morgor.getDistance(intersect.getStarSystem()) <= 12);
        }
        assertEquals(0, intersects.size());

    }

    @Test
    public void intersectTest2() throws Exception {
        Collection<Place> starSystems = world.get();
        Collection<Place> controllingLhs3262 = starSystems.stream().filter(p -> p.getDistance(lhs3262) <= 12).collect(Collectors.toList());
        Collection<Place> controllingAulin = starSystems.stream().filter(p -> p.getDistance(aulin) <= 12).collect(Collectors.toList());
        Collection<Place> controllingMorgor = starSystems.stream().filter(p -> p.getDistance(morgor) <= 12).collect(Collectors.toList());
        Collection<Place> controllingLhs417 = starSystems.stream().filter(p -> p.getDistance(lhs417) <= 12).collect(Collectors.toList());
        Collection<Place> expectedIntersect = controllingLhs417.stream().filter(p -> controllingLhs3262.contains(p) || controllingAulin.contains(p)).collect(Collectors.toList());
        Collection<Place> expected3Intersect = controllingLhs417.stream().filter(p -> controllingLhs3262.contains(p) || controllingAulin.contains(p) || controllingMorgor.contains(p)).collect(Collectors.toList());
        Collection<Place> expected2Intersect = controllingLhs417.stream().filter(p -> controllingAulin.contains(p) || controllingMorgor.contains(p)).collect(Collectors.toList());

        PowerPlayAnalyzator analyzator = new PowerPlayAnalyzator(world);
        Collection<Place> centers = new ArrayList<>();
        centers.add(lhs3262);
        centers.add(aulin);
        Collection<PowerPlayAnalyzator.IntersectData> intersects = PowerPlayAnalyzator.getIntersects(lhs417, starSystems, centers, 12);
        LOG.info("Test LHS 417 intersect LHS 3262 or Aulin, found {}", intersects.size());
        for (PowerPlayAnalyzator.IntersectData intersect : intersects) {
            assertTrue(lhs3262.getDistance(intersect.getStarSystem()) <= 12 || aulin.getDistance(intersect.getStarSystem()) <= 12);
            assertTrue(lhs417.getDistance(intersect.getStarSystem()) <= 12);
        }
        assertTrue(intersects.containsAll(expectedIntersect));
        assertEquals(expectedIntersect.size(), intersects.size());

        centers.add(morgor);
        intersects = PowerPlayAnalyzator.getIntersects(lhs417, starSystems, centers, 12);
        LOG.info("Test LHS 417 intersect LHS 3262 or Aulin or Morgor, found {}", intersects.size());
        for (PowerPlayAnalyzator.IntersectData intersect : intersects) {
            assertTrue(lhs3262.getDistance(intersect.getStarSystem()) <= 12 || aulin.getDistance(intersect.getStarSystem()) <= 12 || morgor.getDistance(intersect.getStarSystem()) <= 12);
            assertTrue(lhs417.getDistance(intersect.getStarSystem()) <= 12);
        }
        assertTrue(intersects.containsAll(expected3Intersect));
        assertEquals(expected3Intersect.size(), intersects.size());

        centers.clear();
        centers.add(morgor);
        intersects = PowerPlayAnalyzator.getIntersects(lhs417, starSystems, centers, 12);
        LOG.info("Test LHS 417 intersect Morgor, found {}", intersects.size());
        for (PowerPlayAnalyzator.IntersectData intersect : intersects) {
            assertTrue(morgor.getDistance(intersect.getStarSystem()) <= 12);
            assertTrue(lhs417.getDistance(intersect.getStarSystem()) <= 12);
        }
        assertEquals(0, intersects.size());

        centers.add(aulin);
        intersects = PowerPlayAnalyzator.getIntersects(lhs417, starSystems, centers, 12);
        LOG.info("Test LHS 417 intersect Aulin or Morgor, found {}", intersects.size());
        for (PowerPlayAnalyzator.IntersectData intersect : intersects) {
            assertTrue(aulin.getDistance(intersect.getStarSystem()) <= 12 || morgor.getDistance(intersect.getStarSystem()) <= 12);
            assertTrue(lhs417.getDistance(intersect.getStarSystem()) <= 12);
        }
        assertTrue(intersects.containsAll(expected2Intersect));
        assertEquals(expected2Intersect.size(), intersects.size());


    }

    @Test
    public void nearTest() throws Exception {
        Collection<Place> starSystems = world.get();
        Collection<Place> controllingLhs3262 = starSystems.stream().filter(p -> p.getDistance(lhs3262) <= 15).collect(Collectors.toList());
        Collection<Place> controllingAulin = starSystems.stream().filter(p -> p.getDistance(aulin) <= 15).collect(Collectors.toList());
        Collection<Place> controllingMorgor = starSystems.stream().filter(p -> p.getDistance(morgor) <= 15).collect(Collectors.toList());

        PowerPlayAnalyzator analyzator = new PowerPlayAnalyzator(world);
        Collection<Place> centers = new ArrayList<>();
        centers.add(lhs3262);
        centers.add(aulin);
        Collection<PowerPlayAnalyzator.IntersectData> near = PowerPlayAnalyzator.getNear(starSystems, centers, 15, 30);
        LOG.info("Test near by LHS 3262 and Aulin, found {}", near.size());
        assertTrue(near.size() > 0);
        for (PowerPlayAnalyzator.IntersectData place : near) {
            Collection<PowerPlayAnalyzator.IntersectData> intersect = PowerPlayAnalyzator.getIntersects(place.getStarSystem(), starSystems, centers, 15);
            assertEquals(0, intersect.size());
        }

    }


}
