package pht.eatit.model;

public class Order {

    private int id;
    private String food_id, image, name, price, quantity, discount;

    public Order() {
    }

    public Order(int id, String food_id, String image, String name, String price, String quantity, String discount) {
        this.id = id;
        this.food_id = food_id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.discount = discount;
    }

    public Order(String food_id, String image, String name, String price, String quantity, String discount) {
        this.food_id = food_id;
        this.image = image;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.discount = discount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFood_id() {
        return food_id;
    }

    public void setFood_id(String food_id) {
        this.food_id = food_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}