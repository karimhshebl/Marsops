package universalHelpers;

import interfaces.ILocation;

public class Validation {
    public static String validateString(String string , String name){
        if(string == null) throw new IllegalArgumentException(name + " cannot be null");
        string = string.trim();
        if(string.isEmpty()) throw new IllegalArgumentException(name + " cannot be empty");
        else return string;
    }

    public static double validatePositive(double number , String name){
        if(number > 0) return number;
        else throw new IllegalArgumentException(name + " must be positive");
    }

    public static int validatePositive(int number , String name){
        if(number > 0) return number;
        else throw new IllegalArgumentException(name + " must be positive");
    }

    public static double validateNonNegative(double number , String name){
        if(number >= 0) return number;
        else throw new IllegalArgumentException(name + " must be non-negative");
    }

    public static int validateNonNegative(int number , String name){
        if(number >= 0) return number;
        else throw new IllegalArgumentException(name + " must be non-negative");
    }

    public static double validatePercentage(double number , String name){
        if(number <= 100 && number >= 0) return number;
        else throw new IllegalArgumentException(name + " must be between 0 and 100");
    }

    public static int clampPercentage(int number){
        return Math.max(0, Math.min(100, number));
    }

    public static ILocation validateLocation(ILocation location , String name){
        if (location == null) throw new IllegalArgumentException(name + " cannot be null");
        return location;
    }

}
