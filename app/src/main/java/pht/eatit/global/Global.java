package pht.eatit.global;

import pht.eatit.model.User;

public class Global {
    public static User activeUser;

    public static String convertCodeToStatus(String status) {
        if(status.equals("0")){
            return "Placed";
        }
        else if(status.equals("1")){
            return "On my way";
        }
        else {
            return "Shipped";
        }
    }
} 