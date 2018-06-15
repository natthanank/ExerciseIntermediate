package com.example.admin.exercise_intermediate;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.LicenseInfo;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.security.UserCredential;

public class ESRILicense {

    public static void setLicense() {
        // connect to ArcGIS Online or an ArcGIS portal as a named user
        // The code below shows the use of token based security but
        // for ArcGIS Online you may consider using Oauth authentication.
        UserCredential credential = new UserCredential("natthanank", "stupid1408");

        // replace the URL with either the ArcGIS Online URL or your portal URL
        final Portal portal = new Portal("https://your-org.arcgis.com/");
        portal.setCredential(credential);


        // load portal and listen to done loading event
        portal.loadAsync();
        portal.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                // get license info from the portal
                LicenseInfo licenseInfo = portal.getPortalInfo().getLicenseInfo();
                // Apply the license at Standard level
                ArcGISRuntimeEnvironment.setLicense(licenseInfo);
            }
        });
    }
}
