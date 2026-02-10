package Tests;

import entities.projectagricole;
import entities.ressourceproject;
import services.projectagricoleCRUD;
import services.ressourceprojectCRUD;
import utils.MyDatabase;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // ✅ Test Singleton (connection)
        MyDatabase db = MyDatabase.getInstance();

        Mainjavafx.main(args);


    }
}