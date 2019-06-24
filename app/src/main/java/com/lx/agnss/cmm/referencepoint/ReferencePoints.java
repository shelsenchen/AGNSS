package com.lx.agnss.cmm.referencepoint;

import com.google.ar.sceneform.Node;

import java.util.ArrayList;
import java.util.List;

import uk.co.appoly.arcorelocation.LocationMarker;

public class ReferencePoints {

    private static List<ReferencePoint> listReferencePoint = new ArrayList<ReferencePoint>();

    public ReferencePoints() {
        for(double i = 37.532946; i < 37.532950; i+=0.000001) {
            for(double j = 126.959868; j <126.959878; j+=0.000001) {

                LocationMarker layoutLocationMarker = new LocationMarker((double)i, (double)j, getExampleView());

                ReferencePoint referencePoint = new ReferencePoint();
                referencePoint.locationMarker = new LocationMarker(i, j,getExampleView());

                referencePoint.locationName = "Reference Point Number: #" + i + " - " + j ;
                referencePoint.locationDescription = "Reference Point Description: " + i + " - " + j;


            }
        }
    }

    /**
     * Get a list a Reference Point
     * @return
     */
    public List<ReferencePoint> getList() {

        return this.listReferencePoint;
    }

    /**
     * Put in a new Reference Point to the list
     * @param referencePointItem
     */
    public void putReferencePointItem(ReferencePoint referencePointItem) {
        this.listReferencePoint.add(referencePointItem);
    }

    /**
     * Remove item by index
     * @param index
     */
    public void removeReferencePointItem(int index) {
        listReferencePoint.remove(index);
    }

    /**
     * Remove item by Location Maker
     * @param referencePointItem
     */
    public void removeReferencePointItem(ReferencePoint referencePointItem) {
        listReferencePoint.remove(referencePointItem);
    }

    /**
     * NC soft pannel
     * @return
     */
    private Node getExampleView() {
        Node base = new Node();
//        base.setRenderable(R.layout.popup_layout);
//
//        Context c = this.g;
//        // Add  listeners etc here
//        View eView = popupLayoutRenderable.getView();
//        eView.setOnTouchListener((v, event) -> {
//            Toast.makeText(
//                    c, "NC 소프트\n(37.399464, 127.108851)", Toast.LENGTH_LONG)
//                    .show();
//            return false;
//        });

        return base;
    }
}