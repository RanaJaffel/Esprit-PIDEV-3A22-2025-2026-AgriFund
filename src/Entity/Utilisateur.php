<?php

namespace App\Entity;

use App\Repository\UtilisateurRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: UtilisateurRepository::class)]
class Utilisateur
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 100)]
    private ?string $nom = null;

    #[ORM\Column(length: 100)]
    private ?string $prenom = null;

    #[ORM\Column(length: 255)]
    private ?string $email = null;

    #[ORM\Column(length: 255)]
    private ?string $password = null;

    #[ORM\Column(length: 20, nullable: true)]
    private ?string $tel = null;

    #[ORM\Column(nullable: true)]
    private ?\DateTime $date_inscrit = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $photo = null;

    #[ORM\Column]
    private ?\DateTime $derniere_connexion = null;

    #[ORM\Column(nullable: true)]
    private ?bool $est_en_ligne = null;

    /**
     * @var Collection<int, Admin>
     */
    #[ORM\OneToMany(targetEntity: Admin::class, mappedBy: 'utilsateur_id', orphanRemoval: true)]
    private Collection $idadmin;

    /**
     * @var Collection<int, Agriculteur>
     */
    #[ORM\OneToMany(targetEntity: Agriculteur::class, mappedBy: 'utilisateur_id', orphanRemoval: true)]
    private Collection $agriid;

    /**
     * @var Collection<int, Banque>
     */
    #[ORM\OneToMany(targetEntity: Banque::class, mappedBy: 'utilisateur_id', orphanRemoval: true)]
    private Collection $banqueid;

    /**
     * @var Collection<int, StatutEnLigne>
     */
    #[ORM\OneToMany(targetEntity: StatutEnLigne::class, mappedBy: 'utilisateur_id')]
    private Collection $statut;

    /**
     * @var Collection<int, HistoriqueConnexion>
     */
    #[ORM\OneToMany(targetEntity: HistoriqueConnexion::class, mappedBy: 'utilisateur_id', orphanRemoval: true)]
    private Collection $connid;

    public function __construct()
    {
        $this->idadmin = new ArrayCollection();
        $this->agriid = new ArrayCollection();
        $this->banqueid = new ArrayCollection();
        $this->statut = new ArrayCollection();
        $this->connid = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function setId(int $id): static
    {
        $this->id = $id;

        return $this;
    }

    public function getNom(): ?string
    {
        return $this->nom;
    }

    public function setNom(string $nom): static
    {
        $this->nom = $nom;

        return $this;
    }

    public function getPrenom(): ?string
    {
        return $this->prenom;
    }

    public function setPrenom(string $prenom): static
    {
        $this->prenom = $prenom;

        return $this;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(string $email): static
    {
        $this->email = $email;

        return $this;
    }

    public function getPassword(): ?string
    {
        return $this->password;
    }

    public function setPassword(string $password): static
    {
        $this->password = $password;

        return $this;
    }

    public function getTel(): ?string
    {
        return $this->tel;
    }

    public function setTel(?string $tel): static
    {
        $this->tel = $tel;

        return $this;
    }

    public function getDateInscrit(): ?\DateTime
    {
        return $this->date_inscrit;
    }

    public function setDateInscrit(?\DateTime $date_inscrit): static
    {
        $this->date_inscrit = $date_inscrit;

        return $this;
    }

    public function getPhoto(): ?string
    {
        return $this->photo;
    }

    public function setPhoto(?string $photo): static
    {
        $this->photo = $photo;

        return $this;
    }

    public function getDerniereConnexion(): ?\DateTime
    {
        return $this->derniere_connexion;
    }

    public function setDerniereConnexion(\DateTime $derniere_connexion): static
    {
        $this->derniere_connexion = $derniere_connexion;

        return $this;
    }

    public function isEstEnLigne(): ?bool
    {
        return $this->est_en_ligne;
    }

    public function setEstEnLigne(?bool $est_en_ligne): static
    {
        $this->est_en_ligne = $est_en_ligne;

        return $this;
    }

    /**
     * @return Collection<int, Admin>
     */
    public function getIdadmin(): Collection
    {
        return $this->idadmin;
    }

    public function addIdadmin(Admin $idadmin): static
    {
        if (!$this->idadmin->contains($idadmin)) {
            $this->idadmin->add($idadmin);
            $idadmin->setUtilsateurId($this);
        }

        return $this;
    }

    public function removeIdadmin(Admin $idadmin): static
    {
        if ($this->idadmin->removeElement($idadmin)) {
            // set the owning side to null (unless already changed)
            if ($idadmin->getUtilsateurId() === $this) {
                $idadmin->setUtilsateurId(null);
            }
        }

        return $this;
    }

    /**
     * @return Collection<int, Agriculteur>
     */
    public function getAgriid(): Collection
    {
        return $this->agriid;
    }

    public function addAgriid(Agriculteur $agriid): static
    {
        if (!$this->agriid->contains($agriid)) {
            $this->agriid->add($agriid);
            $agriid->setUtilisateurId($this);
        }

        return $this;
    }

    public function removeAgriid(Agriculteur $agriid): static
    {
        if ($this->agriid->removeElement($agriid)) {
            // set the owning side to null (unless already changed)
            if ($agriid->getUtilisateurId() === $this) {
                $agriid->setUtilisateurId(null);
            }
        }

        return $this;
    }

    /**
     * @return Collection<int, Banque>
     */
    public function getBanqueid(): Collection
    {
        return $this->banqueid;
    }

    public function addBanqueid(Banque $banqueid): static
    {
        if (!$this->banqueid->contains($banqueid)) {
            $this->banqueid->add($banqueid);
            $banqueid->setUtilisateurId($this);
        }

        return $this;
    }

    public function removeBanqueid(Banque $banqueid): static
    {
        if ($this->banqueid->removeElement($banqueid)) {
            // set the owning side to null (unless already changed)
            if ($banqueid->getUtilisateurId() === $this) {
                $banqueid->setUtilisateurId(null);
            }
        }

        return $this;
    }

    /**
     * @return Collection<int, StatutEnLigne>
     */
    public function getStatut(): Collection
    {
        return $this->statut;
    }

    public function addStatut(StatutEnLigne $statut): static
    {
        if (!$this->statut->contains($statut)) {
            $this->statut->add($statut);
            $statut->setUtilisateurId($this);
        }

        return $this;
    }

    public function removeStatut(StatutEnLigne $statut): static
    {
        if ($this->statut->removeElement($statut)) {
            // set the owning side to null (unless already changed)
            if ($statut->getUtilisateurId() === $this) {
                $statut->setUtilisateurId(null);
            }
        }

        return $this;
    }

    /**
     * @return Collection<int, HistoriqueConnexion>
     */
    public function getConnid(): Collection
    {
        return $this->connid;
    }

    public function addConnid(HistoriqueConnexion $connid): static
    {
        if (!$this->connid->contains($connid)) {
            $this->connid->add($connid);
            $connid->setUtilisateurId($this);
        }

        return $this;
    }

    public function removeConnid(HistoriqueConnexion $connid): static
    {
        if ($this->connid->removeElement($connid)) {
            // set the owning side to null (unless already changed)
            if ($connid->getUtilisateurId() === $this) {
                $connid->setUtilisateurId(null);
            }
        }

        return $this;
    }
}
