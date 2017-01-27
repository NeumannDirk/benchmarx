package org.benchmarx.examples.familiestopersons.implementations.medini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

import org.apache.commons.io.output.NullOutputStream;
import org.benchmarx.BXToolForEMF;
import org.benchmarx.Configurator;
import org.benchmarx.families.core.FamiliesComparator;
import org.benchmarx.persons.core.PersonsComparator;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import Families.FamiliesFactory;
import Families.FamiliesPackage;
import Families.FamilyRegister;
import Persons.PersonRegister;
import Persons.PersonsPackage;
import Config.ConfigFactory;
import Config.ConfigPackage;
import Config.Configuration;
import de.ikv.emf.qvt.EMFQvtProcessorImpl;
import de.ikv.medini.qvt.QVTProcessorConsts;
import uk.ac.kent.cs.kmf.util.ILog;
import uk.ac.kent.cs.kmf.util.OutputStreamLog;

public class MediniQVTFamiliesToPersonsConfig extends BXToolForEMF<FamilyRegister, PersonRegister, Decisions> {
	private static final String RULESET = "families2personsconfig.qvt";

	private ILog logger;
	private EMFQvtProcessorImpl processorImpl;
	private ResourceSet resourceSet;
	private Resource source;
	private Resource config;
	private Resource target;
	private String basePath;
	private String transformation;
	private FileReader qvtRuleSet;
	private static final String fwdDir = "perDB";
	private static final String bwdDir = "famDB";
	private Configurator<Decisions> configurator;
	
	/**
	 * An extension of the standard XMIResourceFactory
	 * that allows the creation of XMIResources using UUIDs
	 * @author tbuchmann
	 *
	 */
	class UUIDResourceFactoryImpl extends XMIResourceFactoryImpl {

	    public UUIDResourceFactoryImpl() {
	        super();
	    }

	    @Override
	    public Resource createResource(URI uri) {
	        return new UUIDXMIResourceImpl(uri);
	    }
	}

	/**
	 * A simple extension of the standard XMIResource
	 * providing UUIDs.
	 *
	 * usage: Register UUIDResourceFactoryImpl
	 * ResourceSet set = new ResourceSetImpl();
	 * set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new UUIDResourceFactoryImpl());
	 *
	 * @author tbuchmann
	 *
	 */
	class UUIDXMIResourceImpl extends XMIResourceImpl implements Resource {

	    public UUIDXMIResourceImpl() {
	        super();
	    }

	    public UUIDXMIResourceImpl(URI uri) {
	        super(uri);
	    }

	    @Override
	    protected boolean useUUIDs() {
	        return true;
	    }
	} 

	
	public MediniQVTFamiliesToPersonsConfig() {
		super(new FamiliesComparator(), new PersonsComparator());
		
		logger = new OutputStreamLog(new PrintStream(new NullOutputStream())); 
		// logger = new OutputStreamLog(System.err); 
		
		processorImpl = new EMFQvtProcessorImpl(this.logger);
		processorImpl.setProperty(QVTProcessorConsts.PROP_DEBUG, "true");
		basePath = "./src/org/benchmarx/examples/familiestopersons/implementations/medini/base/";
		
		// Tell the QVT engine, which transformation to execute
		transformation = "families2personsconfig";

		// Tell the QVT engine a directory to work in - e.g. to store the trace (meta)models
		File tracesFile = new File(basePath + "traces/trace.trafo");
		tracesFile.delete();
	}
	
	@Override
	public void initiateSynchronisationDialogue() {
		// Initialise resource set of models
		this.resourceSet = new ResourceSetImpl();
		
		// Use the following lines without XMI ids
		this.resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
		    Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		
		// Use the next line with XMI ids
		// this.resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new UUIDResourceFactoryImpl());	
		
		// Collect all necessary packages from the metamodel(s)
		Collection<EPackage> metaPackages = new ArrayList<EPackage>();
		this.collectMetaModels(metaPackages);

		// Make these packages known to the QVT engine
		init(metaPackages);
		
		// Create resources for models
		source = resourceSet.createResource(URI.createURI("source.xmi"));
		config = resourceSet.createResource(URI.createURI("config.xmi"));
		target = resourceSet.createResource(URI.createURI("target.xmi"));
		
		// Collect the models, which should participate in the transformation.
		// You can provide a list of models for each direction.
		// The models must be added in the same order as defined in your transformation!
		Collection<Collection<Resource>> modelResources = new ArrayList<Collection<Resource>>();
		Collection<Resource> firstSetOfModels = new ArrayList<Resource>();
		Collection<Resource> secondSetOfModels = new ArrayList<Resource>();
		Collection<Resource> thirdSetOfModels = new ArrayList<Resource>();
		modelResources.add(firstSetOfModels);
		modelResources.add(secondSetOfModels);
		modelResources.add(thirdSetOfModels);
		firstSetOfModels.add(source);
		secondSetOfModels.add(config);
		thirdSetOfModels.add(target);
		
		URI directory = URI.createFileURI(basePath + "traces");
		this.preExecution(modelResources, directory);
		
		// Call setConfigurator, which will initialize the configurator with default decisions
		setConfigurator(new Configurator<Decisions>());
		
		source.getContents().add(FamiliesFactory.eINSTANCE.createFamilyRegister());
		config.getContents().add(ConfigFactory.eINSTANCE.createConfiguration());
		
		/* With XMI ids include the following lines
		try {
			source.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			config.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		launchFWD();
		
		/* With XMI ids include the following lines
		try {
			target.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	private void launchFWD() {
		Configuration configuration = (Configuration) config.getContents().get(0);
		// Set direction in the configuration model
		configuration.setFromPersonsToFamilies(false);
		launch(fwdDir);
	}
	
	private void launchBWD() {
		Configuration configuration = (Configuration) config.getContents().get(0);
		// Set direction in the configuration model
		configuration.setFromPersonsToFamilies(true);
		// Copy configuration parameters into configuration model
		boolean preferCreatingParentToChild = configurator.decide(Decisions.PREFER_CREATING_PARENT_TO_CHILD);
		configuration.setPreferParentToChild(preferCreatingParentToChild);
		boolean preferExistingFamilyToNew = configurator.decide(Decisions.PREFER_EXISTING_FAMILY_TO_NEW);
		configuration.setPreferExistingToNewFamily(preferExistingFamilyToNew);
		launch(bwdDir);
	}

	@Override
	public void performAndPropagateTargetEdit(Consumer<PersonRegister> edit) {
		edit.accept(getTargetModel());
		
		/* With XMI ids include the following lines
		try {
			target.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		launchBWD();
		
		/* With XMI ids include the following lines
		try {
			source.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	@Override
	public void performAndPropagateSourceEdit(Consumer<FamilyRegister> edit) {
		edit.accept(getSourceModel());
		
		/* With XMI ids include the following lines
		try {
			source.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		launchFWD();
		
		/* With XMI ids include the following lines
		try {
			target.save(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	@Override
	public FamilyRegister getSourceModel() {
		return (FamilyRegister) source.getContents().get(0);
	}

	@Override
	public PersonRegister getTargetModel() {
		return (PersonRegister) target.getContents().get(0);
	}

	@Override
	public void setConfigurator(Configurator<Decisions> configurator) {
		// Store the passed configurator reference and initialize it
		// default decisions, which can be overridden
		this.configurator = configurator;
		configurator.
			makeDecision(Decisions.PREFER_CREATING_PARENT_TO_CHILD, true).
			makeDecision(Decisions.PREFER_EXISTING_FAMILY_TO_NEW, true);
	}

	/**
	 * Provide the information about the metamodels, which are involved in the transformation
	 * 
	 * @param ePackages
	 *            the metamodel packages
	 */
	public void init(Collection<EPackage> ePackages) {
		Iterator<EPackage> iterator = ePackages.iterator();
		while (iterator.hasNext()) {
			this.processorImpl.addMetaModel(iterator.next());
		}
	}

	/**
	 * Before transforming, the engine has to know the model to perform the transformation on, as
	 * well as a directory for the traces to store.
	 * 
	 * @param modelResources
	 *            models for the execution - take care of the right order!
	 * @param workingDirectory
	 *            working directory of the QVT engine
	 */
	public void preExecution(Collection<?> modelResources, URI workingDirectory) {
		this.processorImpl.setWorkingLocation(workingDirectory);
		this.processorImpl.setModels(modelResources);
	}

	/**
	 * Transform a QVT script in a specific direction.
	 * 
	 * @param qvtRuleSet
	 *            the QVT transformation
	 * @param transformation
	 *            name of the transformation
	 * @param direction
	 *            name of the target - must conform to your QVT transformation definition
	 * @throws Throwable
	 */
	public void transform(Reader qvtRuleSet, String transformation, String direction) throws Throwable {
		processorImpl.evaluateQVT(qvtRuleSet, transformation, true, direction, new Object[0], null, this.logger);
	}

	public void launch(String direction) {
		PrintStream ps = System.out;
		PrintStream ps_err = System.err;
		
		// Load the QVT relations
		try {
			System.setOut(new PrintStream(new NullOutputStream()));
			System.setErr(new PrintStream(new NullOutputStream()));

			qvtRuleSet = new FileReader(basePath + RULESET);
			this.transform(qvtRuleSet, transformation, direction);
		} catch (FileNotFoundException fileNotFoundException) {
			fileNotFoundException.printStackTrace();
			return;
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		} finally {		
			System.setOut(ps);
			System.setErr(ps_err);
		}
	}

	/**
	 * Add all metamodel packages of models/qvt script
	 * 
	 * @param metaPackages
	 * @return
	 */
	protected void collectMetaModels(Collection<EPackage> metaPackages) {
		metaPackages.add(PersonsPackage.eINSTANCE);
		metaPackages.add(ConfigPackage.eINSTANCE);
		metaPackages.add(FamiliesPackage.eINSTANCE);
	}
}
