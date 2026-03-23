<?php

namespace App\Entity;

use App\Repository\AdminRepository;
use Doctrine\ORM\Mapping as ORM;

#[ORM\Entity(repositoryClass: AdminRepository::class)]
class Admin
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(inversedBy: 'idadmin')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Utilisateur $utilsateur_id = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function setId(int $id): static
    {
        $this->id = $id;

        return $this;
    }

    public function getUtilsateurId(): ?Utilisateur
    {
        return $this->utilsateur_id;
    }

    public function setUtilsateurId(?Utilisateur $utilsateur_id): static
    {
        $this->utilsateur_id = $utilsateur_id;

        return $this;
    }
}
