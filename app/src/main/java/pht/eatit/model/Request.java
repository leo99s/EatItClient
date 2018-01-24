package pht.eatit.model;

import java.util.List;

public class Request {
    private String Phone, Name, Address, Total;
    private List<Order> Orders;

    public Request() {
    }

    public Request(String phone, String name, String address, String total, List<Order> orders) {
        Phone = phone;
        Name = name;
        Address = address;
        Total = total;
        Orders = orders;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public List<Order> getOrders() {
        return Orders;
    }

    public void setOrders(List<Order> orders) {
        Orders = orders;
    }
}