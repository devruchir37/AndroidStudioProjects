package com.example.multipleaccountsproject;

public class Item {
    private String title;
    private String details;
    private boolean isExpanded;

    // Constructor, getters, and setters

    public Item(String title, String details) {
        this.title = title;
        this.details = details;
        this.isExpanded = false; // Default to not expanded
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}