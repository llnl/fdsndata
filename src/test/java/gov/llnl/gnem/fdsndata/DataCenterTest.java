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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DataCenterTest {

    private FedCatalog catalog = null;

    public DataCenterTest() {
        try {
            catalog = new FedCatalog();
        } catch (IOException ex) {
            System.getLogger(DataCenterTest.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    /**
     * Test of getServiceList method, of class DataCenter.
     */
    @Test
    public void testGetServiceList() {
        System.out.println("getServiceList");
        if (catalog != null) {
            DataCenter instance = catalog.getDataCenter("IRISDMC");
            Collection<String> result = instance.getServiceList();
            assertTrue(result != null && result.size() >= 3);
        }
    }

    /**
     * Test of getName method, of class DataCenter.
     */
    @Test
    public void testGetName() {
        System.out.println("getName");
        if (catalog != null) {
            DataCenter instance = catalog.getDataCenter("IRISDMC");
            String result = instance.getName();
            assertEquals("IRISDMC", result);
        }
    }

    /**
     * Test of getServices method, of class DataCenter.
     */
    @Test
    public void testGetServices() {
        System.out.println("getServices");
        if (catalog != null) {
            DataCenter instance = catalog.getDataCenter("IRISDMC");
            List<DataService> result = instance.getServices();
            assertTrue(result != null && result.size() >= 3);
        }
    }

    /**
     * Test of hasStationService method, of class DataCenter.
     */
    @Test
    public void testHasStationService() {
        System.out.println("hasStationService");
        if (catalog != null) {
            DataCenter instance = catalog.getDataCenter("IRISDMC");
            boolean result = instance.hasStationService();
            assertTrue(result);
        }
    }

    /**
     * Test of hasWaveformService method, of class DataCenter.
     */
    @Test
    public void testHasWaveformService() {
        System.out.println("hasWaveformService");
        if (catalog != null) {
            DataCenter instance = catalog.getDataCenter("IRISDMC");
            boolean result = instance.hasWaveformService();
            assertTrue(result);
        }
    }

    /**
     * Test of hasEventService method, of class DataCenter.
     */
    @Test
    public void testHasEventService() {
        System.out.println("hasEventService");
        if (catalog != null) {
            DataCenter instance = catalog.getDataCenter("IRISDMC");
            boolean result = instance.hasEventService();
            assertTrue(!result);
        }
    }
}
