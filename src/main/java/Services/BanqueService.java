package Services;

import entities.Banque;
import Utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les Banques
 */
public class BanqueService {

    private Connection connection;
    private UtilisateurService utilisateurService;

    public BanqueService() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
        this.utilisateurService = new UtilisateurService();
    }

    /**
     * INSCRIPTION d'une banque
     */
    public int inscrire(Banque banque) throws SQLException {
        PreparedStatement pstBanque = null;
        int banqueId = -1;

        try {
            connection.setAutoCommit(false);

            // 1. Insérer dans Utilisateur
            int utilisateurId = utilisateurService.ajouter(banque);

            if (utilisateurId > 0) {
                // 2. Insérer dans Banque
                String sqlBanque = "INSERT INTO Banque (utilisateur_id, codebanque, addresseSiege, " +
                        "representantLegal, adresseAgence, logo, siteweb, statusCompte, compteVerfiee) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                pstBanque = connection.prepareStatement(sqlBanque, Statement.RETURN_GENERATED_KEYS);
                pstBanque.setInt(1, utilisateurId);
                pstBanque.setString(2, banque.getCodeBanque());
                pstBanque.setString(3, banque.getAddresseSiege());
                pstBanque.setString(4, banque.getRepresentantLegal());
                pstBanque.setString(5, banque.getAdresseAgence());
                pstBanque.setString(6, banque.getLogo());
                pstBanque.setString(7, banque.getSiteWeb());
                pstBanque.setString(8, "en_attente");
                pstBanque.setBoolean(9, false);

                int rowsAffected = pstBanque.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet rs = pstBanque.getGeneratedKeys();
                    if (rs.next()) {
                        banqueId = rs.getInt(1);
                        banque.setBanqueId(banqueId);
                        banque.setUtilisateurId(utilisateurId);
                    }
                    rs.close();
                }

                connection.commit();
                System.out.println("✓ Banque inscrite avec succès! ID: " + banqueId);
            }

        } catch (SQLException e) {
            connection.rollback();
            System.err.println("✗ Erreur lors de l'inscription: " + e.getMessage());
            throw e;
        } finally {
            connection.setAutoCommit(true);
            if (pstBanque != null) pstBanque.close();
        }

        return banqueId;
    }

    /**
     * VÉRIFIER le compte d'une banque
     */
    public void verifierCompte(int banqueId, boolean verifier) throws SQLException {
        String sql = "UPDATE Banque SET compteVerfiee=?, statusCompte=? WHERE id=?";

        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setBoolean(1, verifier);
            pst.setString(2, verifier ? "actif" : "refuse");
            pst.setInt(3, banqueId);

            pst.executeUpdate();
            System.out.println("✓ Compte banque " + (verifier ? "vérifié" : "refusé") + "!");

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * MODIFIER les informations d'une banque
     */
    public void modifier(Banque banque) throws SQLException {
        String sql = "UPDATE Banque SET representantLegal=?, addresseSiege=?, " +
                "adresseAgence=?, siteweb=?, logo=? WHERE utilisateur_id=?";

        PreparedStatement pst = null;

        try {
            // S'assurer que l'ID utilisateur est défini
            int utilisateurId = banque.getUtilisateurId();
            if (utilisateurId == 0) {
                utilisateurId = banque.getId(); // Fallback sur getId() si utilisateurId pas défini
            }

            if (utilisateurId == 0) {
                throw new SQLException("ID utilisateur non défini pour la banque");
            }

            // Mettre à jour la table Utilisateur d'abord
            utilisateurService.modifier(banque);

            // Mettre à jour la table Banque
            pst = connection.prepareStatement(sql);
            pst.setString(1, banque.getRepresentantLegal());
            pst.setString(2, banque.getAddresseSiege());
            pst.setString(3, banque.getAdresseAgence());
            pst.setString(4, banque.getSiteWeb());
            pst.setString(5, banque.getLogo());
            pst.setInt(6, utilisateurId);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✓ Banque modifiée avec succès!");
            } else {
                System.out.println("⚠ Aucune banque trouvée avec utilisateur_id: " + utilisateurId);
            }

        } finally {
            if (pst != null) pst.close();
        }
    }

    /**
     * AFFICHER toutes les banques
     */
    public List<Banque> afficherTous() throws SQLException {
        List<Banque> banques = new ArrayList<>();
        String sql = "SELECT b.*, u.nom, u.prenom, u.email, u.tel, u.date_inscrit, u.photo " +
                "FROM Banque b " +
                "INNER JOIN Utilisateur u ON b.utilisateur_id = u.id";

        Statement st = null;
        ResultSet rs = null;

        try {
            st = connection.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                Banque banque = new Banque();

                banque.setBanqueId(rs.getInt("b.id"));
                banque.setUtilisateurId(rs.getInt("utilisateur_id"));
                banque.setCodeBanque(rs.getString("codebanque"));
                banque.setAddresseSiege(rs.getString("addresseSiege"));
                banque.setRepresentantLegal(rs.getString("representantLegal"));
                banque.setAdresseAgence(rs.getString("adresseAgence"));
                banque.setLogo(rs.getString("logo"));
                banque.setSiteWeb(rs.getString("siteweb"));
                banque.setStatusCompte(rs.getString("statusCompte"));
                banque.setCompteVerifie(rs.getBoolean("compteVerfiee"));

                banque.setId(rs.getInt("utilisateur_id"));
                banque.setNom(rs.getString("nom"));
                banque.setPrenom(rs.getString("prenom"));
                banque.setEmail(rs.getString("email"));
                banque.setTel(rs.getString("tel"));

                Timestamp timestamp = rs.getTimestamp("date_inscrit");
                if (timestamp != null) {
                    banque.setDateInscrit(timestamp.toLocalDateTime());
                }

                banque.setPhoto(rs.getString("photo"));

                banques.add(banque);
            }

            System.out.println("✓ " + banques.size() + " banque(s) récupérée(s)");

        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
        }

        return banques;
    }

    /**
     * RECHERCHER une banque par utilisateur ID
     */
    public Banque rechercherParUtilisateurId(int utilisateurId) throws SQLException {
        String sql = "SELECT b.*, u.nom, u.prenom, u.email, u.tel, u.date_inscrit, u.photo " +
                "FROM Banque b " +
                "INNER JOIN Utilisateur u ON b.utilisateur_id = u.id " +
                "WHERE b.utilisateur_id = ?";

        Banque banque = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            pst = connection.prepareStatement(sql);
            pst.setInt(1, utilisateurId);
            rs = pst.executeQuery();

            if (rs.next()) {
                banque = new Banque();

                banque.setBanqueId(rs.getInt("id"));
                banque.setUtilisateurId(rs.getInt("utilisateur_id"));
                banque.setCodeBanque(rs.getString("codebanque"));
                banque.setAddresseSiege(rs.getString("addresseSiege"));
                banque.setRepresentantLegal(rs.getString("representantLegal"));
                banque.setAdresseAgence(rs.getString("adresseAgence"));
                banque.setLogo(rs.getString("logo"));
                banque.setSiteWeb(rs.getString("siteweb"));
                banque.setStatusCompte(rs.getString("statusCompte"));
                banque.setCompteVerifie(rs.getBoolean("compteVerfiee"));

                banque.setId(utilisateurId);
                banque.setNom(rs.getString("nom"));
                banque.setPrenom(rs.getString("prenom"));
                banque.setEmail(rs.getString("email"));
                banque.setTel(rs.getString("tel"));

                Timestamp timestamp = rs.getTimestamp("date_inscrit");
                if (timestamp != null) {
                    banque.setDateInscrit(timestamp.toLocalDateTime());
                }

                banque.setPhoto(rs.getString("photo"));
            }

        } finally {
            if (rs != null) rs.close();
            if (pst != null) pst.close();
        }

        return banque;
    }
}