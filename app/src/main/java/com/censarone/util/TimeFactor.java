package com.censarone.util;

import java.util.HashMap;
import java.util.Map;

public class TimeFactor {

    private static final Integer ADVENTURE_SPORTS_VENUE=10800;
    private static final Integer AMUSEMENT_PARK=10800;
    private static final Integer AQUATIC_ZOO_MARINE_PARK=5400;
    private static final Integer BAR=2700;
    private static final Integer BARBEQUE=3600;
    private static final Integer BEACH=7200;
    private static final Integer BUFFET=5400;
    private static final Integer CAFETERIA=1800;
    private static final Integer CASINO=3600;
    private static final Integer ENTERTAINMENT=3600;
    private static final Integer IMPORTANT_TOURIST_ATTRACTION=5400;
    private static final Integer NATURAL_ATTRACTION=3600;
    private static final Integer RESTAURANT=2700;
    private static final Integer TEMPLE=3600;
    private static final Integer ZOO=3600;
    private static final Integer WILDLIFE_PARK=7200;
    private static final Integer SHOPPING_CENTER=5400;

    private static Map<String,Integer> timeMap;

    public static Integer getTimeFactor(String input)
    {
        if(timeMap==null)
            inputValues();

        return timeMap.get(input);
        
    }

    private static void inputValues() {
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
        timeMap.put("entertainment",ENTERTAINMENT);
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
