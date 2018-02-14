package pht.eatit.model;

public class Food {
    private String category_id, name, image, description, price, discount;

    public Food() {
    }

    public Food(String category_id, String name, String image, String description, String price, String discount) {
        this.category_id = category_id;
        this.name = name;
        this.image = image;
        this.description = description;
        this.price = price;
        this.discount = discount;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}