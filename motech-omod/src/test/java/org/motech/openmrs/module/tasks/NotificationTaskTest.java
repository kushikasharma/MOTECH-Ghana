package org.motech.openmrs.module.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.motech.messaging.impl.MessageSchedulerImpl;
import org.motech.model.Message;
import org.motech.model.MessageDefinition;
import org.motech.model.MessageStatus;
import org.motech.model.MessageType;
import org.motech.model.ScheduledMessage;
import org.motech.openmrs.module.MotechModuleActivator;
import org.motech.openmrs.module.MotechService;
import org.motech.svc.RegistrarBean;
import org.motech.util.MotechConstants;
import org.motechproject.ws.ContactNumberType;
import org.motechproject.ws.DeliveryTime;
import org.motechproject.ws.Gender;
import org.motechproject.ws.MediaType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;

public class NotificationTaskTest extends BaseModuleContextSensitiveTest {

	static MotechModuleActivator activator;

	@BeforeClass
	public static void setUpClass() throws Exception {
		activator = new MotechModuleActivator(false);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		activator = null;
	}

	@Before
	public void setup() throws Exception {
		// Perform same steps as BaseSetup (initializeInMemoryDatabase,
		// executeDataSet, authenticate), except load custom XML dataset
		initializeInMemoryDatabase();

		// Created from org.openmrs.test.CreateInitialDataSet
		// using 1.4.4-createdb-from-scratch-with-demo-data.sql
		// Removed all empty short_name="" from concepts
		// Added missing description to relationship_type
		// Removed all patients and related patient/person info (id 2-500)
		executeDataSet("initial-openmrs-dataset.xml");

		authenticate();

		activator.startup();
	}

	@After
	public void tearDown() throws Exception {
		activator.shutdown();
	}

	@Test
	@SkipBaseSetup
	public void testSingleNotify() {

		try {
			Context.openSession();
			Context.authenticate("admin", "test");

			RegistrarBean regService = Context.getService(MotechService.class)
					.getRegistrarBean();

			regService.registerNurse("nursename", "nurseId",
					"nursePhoneNumber", MotechConstants.LOCATION_GHANA);

			assertEquals(2, Context.getUserService().getAllUsers().size());
			regService.registerPatient("nursePhoneNumber", "serialId",
					"patientname", "community", "location", new Date(),
					Gender.FEMALE, 1, "patientphoneNumber",
					ContactNumberType.PERSONAL, "language", MediaType.TEXT,
					DeliveryTime.ANYTIME, new String[] {});

			assertEquals(3, Context.getPatientService().getAllPatients().size());

			List<Patient> patients = Context
					.getPatientService()
					.getPatients(
							"patientname",
							"serialId",
							new ArrayList<PatientIdentifierType>(
									Arrays
											.asList(Context
													.getPatientService()
													.getPatientIdentifierTypeByName(
															MotechConstants.PATIENT_IDENTIFIER_GHANA_CLINIC_ID))),
							true);

			assertEquals(1, patients.size());

			Patient patient = patients.get(0);

			String messageKey = "Test Definition";
			MessageDefinition messageDefinition = new MessageDefinition(
					messageKey, 2L, MessageType.INFORMATIONAL);
			messageDefinition = Context.getService(MotechService.class)
					.saveMessageDefinition(messageDefinition);

			// Schedule message 5 seconds in future
			Date scheduledMessageDate = new Date(
					System.currentTimeMillis() + 5 * 1000);
			MessageSchedulerImpl messageScheduler = new MessageSchedulerImpl();
			messageScheduler.setRegistrarBean(regService);
			messageScheduler.scheduleMessage(messageKey, "Test Group", patient
					.getPersonId(), scheduledMessageDate);
		} finally {
			Context.closeSession();
		}

		NotificationTask task = new NotificationTask();
		TaskDefinition taskDef = new TaskDefinition();
		taskDef.setRepeatInterval(30L);
		task.initialize(taskDef);
		task.execute();

		try {
			Context.openSession();

			List<ScheduledMessage> scheduledMessages = Context.getService(
					MotechService.class).getAllScheduledMessages();

			assertEquals(1, scheduledMessages.size());

			ScheduledMessage retrievedScheduledMessage = scheduledMessages
					.get(0);
			List<Message> messageAttempts = retrievedScheduledMessage
					.getMessageAttempts();

			assertEquals(1, messageAttempts.size());

			Message message = messageAttempts.get(0);

			assertNotNull("Message attempt date is null", message
					.getAttemptDate());
			assertEquals(MessageStatus.ATTEMPT_PENDING, message
					.getAttemptStatus());

			assertEquals(1, Context.getService(MotechService.class)
					.getAllLogs().size());
		} finally {
			Context.closeSession();
		}
	}

}