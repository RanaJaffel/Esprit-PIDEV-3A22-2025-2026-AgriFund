<?php

namespace App\Entity;

use App\Repository\Parametres2FARepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: Parametres2FARepository::class)]
class Parametres2FA
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne]
    private ?Utilisateur $utilisateur_id = null;

    #[ORM\Column(nullable: true)]
    private ?bool $est_active = null;

    #[ORM\Column(length: 100, nullable: true)]
    private ?string $methode_preferee = null;

    #[ORM\Column(length: 20, nullable: true)]
    private ?string $telephone_2fa = null;

    #[ORM\Column(nullable: true)]
    private ?\DateTime $date_activation = null;

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

    public function isEstActive(): ?bool
    {
        return $this->est_active;
    }

    public function setEstActive(?bool $est_active): static
    {
        $this->est_active = $est_active;

        return $this;
    }

    public function getMethodePreferee(): ?string
    {
        return $this->methode_preferee;
    }

    public function setMethodePreferee(?string $methode_preferee): static
    {
        $this->methode_preferee = $methode_preferee;

        return $this;
    }

    public function getTelephone2fa(): ?string
    {
        return $this->telephone_2fa;
    }

    public function setTelephone2fa(?string $telephone_2fa): static
    {
        $this->telephone_2fa = $telephone_2fa;

        return $this;
    }

    public function getDateActivation(): ?\DateTime
    {
        return $this->date_activation;
    }

    public function setDateActivation(?\DateTime $date_activation): static
    {
        $this->date_activation = $date_activation;

        return $this;
    }
}
