package com.company;

import java.io.*;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import static java.lang.Math.*;

public class Main {

    public static void main(String[] args) {

        String location_string = "";
        String radius_string = "";
        String header_string = "";
        String input_string = "";

        Scanner scanner = new Scanner(System.in);

        location_string = scanner.nextLine();

        radius_string = scanner.nextLine();

        header_string = scanner.nextLine();

        while(scanner.hasNextLine()){
            String input = scanner.nextLine();
            if (input.isEmpty()) {
                break;
            }
            else {
                input_string = input_string +"\n"+ input;
            }
        }

        class Phone {
            String phone = "";

            public Phone(String phone) {
                this.phone = phone;
            }
        }

        class Location {
            double lat = 0;
            double lng = 0;

            public Location(double lat, double lng) {
                this.lat = lat;
                this.lng = lng;
            };
        }

        class DateGiven implements Comparable<DateGiven> {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            Date date;

            public DateGiven(String dateString) throws ParseException {
                try {
                    this.date = simpleDateFormat.parse(dateString);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public int compareTo(DateGiven o) {
                return date.compareTo(o.date);
            }
        }

        class PositionFix implements Comparable<PositionFix> {
            Location location;
            DateGiven dateGiven;
            double distanceFromTruck = 0;
            String phone = "";

            public PositionFix(Location location1, DateGiven dateGiven1, String phone1) {
                this.dateGiven = dateGiven1;
                this.location = location1;
                this.phone = phone1;
            }

            @Override
            public int compareTo(PositionFix positionFix) {
                return this.dateGiven.date.compareTo(positionFix.dateGiven.date);
            }
        }

        ArrayList<PositionFix> qualifiedSubscribersPositionList = new ArrayList<>();

        ArrayList<PositionFix> dataList = new ArrayList<>();

        Map<String, ArrayList> dataMap = new LinkedHashMap<>();

        ArrayList<String> coordinates = createStrings(location_string, ",");

        Location foodCartLocation = new Location(Double.parseDouble(coordinates.get(0)), Double.parseDouble(coordinates.get(1)));

        double radius = Double.parseDouble(radius_string);

        ArrayList<String> dataStrings = createStrings(input_string, "\n");

        for (String s : dataStrings) {
            ArrayList<String> data = createStrings(s, ",");

            try {
                DateGiven date = new DateGiven(data.get(0));
                Location location = new Location(Double.parseDouble(data.get(1)), Double.parseDouble(data.get(2)));
                String phone = new String(data.get(3));

                PositionFix positionFix = new PositionFix(location, date, phone);

                if (!dataMap.containsKey(phone)) {
                    ArrayList<PositionFix> list = new ArrayList<>();
                    list.add(positionFix);
                    dataMap.put(phone, list);
                }
                else {
                    dataMap.get(phone).add(positionFix);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for (String phone : dataMap.keySet()) {

            ArrayList<PositionFix> tempList = dataMap.get(phone);

            if (tempList.size() > 1) {
                Collections.sort(tempList);
                PositionFix recentDate = tempList.get(tempList.size()-1);
                double distanceFromTruck = isWithinRange(recentDate.location.lat, recentDate.location.lng, foodCartLocation.lat, foodCartLocation.lng, radius);

                if (distanceFromTruck != -111) {
                    recentDate.distanceFromTruck = distanceFromTruck;
                    qualifiedSubscribersPositionList.add(recentDate);
                }
            } else {
                PositionFix recentDate = tempList.get(0);
                double distanceFromTruck = isWithinRange(recentDate.location.lat, recentDate.location.lng, foodCartLocation.lat, foodCartLocation.lng, radius);

                if (distanceFromTruck != -111) {
                    recentDate.distanceFromTruck = distanceFromTruck;
                    qualifiedSubscribersPositionList.add(recentDate);
                }
            }
        }

        class CustomComparator implements Comparator<PositionFix> {

            @Override
            public int compare(PositionFix positionFix, PositionFix t1) {
                return positionFix.phone.compareTo(t1.phone);
            }
        }

        Collections.sort(qualifiedSubscribersPositionList, new CustomComparator());

        Iterator<PositionFix> it = qualifiedSubscribersPositionList.iterator();
        if (it.hasNext()) {
            System.out.print(it.next().phone);
        }
        while (it.hasNext()) {
            System.out.print("," + it.next().phone);
        }
    }

    private static ArrayList<String> createStrings(String longString, String delimiter) {
        ArrayList<String> stringList = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(longString, delimiter);

        while (tokenizer.hasMoreElements()) {
            stringList.add((String) tokenizer.nextElement());
        }
        return stringList;
    }

    private static double isWithinRange(double lat, double lng, double truck_lat, double truck_lng, double range_expected) {
        double radius_earth = 6378.137;
        double distance = 0;

        distance = 2 * radius_earth * asin(sqrt(pow(sin(toRadians((lat - truck_lat))/2), 2) + cos(toRadians(lat)) * cos(toRadians(truck_lat)) * pow(sin(toRadians(lng - truck_lng)/2), 2)));

        if (distance <= range_expected) {
            return distance;
        }
        else {
            return -111;
        }
    }
}