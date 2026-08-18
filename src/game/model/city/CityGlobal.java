package game.model.city;

import game.model.world.Hex;

import java.util.ArrayList;
import java.util.List;

public class CityGlobal {
    public static List<City> cities = new ArrayList<>();

    public static void setCities(List<City> cityList) {
        cities = cityList;
    }

    public static City findCityAtHex(Hex hex) {
        for (City city : cities) {
            if (city.getTiles().contains(hex)) return city;
        }
        return null;
    }
}