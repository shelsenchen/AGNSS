package com.lx.agnss.cmm.referencepoint;

import com.google.ar.sceneform.Node;

import uk.co.appoly.arcorelocation.LocationMarker;

public class ReferencePoint {
    public String locationName;
    public String locationDescription;
    public LocationMarker locationMarker;

    private LocationMarker getLocationMaker() {
        Node node = new Node();
        return new LocationMarker(0.0, 0.0, node);
    };

    // Constructors
    public ReferencePoint() {
        this.locationName = "Reference Location Name";
        this.locationDescription = "Reference Description";
        this.locationMarker = getLocationMaker() ;
    }

    public ReferencePoint(String locationName, String locationDescription, LocationMarker locationMarker) {
        this.locationName = locationName;
        this.locationDescription = locationDescription;
        this.locationMarker = locationMarker;
    }

}
