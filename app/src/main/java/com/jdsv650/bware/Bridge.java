package com.jdsv650.bware;

import java.io.Serializable;

/**
 * Created by james on 11/7/17.
 */

public class Bridge implements Serializable {

    Double weightStraight = -99.0;
    Double weightStraight_TriAxle = -99.0;
    Double weightCombo = -99.0;
    Double weightDouble = -99.0;
    Double height = -99.0;
    String locationDescription = "";
    String city = "";
    String state = "";
    String zip = "";
    String country = "";
    Boolean isRPosted = false;
    Double latitude = -99.0;
    Double longitude = -99.0;
    String featureCarried = "";
    String featureCrossed = "";
    String county = "";
    String otherPosting = "";
    Integer numVotes = -99;
    Boolean isLocked = false;

}
