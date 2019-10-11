package com.android.launcher3.entry;

import java.util.ArrayList;

public class HotseatCategory {
    String categoryName;

    public ArrayList<String> packageNames = new ArrayList<>();

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void addPackage(String packageName) {
        packageNames.add(packageName);
    }
}
