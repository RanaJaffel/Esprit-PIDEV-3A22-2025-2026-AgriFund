package tests;

import entities.capteur;
import entities.releve_terrain;
import services.ServiceCapteur;
import services.ServiceReleveTerrain;
import utils.MyDabase;

import java.sql.SQLException;

public class main {

    public static void main(String[] args) throws SQLException {
        MyDabase db = MyDabase.getInstance();

        capteur c1 = new capteur(1,"humidité","tunis","actif",3);
        ServiceCapteur sc = new ServiceCapteur();
        releve_terrain r1 = new releve_terrain(1,"cm",1.2,"klm",null,1);
        ServiceReleveTerrain sr = new ServiceReleveTerrain();
        sr.ajouter(r1);
        //sc.ajouter(c1);
        //System.out.println(sc.afficher());





    }
}
