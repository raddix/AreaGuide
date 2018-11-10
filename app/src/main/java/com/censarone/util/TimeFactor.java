package com.censarone.util;

import java.util.HashMap;
import java.util.Map;

public class TimeFactor {

    public static final Double ADVENTURE_SPORTS_VENUE=3.0;
    public static final Double AMUSEMENT_PARK=3.0;
    public static final Double AQUATIC_ZOO_MARINE_PARK=1.5;
    public static final Double BAR=0.75;
    public static final Double BARBEQUE=1.0;
    public static final Double BEACH=2.0;
    public static final Double BUFFET=1.5;
    public static final Double CAFETERIA=0.5;
    public static final Double CASINO=1.0;
    public static final Double ENTERTAINMENT=1.0;
    public static final Double IMPORTANT_TOURIST_ATTRACTION=1.5;
    public static final Double NATURAL_ATTRACTION=1.0;
    public static final Double RESTAURANT=0.75;
    public static final Double TEMPLE=1.0;
    public static final Double ZOO=1.0;
    public static final Double WILDLIFE_PARK=2.0;
    public static final Double SHOPPING_CENTER=1.5;

    public static Map<String,Double> timeMap;

    public static Double getTimeFactor(String input)
    {
        if(timeMap==null)
            inputValues();

        return timeMap.get(input);
        
    }

    public static void inputValues() {
        timeMap = new HashMap<>();
        timeMap.put("adventure sports venue",ADVENTURE_SPORTS_VENUE);
        timeMap.put("amusement park",AMUSEMENT_PARK);
        timeMap.put("aquatic zoo marine park",AQUATIC_ZOO_MARINE_PARK);
        timeMap.put("bar",BAR);
        timeMap.put("barbecue",BARBEQUE);
        timeMap.put("beach",BEACH);
        timeMap.put("buffet",BUFFET);
        timeMap.put("cafeterias",CAFETERIA);
        timeMap.put("casino",CASINO);
        timeMap.put("entertainment",AMUSEMENT_PARK);
        timeMap.put("important tourist attraction",IMPORTANT_TOURIST_ATTRACTION);
        timeMap.put("natural attraction",NATURAL_ATTRACTION);
        timeMap.put("restaurant",RESTAURANT);
        timeMap.put("temple",TEMPLE);
        timeMap.put("zoo",ZOO);
        timeMap.put("wildlife park",WILDLIFE_PARK);
        timeMap.put("shopping center",SHOPPING_CENTER);
    }

    public static String convertTime(Integer input)
    {
        int hour = 0;
        int minute = 0;
        int second = 0;

        if(input>60)
        {
            minute = input/60;
            second = input%60;
        }

        if(minute>60)
        {
            hour = minute/60;
            minute = minute%60;
        }

        return hour+" H :"+minute+" m :"+second+" s";

    }

    private TimeFactor(){}


}
