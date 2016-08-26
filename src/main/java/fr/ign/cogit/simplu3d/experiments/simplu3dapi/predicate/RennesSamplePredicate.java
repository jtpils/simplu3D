package fr.ign.cogit.simplu3d.experiments.simplu3dapi.predicate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.ign.cogit.simplu3d.checker.Checker;
import fr.ign.cogit.simplu3d.checker.Rules;
import fr.ign.cogit.simplu3d.model.BasicPropertyUnit;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.impl.Cuboid;
import fr.ign.mpp.configuration.AbstractBirthDeathModification;
import fr.ign.mpp.configuration.AbstractGraphConfiguration;
import fr.ign.rjmcmc.configuration.ConfigurationModificationPredicate;

/**
 * Predicate for test class on Rennes
 * 
 * 
 * @author MBrasebin
 *
 * @param <O>
 * @param <C>
 * @param <M>
 */
public class RennesSamplePredicate<O extends Cuboid, C extends AbstractGraphConfiguration<O, C, M>, M extends AbstractBirthDeathModification<O, C, M>>
		implements ConfigurationModificationPredicate<C, M> {

	private BasicPropertyUnit currentBPU = null;
	private Rules currentRules = null;

	public RennesSamplePredicate(BasicPropertyUnit bPU, Rules rules) {
		this.currentBPU = bPU;
		this.currentRules = rules;

	}

	@Override
	public boolean check(C c, M m) {

		// On récupère la liste courante des bâtiments
		List<O> lBuildings = new ArrayList<>();

		Iterator<O> iTBat = c.iterator();
		while (iTBat.hasNext()) {
			lBuildings.add(iTBat.next());

		}

		lBuildings.addAll(m.getBirth());
		lBuildings.removeAll(m.getDeath());

		// On ajoute les bâtiments à la sous parcelle en cours (bien sur s'il y
		// a plusieurs sous parcelles faudrait faire une intégration plus
		// propre)
		for (Cuboid b : lBuildings) {
			this.currentBPU.getCadastralParcel().get(0).getSubParcel().get(0).getBuildingsParts().add(b);
		}
		
		if(lBuildings.isEmpty()){
			return true;
		}

		// On vérifie que la liste des contraintes non respsectées est vide
		boolean checked = Checker.check(currentBPU, currentRules,true).isEmpty();

		// On vire les
		for (Cuboid b : lBuildings) {
			this.currentBPU.getCadastralParcel().get(0).getSubParcel().get(0).getBuildingsParts().remove(b);
		}

		return checked;
	}

}