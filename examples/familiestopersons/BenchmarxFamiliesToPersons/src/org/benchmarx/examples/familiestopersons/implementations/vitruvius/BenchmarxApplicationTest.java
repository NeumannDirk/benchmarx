package org.benchmarx.examples.familiestopersons.implementations.vitruvius;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;

import org.benchmarx.Configurator;
import org.benchmarx.examples.familiestopersons.testsuite.Decisions;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.xbase.lib.Functions.Function1;

import Families.FamiliesFactory;
import Families.FamilyRegister;
import Persons.PersonRegister;
import Persons.PersonsFactory;
import tools.vitruv.applications.demo.familiespersons.families2persons.FamiliesToPersonsChangePropagationSpecification;
import tools.vitruv.applications.demo.familiespersons.persons2families.PersonsToFamiliesChangePropagationSpecification;
import tools.vitruv.domains.demo.families.FamiliesDomainProvider;
import tools.vitruv.domains.demo.persons.PersonsDomainProvider;
import tools.vitruv.framework.domains.VitruvDomain;
import tools.vitruv.framework.domains.repository.VitruvDomainRepository;
import tools.vitruv.framework.domains.repository.VitruvDomainRepositoryImpl;
import tools.vitruv.framework.propagation.ChangePropagationSpecification;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.testutils.ChangePublishingTestView;
import tools.vitruv.testutils.TestUserInteraction;
import tools.vitruv.testutils.TestUserInteraction.MultipleChoiceInteractionDescription;
import tools.vitruv.testutils.TestUserInteraction.MultipleChoiceResponseBuilder;
import tools.vitruv.testutils.TestView;
import tools.vitruv.testutils.UriMode;
import tools.vitruv.testutils.VitruvApplicationTest;
import tools.vitruv.testutils.domains.DomainUtil;

public class BenchmarxApplicationTest extends VitruvApplicationTest {

	static Path PERSONS_MODEL = DomainUtil.getModelFileName("model/persons", new PersonsDomainProvider());
	static Path FAMILIES_MODEL = DomainUtil.getModelFileName("model/families", new FamiliesDomainProvider());
	private Boolean preferAdult = null;
	private Boolean preferExistingFamily = null;
	private MultipleChoiceResponseBuilder parentChildResponseBuilder = null;
	private MultipleChoiceResponseBuilder existingOrNewFamilyResponseBuilder = null;
	private Configurator<Decisions> configurator = null;

	@Override
	protected Iterable<? extends ChangePropagationSpecification> getChangePropagationSpecifications() {
		ArrayList<ChangePropagationSpecification> retVal = new ArrayList<ChangePropagationSpecification>();
		retVal.add(new FamiliesToPersonsChangePropagationSpecification());
		retVal.add(new PersonsToFamiliesChangePropagationSpecification());
		return retVal;
	}

	public void prepare(final Path testProjectPath, final Path vsumPath) {
		this.prepareVirtualModelAndView(testProjectPath, vsumPath);
	}

	final void prepareVirtualModelAndView(final Path testProjectPath, final Path vsumPath) {
		final Iterable<ChangePropagationSpecification> changePropagationSpecifications = (Iterable<ChangePropagationSpecification>) this
				.getChangePropagationSpecifications();
		final TestUserInteraction userInteraction = new TestUserInteraction();
		HashSet<VitruvDomain> vd_set = new HashSet<VitruvDomain>();
		for (ChangePropagationSpecification c : changePropagationSpecifications) {
			vd_set.add(c.getSourceDomain());
			vd_set.add(c.getTargetDomain());
		}
		final VitruvDomainRepositoryImpl targetDomains = new VitruvDomainRepositoryImpl(vd_set);

		this.virtualModel = new VirtualModelBuilder().withStorageFolder(vsumPath)
				.withUserInteractorForResultProvider(new TestUserInteraction.ResultProvider(new TestUserInteraction()))
				.withDomainRepository(targetDomains)
				.withChangePropagationSpecifications(changePropagationSpecifications).buildAndInitialize();
		this.testView = new ChangePublishingTestView(testProjectPath, userInteraction, UriMode.FILE_URIS, virtualModel,
				targetDomains);
	}

	TestView generateTestView(final Path testProjectPath, final TestUserInteraction userInteraction,
			final VitruvDomainRepository targetDomains) {
		UriMode _uriMode = this.getUriMode();
		return new ChangePublishingTestView(testProjectPath, userInteraction, _uriMode, this.virtualModel,
				targetDomains);
	}

	public void performAndPropagateSourceEdit(Consumer<FamilyRegister> edit) {
		this.updateConfiguration();
		FamilyRegister fam1 = getFamiliyRegister();			
		if(fam1 == null) {
			System.out.println("No FamiliesModel before propagation.");
			final Consumer<Resource> _function = (Resource it) -> {
				EList<EObject> _contents = it.getContents();
				FamilyRegister _createFamilyRegister = FamiliesFactory.eINSTANCE.createFamilyRegister();
				_contents.add(_createFamilyRegister);
			};
			this.<Resource>propagate(this.resourceAt(FAMILIES_MODEL), _function);			
		}
//		FamilyRegister fam2 = getFamiliyRegister();
		this.propagate(this.<FamilyRegister>from(FamilyRegister.class, FAMILIES_MODEL), edit);
	}

	public void performAndPropagateTargetEdit(Consumer<PersonRegister> edit) {
		this.updateConfiguration();
		PersonRegister per = getPersonRegister();
		if(per == null) {
			System.out.println("No PersonsModel before propagation.");
			final Consumer<Resource> _function = (Resource it) -> {
				EList<EObject> _contents = it.getContents();
				PersonRegister _createPersonRegister = PersonsFactory.eINSTANCE.createPersonRegister();
				_contents.add(_createPersonRegister);
			};
			this.<Resource>propagate(this.resourceAt(PERSONS_MODEL), _function);
		}
//		PersonRegister per2 = getPersonRegister();
		this.propagate(this.<PersonRegister>from(PersonRegister.class, PERSONS_MODEL), edit);
	}

	public FamilyRegister getFamiliyRegister() {
		try {
			Object x = this.testView.resourceAt(FAMILIES_MODEL).getContents().get(0);
			return (FamilyRegister) x;
		} catch (Exception e) {
			return null;
		}
	}

	public PersonRegister getPersonRegister() {
		try {
			Object x = this.testView.resourceAt(PERSONS_MODEL).getContents().get(0);
			return (PersonRegister) x;
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
			return null;
		}
	}
	
	public void setConfigurator(Configurator<Decisions> configurator) {
		this.configurator  = configurator;
	}
	
	public void updateConfiguration() {
		try {
			this.preferAdult = this.configurator.decide(Decisions.PREFER_CREATING_PARENT_TO_CHILD);
			System.out.println(this.preferAdult ? "Prefer Parent" : "Prefer Child");
		} catch (Exception e) {
			this.preferAdult = null;
			System.out.println("No Parent-Child-Preference");
		}
		try {
			this.preferExistingFamily = this.configurator.decide(Decisions.PREFER_EXISTING_FAMILY_TO_NEW);
			System.out.println(this.preferExistingFamily ? "Prefer Existing Family" : "Prefer New Family");
		} catch (Exception e) {
			this.preferExistingFamily = null;
			System.out.println("No Existing-New-Family-Preference");
		}
		if (this.preferAdult != null) {
			this.parentChildResponseBuilder = this.getUserInteraction()
					.onMultipleChoiceSingleSelection(checkIfParentChildInteraction);
			this.parentChildResponseBuilder.respondWithChoiceAt(this.preferAdult ? 0 : 1);
		}
		if (this.preferExistingFamily != null) {
			this.existingOrNewFamilyResponseBuilder = this.getUserInteraction()
					.onMultipleChoiceSingleSelection(checkIfExistingOrNewFamilyInteraction);
			this.existingOrNewFamilyResponseBuilder.respondWithChoiceAt(this.preferExistingFamily ? 1 : 0);
		}
	}

	private Function1<? super MultipleChoiceInteractionDescription, ? extends Boolean> checkIfParentChildInteraction = new Function1<MultipleChoiceInteractionDescription, Boolean>() {
		private String parentOrChildTitle = "Parent or Child";
		@Override
		public Boolean apply(MultipleChoiceInteractionDescription multipleChoiceInteractionDescription) {
			return multipleChoiceInteractionDescription.getTitle().equals(this.parentOrChildTitle);
		}
	};
	
	private Function1<? super MultipleChoiceInteractionDescription, ? extends Boolean> checkIfExistingOrNewFamilyInteraction = new Function1<MultipleChoiceInteractionDescription, Boolean>() {
		private String existingOrNewFamilyTitle = "Existing or New Family";
		@Override
		public Boolean apply(MultipleChoiceInteractionDescription multipleChoiceInteractionDescription) {
			return multipleChoiceInteractionDescription.getTitle().equals(this.existingOrNewFamilyTitle);
		}
	};

	public void endTest() {
		this.getUserInteraction().clearResponses();
		this.configurator = null;
		this.preferAdult = null;
		this.preferExistingFamily = null;
	}
}
