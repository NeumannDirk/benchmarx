package org.benchmarx.examples.familiestopersons.implementations.vitruvius;

import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.benchmarx.Configurator;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.emf.Comparator;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.benchmarx.families.core.FamiliesComparator;
import org.benchmarx.persons.core.PersonsComparator;

import Families.FamiliesFactory;
import Families.Family;
import Families.FamilyRegister;
import Persons.Person;
import Persons.PersonRegister;
import Persons.PersonsFactory;

//import mir.reactions.familiesToPersons;
//import java.nio.file.Path;
/**
 * This class implements the bx tool interface for the Vitruvius tool. Vitruvius is
 * delta-based and corr-based.
 * 
 * @author Dirk Neumann
 */
public class VitruviusFamiliesToPersons extends BXToolForEMF<FamilyRegister, PersonRegister, Decisions>{
	
	private FamilyRegister src;
	private PersonRegister trg;
	private String resultSrc;
	private String resultTrg;
	private FamiliesComparator srcHelper = new FamiliesComparator();
	private PersonsComparator trgHelper = new PersonsComparator();

//	static Path PERSONS_MODEL = DomainUtil.getModelFileName("model/persons", new PersonsDomainProvider())
//	static Path FAMILIES_MODEL = DomainUtil.getModelFileName("model/families", new FamiliesDomainProvider())
	
	
	@Override
	public String getName() {
		return "Vitruvius";		
	}
	
	public VitruviusFamiliesToPersons() {
		super(new FamiliesComparator(), new PersonsComparator());
	}	
	public VitruviusFamiliesToPersons(Comparator<FamilyRegister> src, Comparator<PersonRegister> trg) {
		super(src, trg);
	}

	@Override
	public void initiateSynchronisationDialogue() {		
		src = FamiliesFactory.eINSTANCE.createFamilyRegister();
		trg = PersonsFactory.eINSTANCE.createPersonRegister();
		resultSrc = srcHelper.familyToString(src);
		resultTrg = trgHelper.personsToString(trg);
		
		int x = 0;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void performAndPropagateSourceEdit(Consumer<FamilyRegister> edit) {		
		edit.accept(src);	
		//this.<FamilyRegister>propagate(src, edit);
		
		
		printFamilyRegister(src);
		printPersonRegister(trg);
		int x = 0;
	}
	
	public void printFamilyRegister(FamilyRegister freg) {
		if(freg.getFamilies() == null || freg.getFamilies().size() == 0) {
			System.out.println("Empty FamilyRegister.");
			return;
		}
		for(int i = 0; i < freg.getFamilies().size(); i++) {
			Family fam = freg.getFamilies().get(i);
			System.out.println(fam.getName());
			if(fam.getFather() != null) {
				System.out.println("\tFather: " + fam.getFather().getName());				
			}
			if(fam.getMother() != null) {
				System.out.println("\tMother: " + fam.getMother().getName());				
			}
			if(fam.getSons() != null && fam.getSons().size() > 0) {
				System.out.print("\tSons:(");
				for(int s = 0; s < fam.getSons().size(); s++) {
					System.out.print(fam.getSons().get(s).getName());
					if(s != fam.getSons().size() - 1) {
						System.out.print(", ");
					}
				}
				System.out.println(")");
			}
			if(fam.getDaughters() != null && fam.getDaughters().size() > 0) {
				System.out.print("\tSons:(");
				for(int d = 0; d < fam.getDaughters().size(); d++) {
					System.out.print(fam.getDaughters().get(d).getName());
					if(d != fam.getDaughters().size() - 1) {
						System.out.print(", ");
					}
				}
				System.out.println(")");
			}
		}		
	}
	
	public void printPersonRegister(PersonRegister preg) {
		if(preg.getPersons() == null || preg.getPersons().size() == 0) {
			System.out.println("Empty PersonRegister.");
			return;
		}
		for(int i = 0; i < preg.getPersons().size(); i++) {
			Person p = preg.getPersons().get(i);
			String pn = p.getName();
			if(p.getBirthday() != null) {
				System.out.println(pn + "(" + p.getBirthday().toString() + ")");
			}
			else {
				System.out.println(pn + "(-)");
			}
		}		
	}

	@Override
	public void performAndPropagateTargetEdit(Consumer<PersonRegister> edit) {
		int x = 0;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void performIdleSourceEdit(Consumer<FamilyRegister> edit) {
		int x = 0;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void performIdleTargetEdit(Consumer<PersonRegister> edit) {
		int x = 0;
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		int x = 0;
		// TODO Auto-generated method stub
		
	}

	@Override
	public FamilyRegister getSourceModel() {
		int x = 0;
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersonRegister getTargetModel() {
		int x = 0;
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveModels(String name) {
		int x = 0;
		// TODO Auto-generated method stub
		
	}

}
