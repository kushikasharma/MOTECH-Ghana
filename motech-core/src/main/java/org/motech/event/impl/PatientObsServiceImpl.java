package org.motech.event.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.motech.event.PatientObsService;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;

public class PatientObsServiceImpl implements PatientObsService {

	private List<Obs> getMatchingObs(Patient patient, String conceptName,
			String conceptValue) {

		List<Concept> questions = null;
		if (conceptName != null) {
			Concept concept = Context.getConceptService().getConcept(
					conceptName);
			questions = new ArrayList<Concept>();
			questions.add(concept);
		}

		List<Concept> answers = null;
		if (conceptValue != null) {
			Concept conceptAnswer = Context.getConceptService().getConcept(
					conceptValue);
			answers = new ArrayList<Concept>();
			answers.add(conceptAnswer);
		}

		List<Person> whom = new ArrayList<Person>();
		whom.add((Person) patient);

		// patients, encounters, questions, answers, persontype, locations,
		// sort, max returned, group id, from date, to date, include voided
		List<Obs> obsList = Context.getObsService().getObservations(whom, null,
				questions, answers, null, null, null, null, null, null, null,
				false);

		return obsList;
	}

	public int getNumberOfObs(Patient patient, String conceptName,
			String conceptValue) {

		List<Obs> obsList = getMatchingObs(patient, conceptName, conceptValue);

		return obsList.size();
	}

	public Date getLastObsDate(Patient patient, String conceptName,
			String conceptValue) {

		Date latestObsDate = null;

		// List default sorted by Obs datetime
		List<Obs> obsList = getMatchingObs(patient, conceptName, conceptValue);

		if (obsList.size() > 0) {
			latestObsDate = obsList.get(0).getObsDatetime();
		}
		return latestObsDate;
	}

}