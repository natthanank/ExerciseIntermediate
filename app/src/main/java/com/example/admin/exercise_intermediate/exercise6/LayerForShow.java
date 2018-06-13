package com.example.admin.exercise_intermediate.exercise6;

public class LayerForShow {

    private String name;
    private boolean isShow;

    public LayerForShow() {

    }

    public LayerForShow(String name, boolean isShow) {
        this.name = name;
        this.isShow = isShow;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
