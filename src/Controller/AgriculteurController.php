<?php

namespace App\Controller;

use App\Entity\Agriculteur;
use App\Form\AgriculteurType;
use App\Repository\AgriculteurRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/agriculteur')]
final class AgriculteurController extends AbstractController
{
    #[Route(name: 'app_agriculteur_index', methods: ['GET'])]
    public function index(AgriculteurRepository $agriculteurRepository): Response
    {
        return $this->render('agriculteur/index.html.twig', [
            'agriculteurs' => $agriculteurRepository->findAll(),
        ]);
    }

    #[Route('/new', name: 'app_agriculteur_new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $agriculteur = new Agriculteur();
        $form = $this->createForm(AgriculteurType::class, $agriculteur);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->persist($agriculteur);
            $entityManager->flush();

            return $this->redirectToRoute('app_agriculteur_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('agriculteur/new.html.twig', [
            'agriculteur' => $agriculteur,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_agriculteur_show', methods: ['GET'])]
    public function show(Agriculteur $agriculteur): Response
    {
        return $this->render('agriculteur/show.html.twig', [
            'agriculteur' => $agriculteur,
        ]);
    }

    #[Route('/{id}/edit', name: 'app_agriculteur_edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, Agriculteur $agriculteur, EntityManagerInterface $entityManager): Response
    {
        $form = $this->createForm(AgriculteurType::class, $agriculteur);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $entityManager->flush();

            return $this->redirectToRoute('app_agriculteur_index', [], Response::HTTP_SEE_OTHER);
        }

        return $this->render('agriculteur/edit.html.twig', [
            'agriculteur' => $agriculteur,
            'form' => $form,
        ]);
    }

    #[Route('/{id}', name: 'app_agriculteur_delete', methods: ['POST'])]
    public function delete(Request $request, Agriculteur $agriculteur, EntityManagerInterface $entityManager): Response
    {
        if ($this->isCsrfTokenValid('delete'.$agriculteur->getId(), $request->getPayload()->getString('_token'))) {
            $entityManager->remove($agriculteur);
            $entityManager->flush();
        }

        return $this->redirectToRoute('app_agriculteur_index', [], Response::HTTP_SEE_OTHER);
    }
}
