package com.smartagriculture;

/**
 * This class is for each Category of product
 * - name - String
 * - count - int
 * - pic - String
 */

public class Category {

    private String name;
    private int count;
    private String pic;

    public Category() {
        count = 0;
    }

    public Category(String name) {
        count = 0;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void increaseCount() {
        count++;
    }

    public void decreaseCount() {
        if (count > 0)
            count--;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String picUri) {
        this.pic = picUri;
    }
}
