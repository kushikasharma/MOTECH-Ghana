package org.motech.openmrs.module.web.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.motech.model.HIVStatus;
import org.motech.model.WhoRegistered;
import org.motech.util.GenderTypeConverter;
import org.motech.util.MotechConstants;
import org.motechproject.ws.ContactNumberType;
import org.motechproject.ws.MediaType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;

public class WebModelConverterImpl implements WebModelConverter {

	private final Log log = LogFactory.getLog(WebModelConverterImpl.class);

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE MMM d HH:mm:ss z yyyy");

	public void patientToWeb(Patient patient, WebPatient webPatient) {

		personToWeb(patient, webPatient);

		PatientIdentifier patientId = patient
				.getPatientIdentifier(MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID);
		if (patientId != null) {
			webPatient.setRegNumberGHS(patientId.getIdentifier());
		}

		PersonAttribute nhisExpDateAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_NHIS_EXP_DATE);
		if (nhisExpDateAttr != null) {
			Date nhisExpDate = null;
			String nhisExpDateString = nhisExpDateAttr.getValue();
			try {
				nhisExpDate = dateFormat.parse(nhisExpDateString);
			} catch (ParseException e) {
				log.error("Cannot parse NHIS Expiration Date: "
						+ nhisExpDateString, e);
			}
			webPatient.setNhisExpDate(nhisExpDate);
		}

		PersonAttribute registeredGHSAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_GHS_REGISTERED);
		if (registeredGHSAttr != null) {
			webPatient.setRegisteredGHS(Boolean.valueOf(registeredGHSAttr
					.getValue()));
		}

		PersonAttribute insuredAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_INSURED);
		if (insuredAttr != null) {
			webPatient.setInsured(Boolean.valueOf(insuredAttr.getValue()));
		}

		PersonAttribute nhisAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_NHIS_NUMBER);
		if (nhisAttr != null) {
			webPatient.setNhis(nhisAttr.getValue());
		}

		PersonAttribute clinicAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_HEALTH_CENTER);
		if (clinicAttr != null) {
			webPatient.setClinic(Integer.valueOf(clinicAttr.getValue()));
		}

		// TODO: populate dueDate
		// TODO: populate dueDateConfirmed
		// TODO: populate gravida
		// TODO: populate parity

		PersonAttribute hivAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_HIV_STATUS);
		if (hivAttr != null) {
			webPatient.setHivStatus(HIVStatus.valueOf(hivAttr.getValue()));
		}

		// TODO: populate registerPregProgram

		PersonAttribute primaryPhoneAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_NUMBER);
		if (primaryPhoneAttr != null) {
			webPatient.setPrimaryPhone(primaryPhoneAttr.getValue());
		}

		PersonAttribute primaryPhoneTypeAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_PRIMARY_PHONE_TYPE);
		if (primaryPhoneTypeAttr != null) {
			webPatient.setPrimaryPhoneType(ContactNumberType
					.valueOf(primaryPhoneTypeAttr.getValue()));
		}

		PersonAttribute secondaryPhoneAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_NUMBER);
		if (secondaryPhoneAttr != null) {
			webPatient.setSecondaryPhone(secondaryPhoneAttr.getValue());
		}

		PersonAttribute secondaryPhoneTypeAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_SECONDARY_PHONE_TYPE);
		if (secondaryPhoneTypeAttr != null) {
			webPatient.setSecondaryPhoneType(ContactNumberType
					.valueOf(secondaryPhoneTypeAttr.getValue()));
		}

		PersonAttribute mediaTypeInfoAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_INFORMATIONAL);
		if (mediaTypeInfoAttr != null) {
			webPatient.setMediaTypeInfo(MediaType.valueOf(mediaTypeInfoAttr
					.getValue()));
		}

		PersonAttribute mediaTypeReminderAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE_REMINDER);
		if (mediaTypeReminderAttr != null) {
			webPatient.setMediaTypeReminder(MediaType
					.valueOf(mediaTypeReminderAttr.getValue()));
		}

		PersonAttribute languageVoiceAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_VOICE);
		if (languageVoiceAttr != null) {
			webPatient.setLanguageVoice(languageVoiceAttr.getValue());
		}

		PersonAttribute languageTextAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_LANGUAGE_TEXT);
		if (languageTextAttr != null) {
			webPatient.setLanguageText(languageTextAttr.getValue());
		}

		PersonAttribute whoRegisteredAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_WHO_REGISTERED);
		if (whoRegisteredAttr != null) {
			webPatient.setWhoRegistered(WhoRegistered.valueOf(whoRegisteredAttr
					.getValue()));
		}

		PersonAttribute religionAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_RELIGION);
		if (religionAttr != null) {
			webPatient.setReligion(religionAttr.getValue());
		}

		PersonAttribute occupationAttr = patient
				.getAttribute(MotechConstants.PERSON_ATTRIBUTE_OCCUPATION);
		if (occupationAttr != null) {
			webPatient.setOccupation(occupationAttr.getValue());
		}
	}

	public void personToWeb(Person person, WebPatient webPatient) {

		webPatient.setId(person.getPersonId());
		webPatient.setFirstName(person.getGivenName());
		webPatient.setLastName(person.getFamilyName());
		webPatient.setPrefName(person.getMiddleName());
		webPatient.setBirthDate(person.getBirthdate());
		webPatient.setBirthDateEst(person.getBirthdateEstimated());
		webPatient.setSex(GenderTypeConverter
				.valueOfOpenMRS(person.getGender()));

		PersonAddress patientAddress = person.getPersonAddress();
		if (patientAddress != null) {
			webPatient.setRegion(patientAddress.getRegion());
			webPatient.setDistrict(patientAddress.getCountyDistrict());
			webPatient.setCommunity(patientAddress.getCityVillage());
			webPatient.setAddress(patientAddress.getAddress1());
		}
	}

}