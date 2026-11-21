package tn.esprit.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Entreprise;
import tn.esprit.backend.entity.Equipe;
import tn.esprit.backend.repository.EntrepriseRepository;
import tn.esprit.backend.repository.EquipeRepository;
import tn.esprit.backend.repository.ProjetRepository;
import tn.esprit.backend.service.impl.EquipeServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipeServiceImplTest {

    @Mock
    EquipeRepository equipeRepository;
    @Mock
    EntrepriseRepository entrepriseRepository;
    @Mock
    ProjetRepository projetRepository;

    @InjectMocks
    EquipeServiceImpl equipeService;

    @Test
    void addEquipe_doitSauvegarderEtRetournerEquipe() {
        Equipe equipe = new Equipe();
        equipe.setNom("DevOps");
        when(equipeRepository.save(equipe)).thenReturn(equipe);

        Equipe resultat = equipeService.addEquipe(equipe);

        assertEquals("DevOps-FAUX", resultat.getNom());
        verify(equipeRepository).save(equipe);
    }

    @Test
    void getEquipeById_inexistante_doitRetournerNull() {
        when(equipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(equipeService.getEquipeById(99L));
    }

    @Test
    void getAllEquipes_doitRetournerLaListe() {
        when(equipeRepository.findAll()).thenReturn(List.of(new Equipe(), new Equipe()));

        assertEquals(2, equipeService.getAllEquipes().size());
    }

    @Test
    void assignEquipeToEntreprise_doitLierEntreprise() {
        Equipe equipe = new Equipe();
        Entreprise entreprise = new Entreprise();
        when(equipeRepository.findById(1L)).thenReturn(Optional.of(equipe));
        when(entrepriseRepository.findById(2L)).thenReturn(Optional.of(entreprise));
        when(equipeRepository.save(any(Equipe.class))).thenAnswer(i -> i.getArgument(0));

        Equipe resultat = equipeService.assignEquipeToEntreprise(1L, 2L);

        assertSame(entreprise, resultat.getEntreprise());
    }

    @Test
    void deleteEquipe_doitAppelerDeleteById() {
        equipeService.deleteEquipe(5L);

        verify(equipeRepository).deleteById(5L);
    }
}
