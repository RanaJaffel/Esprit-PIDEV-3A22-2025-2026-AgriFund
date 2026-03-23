<?php

namespace App\Entity;

use App\Repository\AgriculteurRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: AgriculteurRepository::class)]
class Agriculteur
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'agriid')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Utilisateur $utilisateur_id = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $adresseferme = null;

    #[ORM\Column(type: Types::DECIMAL, precision: 10, scale: 2, nullable: true)]
    private ?string $superficieferme = null;

    #[ORM\Column(length: 100, nullable: true)]
    private ?string $typeculture = null;

    #[ORM\Column(length: 50, nullable: true)]
    private ?string $statuscompte = null;

    #[ORM\Column(nullable: true)]
    private ?bool $compteverifie = null;

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

    public function getAdresseferme(): ?string
    {
        return $this->adresseferme;
    }

    public function setAdresseferme(?string $adresseferme): static
    {
        $this->adresseferme = $adresseferme;

        return $this;
    }

    public function getSuperficieferme(): ?string
    {
        return $this->superficieferme;
    }

    public function setSuperficieferme(?string $superficieferme): static
    {
        $this->superficieferme = $superficieferme;

        return $this;
    }

    public function getTypeculture(): ?string
    {
        return $this->typeculture;
    }

    public function setTypeculture(?string $typeculture): static
    {
        $this->typeculture = $typeculture;

        return $this;
    }

    public function getStatuscompte(): ?string
    {
        return $this->statuscompte;
    }

    public function setStatuscompte(?string $statuscompte): static
    {
        $this->statuscompte = $statuscompte;

        return $this;
    }

    public function isCompteverifie(): ?bool
    {
        return $this->compteverifie;
    }

    public function setCompteverifie(?bool $compteverifie): static
    {
        $this->compteverifie = $compteverifie;

        return $this;
    }
}
