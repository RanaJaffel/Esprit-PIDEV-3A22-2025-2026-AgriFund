package com.agrifund.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NavigationManager {

    private static NavigationManager instance;
    private final StringProperty currentPage = new SimpleStringProperty("dashboard");
    private final StringProperty currentRole = new SimpleStringProperty("agriculteur");

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public String getCurrentPage() {
        return currentPage.get();
    }

    public void setCurrentPage(String page) {
        this.currentPage.set(page);
    }

    public StringProperty currentPageProperty() {
        return currentPage;
    }

    public String getCurrentRole() {
        return currentRole.get();
    }

    public void setCurrentRole(String role) {
        this.currentRole.set(role);
    }

    public void reset() {
        this.currentPage.set("dashboard");
    }
}
