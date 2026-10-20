/*-
 * #%L
 * FDSN Data Services (FDSNData)
 *  LLNL-CODE-2025872
 *  This work was performed under the auspices of the U.S. Department of Energy
 *  by Lawrence Livermore National Laboratory under Contract DE-AC52-07NA27344.
 * %%
 * Copyright (C) 2026 Lawrence Livermore National Laboratory
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package gov.llnl.gnem.fdsndata;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FedCatalogTest {

    private static FedCatalog fedCat;

    @BeforeAll
    public static void setup() throws IOException {

        // NOTE Somewhat bogus test for automation since the makeFromList does not add RASPISHAKE IRISPH5
        // fedCat = new FedCatalog();

        FedCatalog fedCatTemp = FedCatalog.makeFromList();
        List<DataCenter> dcs = fedCatTemp.getDataCenters();

        String location = "Seattle, WA, USA";
        String website = "http://raspberryshake.net/";
        String lastUpdate = "";
        String description = "Raspberry Shake Project";
        String name = "RASPISHAKE";
        List<DataService> services = new ArrayList<>();
        services.add(new DataService("dataselectService", "http://fdsnws.raspberryshakedata.com/fdsnws/dataselect/1/"));
        services.add(new DataService("stationService", "https://fdsnws.raspberryshakedata.com/fdsnws/station/1/"));
        services.add(new DataService("eventService", "http://fdsnws.raspberryshakedata.com/fdsnws/event/1/"));

        DataCenter dc = new DataCenter(location, website, lastUpdate, description, name, services);
        dcs.add(dc);

        fedCat = new FedCatalog(dcs);
    }

    @Test
    public void testRemoveRASPISHAKE() {

        Collection<String> sources = fedCat.getStationServiceSources(true);
        assertFalse(sources.contains("RASPISHAKE"), "Test RASPISHAKE removed.");
        assertFalse(sources.isEmpty(), "Should have some results.");
        assertTrue(sources.contains("IRISDMC"), "Should have IRISDMC");
    }

    @Test
    public void testRemoveIRISPH5() {

        Collection<String> sources = fedCat.getStationServiceSources(true);
        assertFalse(sources.contains("IRISPH5"), "Test IRISPH5 removed.");
        assertFalse(sources.isEmpty(), "Should have some results.");
        assertTrue(sources.contains("IRISDMC"), "Should have IRISDMC");
    }

    /**
     * Test of makeFromList method, of class FedCatalog.
     */
    @Test
    public void testMakeFromList() {
        System.out.println("makeFromList");
        FedCatalog result = FedCatalog.makeFromList();
        assertTrue(result != null && result.getDataCenters().size() >= 4);
    }

    /**
     * Test of supports method, of class FedCatalog.
     */
    @Test
    public void testSupports() {
        System.out.println("supports");
        String source = "IRISDMC";
        FedCatalog instance;
        try {
            instance = new FedCatalog();
            boolean result = instance.supports(source);
            assertTrue(result);
        } catch (IOException ex) {
            System.getLogger(FedCatalogTest.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    /**
     * Test of getDataCenter method, of class FedCatalog.
     */
    @Test
    public void testGetDataCenter() {
        System.out.println("getDataCenter");
        String source = "IRISDMC";
        FedCatalog instance;
        try {
            instance = new FedCatalog();
            DataCenter result = instance.getDataCenter(source);
            assertTrue(result != null && result.hasWaveformService());
        } catch (IOException ex) {
            System.getLogger(FedCatalogTest.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    /**
     * Test of getDataCenters method, of class FedCatalog.
     */
    @Test
    public void testGetDataCenters() {
        System.out.println("getDataCenters");
        FedCatalog instance;
        try {
            instance = new FedCatalog();
            List<DataCenter> result = instance.getDataCenters();
            assertTrue(result != null && !result.isEmpty());
        } catch (IOException ex) {
            System.getLogger(FedCatalogTest.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
