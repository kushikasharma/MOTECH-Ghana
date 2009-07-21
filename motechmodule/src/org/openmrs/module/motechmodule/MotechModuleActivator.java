/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.motechmodule;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNameTag;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class MotechModuleActivator implements Activator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	public void startup() {
		log.info("Starting Motech Module");
		
		Context.authenticate("admin", "test");
		
		log.info("Verifying Person Attributes Exist");
		createPersonAttributeType("Phone Number", "A person's phone number.", "java.lang.String");
		createPersonAttributeType("NHIS Number", "A person's NHIS number.", "java.lang.String");
		createPersonAttributeType("Language", "A person's language preference.", "java.lang.String");
		
		log.info("Verifying Patient Identifier Exist");
		createPatientIdentifierType("Ghana Clinic Id", "Patient Id for Ghana Clinics.");
		
		log.info("Verifying Default Location Exists");
		createLocation("Default Ghana Clinic", "Default Ghana Clinic Location");
		
		log.info("Verifying Encounter Types Exist");
		createEncounterType("MATERNALVISIT", "Ghana Maternal Visit");
		createEncounterType("IMMUNIZVISIT", "Ghana Immunization Visit");
		createEncounterType("GENERALVISIT", "Ghana General Visit");
		
		log.info("Verifying Concepts Exist");
		createConcept("PREGNANCY VISIT NUMBER", "Visit Number for Pregnancy", "Misc", "Numeric");
		createConcept("INTERMITTENT PREVENTATIVE TREATMENT", "Treatment for Malaria", "Drug", "N/A");
		createConcept("INSECTICIDE-TREATED NET USAGE",
		    "Question on encounter form: \"Does the patient use insecticide-treated nets?\"", "Question", "Boolean");
		createConcept("PENTA VACCINATION", "Vaccination booster for infants.", "Drug", "N/A");
		createConcept("CEREBRO-SPINAL MENINGITIS VACCINATION", "Vaccination against Cerebro-Spinal Meningitis.", "Drug",
		    "N/A");
		createConcept("VITAMIN A", "Supplement for Vitamin A.", "Drug", "N/A");
		createConcept(
		    "PRE PREVENTING MATERNAL TO CHILD TRANSMISSION",
		    "Question on encounter form: \"Did the patient receive Pre Counseling for Preventing Mother-to-Child Transmission (PMTCT) of HIV\"",
		    "Question", "Boolean");
		createConcept(
		    "TEST PREVENTING MATERNAL TO CHILD TRANSMISSION",
		    "Question on encounter form: \"Did the patient receive Testing for Preventing Mother-to-Child Transmission (PMTCT) of HIV\"",
		    "Question", "Boolean");
		createConcept(
		    "POST PREVENTING MATERNAL TO CHILD TRANSMISSION",
		    "Question on encounter form: \"Did the patient receive Post Counseling for Preventing Mother-to-Child Transmission (PMTCT) of HIV\"",
		    "Question", "Boolean");
		createConcept("HEMOGLOBIN AT 36 WEEKS", "Hemoglobin level at 36 weeks of Pregnancy", "Test", "Numeric");
		
		log.info("Verifying Concepts Exist as Answers");
		// TODO: Add IPT to proper Concept as an Answer, not an immunization
		addConceptAnswers("IMMUNIZATIONS ORDERED", new String[] { "TETANUS BOOSTER", "YELLOW FEVER VACCINATION",
		        "INTERMITTENT PREVENTATIVE TREATMENT", "PENTA VACCINATION", "CEREBRO-SPINAL MENINGITIS VACCINATION" });
	}
	
	private void createPersonAttributeType(String name, String description, String format) {
		PersonAttributeType attrType = Context.getPersonService().getPersonAttributeTypeByName(name);
		if (attrType == null) {
			log.info(name + " PersonAttributeType Does Not Exist - Creating");
			attrType = new PersonAttributeType();
			attrType.setName(name);
			attrType.setDescription(description);
			attrType.setFormat(format);
			Context.getPersonService().savePersonAttributeType(attrType);
		}
	}
	
	private void createPatientIdentifierType(String name, String description) {
		PatientIdentifierType idType = Context.getPatientService().getPatientIdentifierTypeByName(name);
		if (idType == null) {
			log.info(name + " PatientIdentifierType Does Not Exist - Creating");
			idType = new PatientIdentifierType();
			idType.setName(name);
			idType.setDescription(description);
			Context.getPatientService().savePatientIdentifierType(idType);
		}
	}
	
	private void createLocation(String name, String description) {
		Location location = Context.getLocationService().getLocation(name);
		if (location == null) {
			log.info(name + " Location Does Not Exist - Creating");
			location = new Location();
			location.setName(name);
			location.setDescription(description);
			Context.getLocationService().saveLocation(location);
		}
	}
	
	private void createEncounterType(String name, String description) {
		EncounterType encType = Context.getEncounterService().getEncounterType(name);
		if (encType == null) {
			log.info(name + " EncounterType Does Not Exist - Creating");
			encType = new EncounterType();
			encType.setName(name);
			encType.setDescription(description);
			Context.getEncounterService().saveEncounterType(encType);
		}
	}
	
	private Concept createConcept(String name, String description, String className, String dataTypeName) {
		// Default "en" Locale matching other existing concepts
		Locale defaultLocale = Locale.ENGLISH;
		Concept concept = Context.getConceptService().getConcept(name);
		if (concept == null) {
			log.info(name + " Concept Does Not Exist - Creating");
			concept = new Concept();
			ConceptName conceptName = new ConceptName(name, defaultLocale);
			conceptName.addTag(ConceptNameTag.PREFERRED);
			// AddTag is workaround since the following results in "preferred_en" instead of "preferred"
			// itn.setPreferredName(defaultLocale, conceptName) 
			concept.addName(conceptName);
			concept.addDescription(new ConceptDescription(description, defaultLocale));
			concept.setConceptClass(Context.getConceptService().getConceptClassByName(className));
			concept.setDatatype(Context.getConceptService().getConceptDatatypeByName(dataTypeName));
			concept = Context.getConceptService().saveConcept(concept);
		} else {
			log.info(name + " Concept Exists");
		}
		return concept;
	}
	
	private void addConceptAnswers(String conceptName, String[] answerNames) {
		Concept concept = Context.getConceptService().getConcept(conceptName);
		Set<Integer> currentAnswerIds = new HashSet<Integer>();
		for (ConceptAnswer answer : concept.getAnswers()) {
			currentAnswerIds.add(answer.getAnswerConcept().getId());
		}
		boolean changed = false;
		for (String answerName : answerNames) {
			Concept answer = Context.getConceptService().getConcept(answerName);
			if (!currentAnswerIds.contains(answer.getId())) {
				log.info("Adding Concept Answer " + answerName + " to " + conceptName);
				changed = true;
				concept.addAnswer(new ConceptAnswer(answer));
			}
		}
		if (changed) {
			Context.getConceptService().saveConcept(concept);
		}
	}
		
	/**
	 * @see org.openmrs.module.Activator#shutdown()
	 */
	public void shutdown() {
		log.info("Shutting down Motech Module");
	}
	
}