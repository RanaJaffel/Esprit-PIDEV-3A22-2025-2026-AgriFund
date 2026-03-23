<?php

namespace App\Entity;

use App\Repository\StatutEnLigneRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: StatutEnLigneRepository::class)]
class StatutEnLigne
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'statut')]
    private ?Utilisateur $utilisateur_id = null;

    #[ORM\Column]
    private ?bool $est_en_ligne = null;

    #[ORM\Column(length: 255)]
    private ?string $datetime = null;

    #[ORM\Column(nullable: true)]
    private ?\DateTime $derniere_activite = null;

    public function getId(): ?int
    {
        return $this->id;
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

    public function isEstEnLigne(): ?bool
    {
        return $this->est_en_ligne;
    }

    public function setEstEnLigne(bool $est_en_ligne): static
    {
        $this->est_en_ligne = $est_en_ligne;

        return $this;
    }

    public function getDatetime(): ?string
    {
        return $this->datetime;
    }

    public function setDatetime(string $datetime): static
    {
        $this->datetime = $datetime;

        return $this;
    }

    public function getDerniereActivite(): ?\DateTime
    {
        return $this->derniere_activite;
    }

    public function setDerniereActivite(?\DateTime $derniere_activite): static
    {
        $this->derniere_activite = $derniere_activite;

        return $this;
    }
}
