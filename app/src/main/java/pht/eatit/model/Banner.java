package pht.eatit.model;

public class Banner {

    private String food_id, name, image;

    public Banner() {
    }

    public Banner(String food_id, String name, String image) {
        this.food_id = food_id;
        this.name = name;
        this.image = image;
    }

    public String getFood_id() {
        return food_id;
    }

    public void setFood_id(String food_id) {
        this.food_id = food_id;
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
}