package pht.eatit.model;

public class Rating {

    private String phone, food_id, rating, comment;

    public Rating() {
    }

    public Rating(String phone, String food_id, String rating, String comment) {
        this.phone = phone;
        this.food_id = food_id;
        this.rating = rating;
        this.comment = comment;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFood_id() {
        return food_id;
    }

    public void setFood_id(String food_id) {
        this.food_id = food_id;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}