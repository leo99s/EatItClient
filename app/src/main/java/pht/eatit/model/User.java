package pht.eatit.model;

public class User {

    private String Phone, Name, Password, Admin, SecureCode;

    public User() {
    }

    public User(String name, String password, String secureCode) {
        Name = name;
        Password = password;
        Admin = "false";
        SecureCode = secureCode;
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

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getAdmin() {
        return Admin;
    }

    public void setAdmin(String admin) {
        Admin = admin;
    }

    public String getSecureCode() {
        return SecureCode;
    }

    public void setSecureCode(String secureCode) {
        SecureCode = secureCode;
    }
}