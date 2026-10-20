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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import llnl.gnem.core.util.TimeT;


public class Util {

    public static Date convertToDate(double anEpochTime) throws ParseException {

        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        dfm.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        TimeT tmp = new TimeT(anEpochTime);

        return dfm.parse(tmp.toString("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public static String getDateString(Double anEpochTime) {
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        dfm.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        TimeT tmp = new TimeT(anEpochTime);
        return tmp.toString("yyyy-MM-dd'T'HH:mm:ss.SSS");
    }
}
