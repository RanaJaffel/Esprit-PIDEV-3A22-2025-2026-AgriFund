<?php

namespace App\Entity;

use App\Repository\HistoriqueConnexionRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: HistoriqueConnexionRepository::class)]
class HistoriqueConnexion
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'connid')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Utilisateur $utilisateur_id = null;

    #[ORM\Column(nullable: true)]
    private ?\DateTime $date_connexion = null;

    #[ORM\Column(length: 45, nullable: true)]
    private ?string $adresse_ip = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $navigateur = null;

    #[ORM\Column(length: 100, nullable: true)]
    private ?string $systeme_exploitation = null;

    #[ORM\Column(nullable: true)]
    private ?bool $connexion_reussie = null;

    #[ORM\Column(length: 100, nullable: true)]
    private ?string $methode_auth = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $localisation = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function setId(int $id): static
    {
        $this->id = $id;

        return $this;
    }

    public function getUtilisateurId(): ?Utilisateur
    {
        return $this->utilisateur_id;
    }

    public function setUtilisateurId(?Utilisateur $utilisateur_id): static
    {
        $this->utilisateur_id = $utilisateur_id;

        return $this;
    }

    public function getDateConnexion(): ?\DateTime
    {
        return $this->date_connexion;
    }

    public function setDateConnexion(?\DateTime $date_connexion): static
    {
        $this->date_connexion = $date_connexion;

        return $this;
    }

    public function getAdresseIp(): ?string
    {
        return $this->adresse_ip;
    }

    public function setAdresseIp(?string $adresse_ip): static
    {
        $this->adresse_ip = $adresse_ip;

        return $this;
    }

    public function getNavigateur(): ?string
    {
        return $this->navigateur;
    }

    public function setNavigateur(?string $navigateur): static
    {
        $this->navigateur = $navigateur;

        return $this;
    }

    public function getSystemeExploitation(): ?string
    {
        return $this->systeme_exploitation;
    }

    public function setSystemeExploitation(?string $systeme_exploitation): static
    {
        $this->systeme_exploitation = $systeme_exploitation;

        return $this;
    }

    public function isConnexionReussie(): ?bool
    {
        return $this->connexion_reussie;
    }

    public function setConnexionReussie(?bool $connexion_reussie): static
    {
        $this->connexion_reussie = $connexion_reussie;

        return $this;
    }

    public function getMethodeAuth(): ?string
    {
        return $this->methode_auth;
    }

    public function setMethodeAuth(?string $methode_auth): static
    {
        $this->methode_auth = $methode_auth;

        return $this;
    }

    public function getLocalisation(): ?string
    {
        return $this->localisation;
    }

    public function setLocalisation(?string $localisation): static
    {
        $this->localisation = $localisation;

        return $this;
    }
}
