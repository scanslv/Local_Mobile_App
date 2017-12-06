package com.iivanovs.locals.entity;

import java.util.ArrayList;

public class LocalImg {
    private int id;
    private String img_path;
    private int local_id;

    public LocalImg(){};

    public LocalImg(String img_path, int local_id) {
        this.img_path = img_path;
        this.local_id = local_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public int getLocal_id() {
        return local_id;
    }

    public void setLocal_id(int local_id) {
        this.local_id = local_id;
    }
}
