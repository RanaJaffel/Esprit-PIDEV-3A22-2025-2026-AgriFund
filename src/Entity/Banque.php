<?php

namespace App\Entity;

use App\Repository\BanqueRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: BanqueRepository::class)]
class Banque
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'banqueid')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Utilisateur $utilisateur_id = null;

    #[ORM\Column(length: 50)]
    private ?string $codebanque = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $addresseSiege = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $representantLegal = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $adresseAgence = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $logo = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $siteweb = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $statusCompte = null;

    #[ORM\Column(nullable: true)]
    private ?bool $compteverfiee = null;

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

    public function getCodebanque(): ?string
    {
        return $this->codebanque;
    }

    public function setCodebanque(string $codebanque): static
    {
        $this->codebanque = $codebanque;

        return $this;
    }

    public function getAddresseSiege(): ?string
    {
        return $this->addresseSiege;
    }

    public function setAddresseSiege(?string $addresseSiege): static
    {
        $this->addresseSiege = $addresseSiege;

        return $this;
    }

    public function getRepresentantLegal(): ?string
    {
        return $this->representantLegal;
    }

    public function setRepresentantLegal(?string $representantLegal): static
    {
        $this->representantLegal = $representantLegal;

        return $this;
    }

    public function getAdresseAgence(): ?string
    {
        return $this->adresseAgence;
    }

    public function setAdresseAgence(?string $adresseAgence): static
    {
        $this->adresseAgence = $adresseAgence;

        return $this;
    }

    public function getLogo(): ?string
    {
        return $this->logo;
    }

    public function setLogo(?string $logo): static
    {
        $this->logo = $logo;

        return $this;
    }

    public function getSiteweb(): ?string
    {
        return $this->siteweb;
    }

    public function setSiteweb(?string $siteweb): static
    {
        $this->siteweb = $siteweb;

        return $this;
    }

    public function getStatusCompte(): ?string
    {
        return $this->statusCompte;
    }

    public function setStatusCompte(?string $statusCompte): static
    {
        $this->statusCompte = $statusCompte;

        return $this;
    }

    public function isCompteverfiee(): ?bool
    {
        return $this->compteverfiee;
    }

    public function setCompteverfiee(?bool $compteverfiee): static
    {
        $this->compteverfiee = $compteverfiee;

        return $this;
    }
}
