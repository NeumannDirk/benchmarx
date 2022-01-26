package org.benchmarx.examples.familiestopersons.implementations.vitruvius;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.benchmarx.Configurator;
import org.benchmarx.emf.BXToolForEMF;
import org.benchmarx.emf.Comparator;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.benchmarx.families.core.FamiliesComparator;
import org.benchmarx.persons.core.PersonsComparator;

import Families.FamilyRegister;
import Persons.PersonRegister;

/**
 * This class implements the bx tool interface for the Vitruvius tool. Vitruvius is
 * delta-based and corr-based.
 * 
 * @author Dirk Neumann
 */
public class VitruviusFamiliesToPersons extends BXToolForEMF<FamilyRegister, PersonRegister, Decisions>{
	
	private BenchmarxApplicationTest benchmarxTest = new BenchmarxApplicationTest();
	private FamilyRegister src;
	private PersonRegister trg;
//	private FamiliesComparator srcHelper = new FamiliesComparator();
//	private PersonsComparator trgHelper = new PersonsComparator();
	static int testCount = 0;
	
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
		System.out.println("\n\n");
		
		//TODO nur temporär
	    Path vsumPath = new File("\\model\\vsum").toPath();
	    Path testProjectPath = new File("\\model").toPath();
//	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");  
//	    Date date = new Date();  
//	    String dateString = formatter.format(date);	  
//	    @TestProject Path testProjectPath,
//		@TestProject(variant="vsum") Path vsumPath
//		Path vsumPath = new File("C:\\testModels\\" + dateString + "\\" + testCount + "\\vsum").toPath();
//		Path testProjectPath = new File("C:\\testModels\\" + dateString + "\\" + testCount).toPath();		
		//Path testProjectPath = Path.of(System.getProperty("user.dir") + "\\testModels\\" + dateString + "\\" + testCount);
		
		this.benchmarxTest.prepare(testProjectPath, vsumPath);
		
		this.setLocalRegisters();		
	}

	private void setLocalRegisters() {
		this.src = this.benchmarxTest.getFamiliyRegister();
		this.trg = this.benchmarxTest.getPersonRegister();		
	}
	private void printLocalRegisters() {
		ModelPrinter.printFamilyRegister(src);
		ModelPrinter.printPersonRegister(trg);
		System.out.println("\n");
	}
	
	@Override
	public void performAndPropagateSourceEdit(Consumer<FamilyRegister> edit) {		
		System.out.println(testCount + ": Fam->Per");
		testCount++;
		this.benchmarxTest.performAndPropagateSourceEdit(edit);		
		this.setLocalRegisters();
		this.printLocalRegisters();
	}
	
	@Override
	public void performAndPropagateTargetEdit(Consumer<PersonRegister> edit) {
		System.out.println(testCount + ": Per->Fam");
		testCount++;
		this.benchmarxTest.performAndPropagateTargetEdit(edit);		
		this.setLocalRegisters();
		this.printLocalRegisters();
	}
	
	@Override
	public void performIdleSourceEdit(Consumer<FamilyRegister> edit) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void performIdleTargetEdit(Consumer<PersonRegister> edit) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		this.benchmarxTest.setConfigurator(configurator);
		// TODO Auto-generated method stub		
	}

	@Override
	public FamilyRegister getSourceModel() {
		return this.benchmarxTest.getFamiliyRegister();
	}

	@Override
	public PersonRegister getTargetModel() {
		return this.benchmarxTest.getPersonRegister();
	}

	@Override
	public void saveModels(String name) {
		this.benchmarxTest.endTest();	
	}
}
