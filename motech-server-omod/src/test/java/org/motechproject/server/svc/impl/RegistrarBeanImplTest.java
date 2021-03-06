/**
 * MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT
 *
 * Copyright (c) 2010-11 The Trustees of Columbia University in the City of
 * New York and Grameen Foundation USA.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Grameen Foundation USA, Columbia University, or
 * their respective contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA, COLUMBIA UNIVERSITY
 * AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION
 * USA, COLUMBIA UNIVERSITY OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.motechproject.server.svc.impl;

import junit.framework.TestCase;
import org.easymock.Capture;
import org.motechproject.server.model.*;
import org.motechproject.server.omod.ContextService;
import org.motechproject.server.omod.MotechService;
import org.motechproject.server.omod.web.model.KassenaNankana;
import org.motechproject.server.svc.RCTService;
import org.motechproject.server.util.MotechConstants;
import org.motechproject.ws.Care;
import org.motechproject.ws.CareMessageGroupingStrategy;
import org.motechproject.ws.DayOfWeek;
import org.motechproject.ws.MediaType;
import org.motechproject.ws.mobile.MessageService;
import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.easymock.EasyMock.*;

public class RegistrarBeanImplTest extends TestCase {

    RegistrarBeanImpl registrarBean;

    ContextService contextService;
    AdministrationService adminService;
    MotechService motechService;
    PersonService personService;
    RCTService rctService;
    MessageService mobileService;

    @Override
    protected void setUp() throws Exception {
        contextService = createMock(ContextService.class);
        adminService = createMock(AdministrationService.class);
        motechService = createMock(MotechService.class);
        personService = createMock(PersonService.class);
        rctService = createMock(RCTService.class);
        mobileService = createMock(MessageService.class);

        registrarBean = new RegistrarBeanImpl();
        registrarBean.setContextService(contextService);
        registrarBean.setAdministrationService(adminService);
        registrarBean.setPersonService(personService);
        registrarBean.setRctService(rctService);
        registrarBean.setMobileService(mobileService);
    }

    @Override
    protected void tearDown() throws Exception {
        registrarBean = null;
        contextService = null;
        adminService = null;
        motechService = null;
        personService = null;
    }

    public void testFindPersonPreferredMessageDate() {
        DayOfWeek day = DayOfWeek.MONDAY;
        String timeAsString = "09:00";
        Date messageDate = new Date();
        Person person = createPersonWithDateTimePreferences(day, timeAsString);
        Date preferredDate = registrarBean.findPreferredMessageDate(person, messageDate, messageDate, true);
        Calendar messageCalendar = getCalendar(messageDate);
        Calendar preferenceCalendar = getCalendar(preferredDate);

        assertTrue(preferenceCalendar.after(messageCalendar));
        assertEquals(day.getCalendarValue(), preferenceCalendar.get(Calendar.DAY_OF_WEEK));
        assertEquals(9, preferenceCalendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, preferenceCalendar.get(Calendar.MINUTE));
        assertEquals(0, preferenceCalendar.get(Calendar.SECOND));
    }

    private Calendar getCalendar(Date messageDate) {
        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(messageDate);
        return messageCalendar;
    }

    private Person createPersonWithDateTimePreferences(DayOfWeek day, String timeAsString) {
        Person person = new Person(1);
        PersonAttributeType dayType = new PersonAttributeType(1);
        dayType.setName(MotechConstants.PERSON_ATTRIBUTE_DELIVERY_DAY);
        PersonAttributeType timeType = new PersonAttributeType(2);
        timeType.setName(MotechConstants.PERSON_ATTRIBUTE_DELIVERY_TIME);
        person.addAttribute(new PersonAttribute(dayType, day.toString()));
        person.addAttribute(new PersonAttribute(timeType, timeAsString));
        return person;
    }

    public void testDetermineDefaultPrefDate() {
        DayOfWeek day = DayOfWeek.MONDAY;
        String timeAsString = "09:00";
        Date messageDate = new Date();

        Person person = new Person(1);

        expect(adminService.getGlobalProperty(MotechConstants.GLOBAL_PROPERTY_DAY_OF_WEEK)).andReturn(day.toString());
        expect(adminService.getGlobalProperty(MotechConstants.GLOBAL_PROPERTY_TIME_OF_DAY)).andReturn(timeAsString);
        replay(contextService, adminService);

        Date prefDate = registrarBean.findPreferredMessageDate(person, messageDate, messageDate, true);

        verify(contextService, adminService);

        Calendar messageCalendar = getCalendar(messageDate);
        Calendar preferredCalendar = getCalendar(prefDate);

        assertTrue(preferredCalendar.after(messageCalendar));
        assertEquals(day.getCalendarValue(), preferredCalendar.get(Calendar.DAY_OF_WEEK));
        assertEquals(9, preferredCalendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, preferredCalendar.get(Calendar.MINUTE));
        assertEquals(0, preferredCalendar.get(Calendar.SECOND));
    }

    public void testFindNoPreferenceDate() {
        Date messageDate = new Date();
        Person person = new Person(1);

        expect(adminService.getGlobalProperty(MotechConstants.GLOBAL_PROPERTY_DAY_OF_WEEK)).andReturn(null);
        expect(adminService.getGlobalProperty(MotechConstants.GLOBAL_PROPERTY_TIME_OF_DAY)).andReturn(null);
        replay(contextService, adminService);

        Date prefDate = registrarBean.findPreferredMessageDate(person, messageDate, messageDate, true);

        verify(contextService, adminService);

        Calendar messageCal = getCalendar(messageDate);
        Calendar prefCal = getCalendar(prefDate);

        assertEquals(messageCal.get(Calendar.YEAR), prefCal.get(Calendar.YEAR));
        assertEquals(messageCal.get(Calendar.MONTH), prefCal.get(Calendar.MONTH));
        assertEquals(messageCal.get(Calendar.DATE), prefCal.get(Calendar.DATE));
        assertEquals(messageCal.get(Calendar.HOUR_OF_DAY), prefCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(messageCal.get(Calendar.MINUTE), prefCal.get(Calendar.MINUTE));
        assertEquals(0, prefCal.get(Calendar.SECOND));
    }

    public void testAdjustDateTime() {
        Calendar calendar = getCalendarWithTime(14, 13, 54);
        Date messageDate = calendar.getTime();

        int hour = 15;
        int minute = 37;
        int second = 0;

        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.set(Calendar.YEAR, 1986);
        timeCalendar.set(Calendar.MONTH, 6);
        timeCalendar.set(Calendar.DAY_OF_MONTH, 4);
        timeCalendar.set(Calendar.HOUR_OF_DAY, hour);
        timeCalendar.set(Calendar.MINUTE, minute);
        timeCalendar.set(Calendar.SECOND, 34);

        Date prefDate = registrarBean.adjustTime(messageDate, timeCalendar.getTime());

        Calendar prefCal = getCalendar(prefDate);

        assertEquals(calendar.get(Calendar.YEAR), prefCal.get(Calendar.YEAR));
        assertEquals(calendar.get(Calendar.MONTH), prefCal.get(Calendar.MONTH));
        assertEquals(calendar.get(Calendar.DATE), prefCal.get(Calendar.DATE));
        assertFalse("Hour not updated", calendar.get(Calendar.HOUR_OF_DAY) == prefCal.get(Calendar.HOUR_OF_DAY));
        assertEquals(hour, prefCal.get(Calendar.HOUR_OF_DAY));
        assertFalse("Minute not updated", calendar.get(Calendar.MINUTE) == prefCal.get(Calendar.MINUTE));
        assertEquals(minute, prefCal.get(Calendar.MINUTE));
        assertFalse("Second not updated", calendar.get(Calendar.SECOND) == prefCal.get(Calendar.SECOND));
        assertEquals(second, prefCal.get(Calendar.SECOND));
    }

    public void testAdjustDateBlackoutInTheMorning() {
        Calendar calendar = getCalendarWithTime(2, 13, 54);
        Date morningMessageTime = calendar.getTime();

        expect(contextService.getMotechService()).andReturn(motechService);
        expect(motechService.getBlackoutSettings()).andReturn(new Blackout(Time.valueOf("22:00:00"), Time.valueOf("06:00:00")));
        replay(contextService, adminService, motechService);

        Date preferredDate = registrarBean.adjustDateForBlackout(morningMessageTime);
        verify(contextService, adminService, motechService);

        Calendar preferredCalendar = getCalendar(preferredDate);

        assertFalse("Hour not updated", calendar.get(Calendar.HOUR_OF_DAY) == preferredCalendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(6, preferredCalendar.get(Calendar.HOUR_OF_DAY));
        assertFalse("Minute not updated", calendar.get(Calendar.MINUTE) == preferredCalendar.get(Calendar.MINUTE));
        assertEquals(0, preferredCalendar.get(Calendar.MINUTE));
        assertFalse("Second not updated", calendar.get(Calendar.SECOND) == preferredCalendar.get(Calendar.SECOND));
        assertEquals(0, preferredCalendar.get(Calendar.SECOND));
    }


    public void testAdjustDateBlackoutInTheNight() {
        Calendar calendar = getCalendarWithTime(22, 13, 54);
        Date eveningMessageTime = calendar.getTime();
        Blackout blackout = new Blackout(Time.valueOf("22:00:00"), Time.valueOf("06:00:00"));

        expect(contextService.getMotechService()).andReturn(motechService);
        expect(motechService.getBlackoutSettings()).andReturn(blackout);
        replay(contextService, adminService, motechService);

        Date prefDate = registrarBean.adjustDateForBlackout(eveningMessageTime);

        verify(contextService, adminService, motechService);
        Calendar preferredCalendar = getCalendar(prefDate);
        assertFalse("Hour not updated", calendar.get(Calendar.HOUR_OF_DAY) == preferredCalendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(6, preferredCalendar.get(Calendar.HOUR_OF_DAY));
        assertFalse("Minute not updated", calendar.get(Calendar.MINUTE) == preferredCalendar.get(Calendar.MINUTE));
        assertEquals(0, preferredCalendar.get(Calendar.MINUTE));
        assertFalse("Second not updated", calendar.get(Calendar.SECOND) == preferredCalendar.get(Calendar.SECOND));
        assertEquals(0, preferredCalendar.get(Calendar.SECOND));
    }

    private Calendar getCalendarWithTime(int hourOfTheDay, int minutes, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfTheDay);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);
        return calendar;
    }

    public void testShouldFindOutIfMessageTimeIsDuringBlackoutPeriod() {
        Calendar calendar = getCalendarWithTime(23, 13, 54);
        Date morningMessageTime = calendar.getTime();
        calendar = getCalendarWithTime(3, 13, 54);
        Date nightMessageTime = calendar.getTime();
        calendar = getCalendarWithTime(19, 30, 30);
        Date eveningMessageTime = calendar.getTime();

        Blackout blackout = new Blackout(Time.valueOf("23:00:00"), Time.valueOf("06:00:00"));

        expect(contextService.getMotechService()).andReturn(motechService).times(3);
        expect(motechService.getBlackoutSettings()).andReturn(blackout).times(3);
        replay(contextService, adminService, motechService);

        assertTrue(registrarBean.isMessageTimeWithinBlackoutPeriod(morningMessageTime));
        assertTrue(registrarBean.isMessageTimeWithinBlackoutPeriod(nightMessageTime));
        assertFalse(registrarBean.isMessageTimeWithinBlackoutPeriod(eveningMessageTime));
        verify(contextService, adminService, motechService);

    }

    public void testSchedulingInfoMessageWithExistingScheduled() {
        String messageKey = "message";
        String messageKeyA = "message.a";
        String messageKeyB = "message.b";
        String messageKeyC = "message.c";

        Date currentDate = new Date();
        Date messageDate = new Date(System.currentTimeMillis() + 5 * 1000);
        boolean userPreferenceBased = true;

        Integer personId = 1;
        Person person = new Person(personId);
        PersonAttributeType mediaTypeAttribute = new PersonAttributeType();
        mediaTypeAttribute.setName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
        person.addAttribute(new PersonAttribute(mediaTypeAttribute, MediaType.VOICE.toString()));

        MessageProgramEnrollment enrollment = new MessageProgramEnrollment();
        enrollment.setPersonId(personId);

        MessageDefinition messageDef = new MessageDefinition(messageKey, 1L, MessageType.INFORMATIONAL);
        List<Message> messagesToRemove = new ArrayList<Message>();

        List<ScheduledMessage> existingMessages = new ArrayList<ScheduledMessage>();
        ScheduledMessage scheduledMessage = new ScheduledMessage();
        existingMessages.add(scheduledMessage);

        Capture<ScheduledMessage> capturedScheduledMessage = new Capture<ScheduledMessage>();

        expect(contextService.getMotechService()).andReturn(motechService).atLeastOnce();
        expect(personService.getPerson(personId)).andReturn(person);
        expect(motechService.getMessageDefinition(messageKey)).andReturn(messageDef);
        expect(motechService.getMessages(personId, enrollment, messageDef,messageDate, MessageStatus.SHOULD_ATTEMPT)).
                andReturn(messagesToRemove);
        expect(motechService.getScheduledMessages(personId, messageDef,enrollment, messageDate)).andReturn(existingMessages);
        expect(motechService.saveScheduledMessage(capture(capturedScheduledMessage))).andReturn(new ScheduledMessage());

        replay(contextService, adminService, motechService, personService);

        registrarBean.scheduleInfoMessages(messageKey, messageKeyA, messageKeyB,
                messageKeyC, enrollment, messageDate, userPreferenceBased,
                currentDate);

        verify(contextService, adminService, motechService, personService);

        ScheduledMessage scheduledMsg = capturedScheduledMessage.getValue();
        List<Message> attempts = scheduledMsg.getMessageAttempts();
        assertEquals(1, attempts.size());
        Message message = attempts.get(0);
        assertEquals(MessageStatus.SHOULD_ATTEMPT, message.getAttemptStatus());
        assertEquals(messageDate, message.getAttemptDate());
    }

    public void testSchedulingInfoMessageWithExistingMatchingMessage() {
        String messageKey = "message", messageKeyA = "message.a", messageKeyB = "message.b", messageKeyC = "message.c";
        Date currentDate = new Date();
        Date messageDate = new Date(System.currentTimeMillis() + 5 * 1000);
        boolean userPreferenceBased = true;

        Integer personId = 1;
        Person person = new Person(personId);
        PersonAttributeType mediaTypeAttr = new PersonAttributeType();
        mediaTypeAttr.setName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
        person.addAttribute(new PersonAttribute(mediaTypeAttr, MediaType.VOICE
                .toString()));

        MessageProgramEnrollment enrollment = new MessageProgramEnrollment();
        enrollment.setPersonId(personId);

        MessageDefinition messageDef = new MessageDefinition(messageKey, 1L,
                MessageType.INFORMATIONAL);

        List<Message> messagesToRemove = new ArrayList<Message>();

        List<ScheduledMessage> existingMessages = new ArrayList<ScheduledMessage>();
        ScheduledMessage schedMessage = new ScheduledMessage();
        Message msg = messageDef.createMessage(schedMessage);
        msg.setAttemptDate(new Timestamp(messageDate.getTime()));
        schedMessage.getMessageAttempts().add(msg);
        existingMessages.add(schedMessage);

        expect(contextService.getMotechService()).andReturn(motechService)
                .atLeastOnce();
        expect(personService.getPerson(personId)).andReturn(person);
        expect(motechService.getMessageDefinition(messageKey)).andReturn(
                messageDef);
        expect(
                motechService.getMessages(personId, enrollment, messageDef,
                        messageDate, MessageStatus.SHOULD_ATTEMPT)).andReturn(
                messagesToRemove);
        expect(
                motechService.getScheduledMessages(personId, messageDef,
                        enrollment, messageDate)).andReturn(existingMessages);

        replay(contextService, adminService, motechService, personService);

        registrarBean.scheduleInfoMessages(messageKey, messageKeyA, messageKeyB,
                messageKeyC, enrollment, messageDate, userPreferenceBased,
                currentDate);

        verify(contextService, adminService, motechService, personService);
    }

    public void testSchedulingInfoMessageWithExistingPendingMessage() {
        String messageKey = "message", messageKeyA = "message.a", messageKeyB = "message.b", messageKeyC = "message.c";
        Date currentDate = new Date();
        Date messageDate = new Date(System.currentTimeMillis() + 5 * 1000);
        boolean userPreferenceBased = true;

        Integer personId = 1;
        Person person = new Person(personId);
        PersonAttributeType mediaTypeAttr = new PersonAttributeType();
        mediaTypeAttr.setName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
        person.addAttribute(new PersonAttribute(mediaTypeAttr, MediaType.VOICE
                .toString()));

        MessageProgramEnrollment enrollment = new MessageProgramEnrollment();
        enrollment.setPersonId(personId);

        MessageDefinition messageDef = new MessageDefinition(messageKey, 1L,
                MessageType.INFORMATIONAL);

        List<Message> messagesToRemove = new ArrayList<Message>();

        List<ScheduledMessage> existingMessages = new ArrayList<ScheduledMessage>();
        ScheduledMessage schedMessage = new ScheduledMessage();
        Message msg = messageDef.createMessage(schedMessage);
        msg.setAttemptDate(new Timestamp(messageDate.getTime()));
        msg.setAttemptStatus(MessageStatus.ATTEMPT_PENDING);
        schedMessage.getMessageAttempts().add(msg);
        existingMessages.add(schedMessage);

        expect(contextService.getMotechService()).andReturn(motechService)
                .atLeastOnce();
        expect(personService.getPerson(personId)).andReturn(person);
        expect(motechService.getMessageDefinition(messageKey)).andReturn(
                messageDef);
        expect(
                motechService.getMessages(personId, enrollment, messageDef,
                        messageDate, MessageStatus.SHOULD_ATTEMPT)).andReturn(
                messagesToRemove);
        expect(
                motechService.getScheduledMessages(personId, messageDef,
                        enrollment, messageDate)).andReturn(existingMessages);

        replay(contextService, adminService, motechService, personService);

        registrarBean.scheduleInfoMessages(messageKey, messageKeyA, messageKeyB,
                messageKeyC, enrollment, messageDate, userPreferenceBased,
                currentDate);

        verify(contextService, adminService, motechService, personService);
    }

    public void testSchedulingInfoMessageWithExistingDeliveredMessage() {
        String messageKey = "message", messageKeyA = "message.a", messageKeyB = "message.b", messageKeyC = "message.c";
        Date currentDate = new Date();
        Date messageDate = new Date(System.currentTimeMillis() + 5 * 1000);
        boolean userPreferenceBased = true;

        Integer personId = 1;
        Person person = new Person(personId);
        PersonAttributeType mediaTypeAttr = new PersonAttributeType();
        mediaTypeAttr.setName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
        person.addAttribute(new PersonAttribute(mediaTypeAttr, MediaType.VOICE
                .toString()));

        MessageProgramEnrollment enrollment = new MessageProgramEnrollment();
        enrollment.setPersonId(personId);

        MessageDefinition messageDef = new MessageDefinition(messageKey, 1L,
                MessageType.INFORMATIONAL);

        List<Message> messagesToRemove = new ArrayList<Message>();

        List<ScheduledMessage> existingMessages = new ArrayList<ScheduledMessage>();
        ScheduledMessage schedMessage = new ScheduledMessage();
        Message msg = messageDef.createMessage(schedMessage);
        msg.setAttemptDate(new Timestamp(messageDate.getTime()));
        msg.setAttemptStatus(MessageStatus.DELIVERED);
        schedMessage.getMessageAttempts().add(msg);
        existingMessages.add(schedMessage);

        expect(contextService.getMotechService()).andReturn(motechService)
                .atLeastOnce();
        expect(personService.getPerson(personId)).andReturn(person);
        expect(motechService.getMessageDefinition(messageKey)).andReturn(
                messageDef);
        expect(
                motechService.getMessages(personId, enrollment, messageDef,
                        messageDate, MessageStatus.SHOULD_ATTEMPT)).andReturn(
                messagesToRemove);
        expect(
                motechService.getScheduledMessages(personId, messageDef,
                        enrollment, messageDate)).andReturn(existingMessages);

        replay(contextService, adminService, motechService, personService);

        registrarBean.scheduleInfoMessages(messageKey, messageKeyA, messageKeyB,
                messageKeyC, enrollment, messageDate, userPreferenceBased,
                currentDate);

        verify(contextService, adminService, motechService, personService);
    }

    public void testSendStaffCareMessages_GroupByCommunity() {

        Date forDate = new Date();
        String careGroups[] = {"ANC", "TT", "IPT"};

        Location location = new Location();
        location.setName("Test Facility");
        location.setCountyDistrict(new KassenaNankana().toString());

        Facility facility = new Facility();
        facility.setLocation(location);
        facility.setPhoneNumber("+1 555 123-1234");

        List<Facility> facilities = new ArrayList<Facility>();
        facilities.add(facility);


        Patient p = new Patient(5716);
        expect(rctService.isPatientRegisteredAndInControlGroup(p)).andReturn(false);

        ExpectedEncounter enc = new ExpectedEncounter();
        enc.setPatient(p);

        List<ExpectedEncounter> encounters = new ArrayList<ExpectedEncounter>();
        List<ExpectedObs> emptyObs = new ArrayList<ExpectedObs>();

        encounters.add(enc);

        // To Mock
        expect(motechService.getCommunityByPatient(p)).andReturn(null);
        expect(contextService.getMotechService()).andReturn(motechService).anyTimes();
        expect(motechService.getAllFacilities()).andReturn(facilities);
        expect(contextService.getAdministrationService()).andReturn(adminService).anyTimes();
        expect(adminService.getGlobalProperty(MotechConstants.GLOBAL_PROPERTY_MAX_QUERY_RESULTS)).andReturn("35").anyTimes();
        expect(motechService.getExpectedEncounter(null, facility, careGroups, null,
                                                  null, forDate, forDate, 35)).andReturn(encounters);
        expect(motechService.getExpectedObs(null, facility, careGroups, null,
                                                  null, forDate, forDate, 35)).andReturn(emptyObs);

        Capture<String> capturedMessageId = new Capture<String>();
        Capture<String> capturedPhoneNumber = new Capture<String>();
        Capture<Care[]> capturedCares = new Capture<Care[]>();
        Capture<CareMessageGroupingStrategy> capturedStrategy = new Capture<CareMessageGroupingStrategy>();
        Capture<MediaType> capturedMediaType = new Capture<MediaType>();
        Capture<Date> capturedStartDate = new Capture<Date>();
        Capture<Date> capturedEndDate = new Capture<Date>();

        expect(mobileService.sendDefaulterMessage(capture(capturedMessageId), capture(capturedPhoneNumber),
                                                  capture(capturedCares), capture(capturedStrategy),
                                                  capture(capturedMediaType), capture(capturedStartDate),
                                                  capture(capturedEndDate))).andReturn(org.motechproject.ws.MessageStatus.DELIVERED);

        replay(contextService, adminService, motechService, mobileService, rctService);

        registrarBean.sendStaffCareMessages(forDate, forDate,
                                            forDate, forDate,
                                            careGroups,
                                            false,
                                            false);

        verify(contextService, adminService, motechService, mobileService, rctService);

        assertEquals(CareMessageGroupingStrategy.COMMUNITY, capturedStrategy.getValue());
    }

    public void testSendStaffCareMessages_NoDefaulters() {

        Date forDate = new Date();
        String careGroups[] = {"ANC", "TT", "IPT"};

        Location location = new Location();
        location.setName("Test Facility");

        Facility facility = new Facility();
        facility.setLocation(location);
        facility.setPhoneNumber("+1 555 123-1234");

        List<Facility> facilities = new ArrayList<Facility>();
        facilities.add(facility);

        List<ExpectedEncounter> emptyEncounters = new ArrayList<ExpectedEncounter>();
        List<ExpectedObs> emptyObs = new ArrayList<ExpectedObs>();

        // To Mock
        expect(contextService.getMotechService()).andReturn(motechService).anyTimes();
        expect(motechService.getAllFacilities()).andReturn(facilities);
        expect(contextService.getAdministrationService()).andReturn(adminService).anyTimes();
        expect(adminService.getGlobalProperty(MotechConstants.GLOBAL_PROPERTY_MAX_QUERY_RESULTS)).andReturn("35").anyTimes();
        expect(motechService.getExpectedEncounter(null, facility, careGroups, null,
                                                  null, forDate, forDate, 35)).andReturn(emptyEncounters);
        expect(motechService.getExpectedObs(null, facility, careGroups, null,
                                                  null, forDate, forDate, 35)).andReturn(emptyObs);

        Capture<String> capturedMessage = new Capture<String>();
        Capture<String> capturedPhoneNumber = new Capture<String>();

        expect(mobileService.sendMessage(capture(capturedMessage), capture(capturedPhoneNumber))).andReturn(org.motechproject.ws.MessageStatus.DELIVERED);

        replay(contextService, adminService, motechService, mobileService);

        registrarBean.sendStaffCareMessages(forDate, forDate,
                                            forDate, forDate,
                                            careGroups,
                                            false,
                                            false);

        verify(contextService, adminService, motechService, mobileService);

        assertEquals("Test Facility has no defaulters for this week", capturedMessage.getValue());
        assertEquals("+1 555 123-1234", capturedPhoneNumber.getValue());
    }

    public void testSchedulingInfoMessageWithExistingRejectedMessage() {
        String messageKey = "message", messageKeyA = "message.a", messageKeyB = "message.b", messageKeyC = "message.c";
        Date currentDate = new Date();
        Date messageDate = new Date(System.currentTimeMillis() + 5 * 1000);
        boolean userPreferenceBased = true;

        Integer personId = 1;
        Person person = new Person(personId);
        PersonAttributeType mediaTypeAttr = new PersonAttributeType();
        mediaTypeAttr.setName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
        person.addAttribute(new PersonAttribute(mediaTypeAttr, MediaType.VOICE
                .toString()));

        MessageProgramEnrollment enrollment = new MessageProgramEnrollment();
        enrollment.setPersonId(personId);

        MessageDefinition messageDef = new MessageDefinition(messageKey, 1L,
                MessageType.INFORMATIONAL);

        List<Message> messagesToRemove = new ArrayList<Message>();

        List<ScheduledMessage> existingMessages = new ArrayList<ScheduledMessage>();
        ScheduledMessage schedMessage = new ScheduledMessage();
        Message msg = messageDef.createMessage(schedMessage);
        msg.setAttemptDate(new Timestamp(messageDate.getTime()));
        msg.setAttemptStatus(MessageStatus.REJECTED);
        schedMessage.getMessageAttempts().add(msg);
        existingMessages.add(schedMessage);

        expect(contextService.getMotechService()).andReturn(motechService)
                .atLeastOnce();
        expect(personService.getPerson(personId)).andReturn(person);
        expect(motechService.getMessageDefinition(messageKey)).andReturn(
                messageDef);
        expect(
                motechService.getMessages(personId, enrollment, messageDef,
                        messageDate, MessageStatus.SHOULD_ATTEMPT)).andReturn(
                messagesToRemove);
        expect(
                motechService.getScheduledMessages(personId, messageDef,
                        enrollment, messageDate)).andReturn(existingMessages);

        replay(contextService, adminService, motechService, personService);

        registrarBean.scheduleInfoMessages(messageKey, messageKeyA, messageKeyB,
                messageKeyC, enrollment, messageDate, userPreferenceBased,
                currentDate);

        verify(contextService, adminService, motechService, personService);
    }

    public void testSchedulingInfoMessageWithExistingCancelledMessage() {
        String messageKey = "message", messageKeyA = "message.a", messageKeyB = "message.b", messageKeyC = "message.c";
        Date currentDate = new Date();
        Date messageDate = new Date(System.currentTimeMillis() + 5 * 1000);
        boolean userPreferenceBased = true;

        Integer personId = 1;
        Person person = new Person(personId);
        PersonAttributeType mediaTypeAttr = new PersonAttributeType();
        mediaTypeAttr.setName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
        person.addAttribute(new PersonAttribute(mediaTypeAttr, MediaType.VOICE
                .toString()));

        MessageProgramEnrollment enrollment = new MessageProgramEnrollment();
        enrollment.setPersonId(personId);

        MessageDefinition messageDef = new MessageDefinition(messageKey, 1L,
                MessageType.INFORMATIONAL);

        List<Message> messagesToRemove = new ArrayList<Message>();

        List<ScheduledMessage> existingMessages = new ArrayList<ScheduledMessage>();
        ScheduledMessage schedMessage = new ScheduledMessage();
        Message msg = messageDef.createMessage(schedMessage);
        msg.setAttemptDate(new Timestamp(messageDate.getTime()));
        msg.setAttemptStatus(MessageStatus.CANCELLED);
        schedMessage.getMessageAttempts().add(msg);
        existingMessages.add(schedMessage);

        Capture<ScheduledMessage> capturedScheduledMessage = new Capture<ScheduledMessage>();

        expect(contextService.getMotechService()).andReturn(motechService)
                .atLeastOnce();
        expect(personService.getPerson(personId)).andReturn(person);
        expect(motechService.getMessageDefinition(messageKey)).andReturn(
                messageDef);
        expect(
                motechService.getMessages(personId, enrollment, messageDef,
                        messageDate, MessageStatus.SHOULD_ATTEMPT)).andReturn(
                messagesToRemove);
        expect(
                motechService.getScheduledMessages(personId, messageDef,
                        enrollment, messageDate)).andReturn(existingMessages);

        expect(
                motechService
                        .saveScheduledMessage(capture(capturedScheduledMessage)))
                .andReturn(new ScheduledMessage());

        replay(contextService, adminService, motechService, personService);

        registrarBean.scheduleInfoMessages(messageKey, messageKeyA, messageKeyB,
                messageKeyC, enrollment, messageDate, userPreferenceBased,
                currentDate);

        verify(contextService, adminService, motechService, personService);

        ScheduledMessage scheduledMessage = capturedScheduledMessage.getValue();
        List<Message> attempts = scheduledMessage.getMessageAttempts();
        assertEquals(2, attempts.size());
        Message message1 = attempts.get(0);
        assertEquals(MessageStatus.CANCELLED, message1.getAttemptStatus());
        assertEquals(messageDate, message1.getAttemptDate());
        Message message2 = attempts.get(1);
        assertEquals(MessageStatus.SHOULD_ATTEMPT, message2.getAttemptStatus());
        assertEquals(messageDate, message2.getAttemptDate());
    }

    public void testSchedulingInfoMessageWithExistingFailedMessage() {
        String messageKey = "message", messageKeyA = "message.a", messageKeyB = "message.b", messageKeyC = "message.c";
        Date currentDate = new Date();
        Date messageDate = new Date(System.currentTimeMillis() + 5 * 1000);
        boolean userPreferenceBased = true;

        Integer personId = 1;
        Person person = new Person(personId);
        PersonAttributeType mediaTypeAttr = new PersonAttributeType();
        mediaTypeAttr.setName(MotechConstants.PERSON_ATTRIBUTE_MEDIA_TYPE);
        person.addAttribute(new PersonAttribute(mediaTypeAttr, MediaType.VOICE
                .toString()));

        MessageProgramEnrollment enrollment = new MessageProgramEnrollment();
        enrollment.setPersonId(personId);

        MessageDefinition messageDef = new MessageDefinition(messageKey, 1L,
                MessageType.INFORMATIONAL);

        List<Message> messagesToRemove = new ArrayList<Message>();

        List<ScheduledMessage> existingMessages = new ArrayList<ScheduledMessage>();
        ScheduledMessage schedMessage = new ScheduledMessage();
        Message msg = messageDef.createMessage(schedMessage);
        msg.setAttemptDate(new Timestamp(messageDate.getTime()));
        msg.setAttemptStatus(MessageStatus.ATTEMPT_FAIL);
        schedMessage.getMessageAttempts().add(msg);
        existingMessages.add(schedMessage);

        Capture<ScheduledMessage> capturedScheduledMessage = new Capture<ScheduledMessage>();

        expect(contextService.getMotechService()).andReturn(motechService)
                .atLeastOnce();
        expect(personService.getPerson(personId)).andReturn(person);
        expect(motechService.getMessageDefinition(messageKey)).andReturn(
                messageDef);
        expect(
                motechService.getMessages(personId, enrollment, messageDef,
                        messageDate, MessageStatus.SHOULD_ATTEMPT)).andReturn(
                messagesToRemove);
        expect(
                motechService.getScheduledMessages(personId, messageDef,
                        enrollment, messageDate)).andReturn(existingMessages);

        expect(
                motechService
                        .saveScheduledMessage(capture(capturedScheduledMessage)))
                .andReturn(new ScheduledMessage());

        replay(contextService, adminService, motechService, personService);

        registrarBean.scheduleInfoMessages(messageKey, messageKeyA, messageKeyB,
                messageKeyC, enrollment, messageDate, userPreferenceBased,
                currentDate);

        verify(contextService, adminService, motechService, personService);

        ScheduledMessage scheduledMessage = capturedScheduledMessage.getValue();
        List<Message> attempts = scheduledMessage.getMessageAttempts();
        assertEquals(2, attempts.size());
        Message message1 = attempts.get(0);
        assertEquals(MessageStatus.ATTEMPT_FAIL, message1.getAttemptStatus());
        assertEquals(messageDate, message1.getAttemptDate());
        Message message2 = attempts.get(1);
        assertEquals(MessageStatus.SHOULD_ATTEMPT, message2.getAttemptStatus());
        assertEquals(messageDate, message2.getAttemptDate());
    }

    public void testFilteringStaffCareMessages() {
        Patient p1 = new Patient(5716);
        Patient p2 = new Patient(5717);
        Patient p3 = new Patient(5718);
        expect(rctService.isPatientRegisteredAndInControlGroup(p1)).andReturn(false);
        expect(rctService.isPatientRegisteredAndInControlGroup(p1)).andReturn(false);
        expect(rctService.isPatientRegisteredAndInControlGroup(p2)).andReturn(false);
        expect(rctService.isPatientRegisteredAndInControlGroup(p2)).andReturn(false);
        expect(rctService.isPatientRegisteredAndInControlGroup(p3)).andReturn(false);
        expect(rctService.isPatientRegisteredAndInControlGroup(p3)).andReturn(false);

        ExpectedObs obs1 = new ExpectedObs();
        obs1.setPatient(p1);

        ExpectedObs obs2 = new ExpectedObs();
        obs2.setPatient(p2);

        ExpectedObs obs3 = new ExpectedObs();
        obs3.setPatient(p3);

        ExpectedEncounter enc1 = new ExpectedEncounter();
        enc1.setPatient(p1);

        ExpectedEncounter enc2 = new ExpectedEncounter();
        enc2.setPatient(p2);

        ExpectedEncounter enc3 = new ExpectedEncounter();
        enc3.setPatient(p3);


        List<ExpectedObs> expObs = new ArrayList<ExpectedObs>();
        List<ExpectedEncounter> expEnc = new ArrayList<ExpectedEncounter>();

        expObs.add(obs1);
        expObs.add(obs2);
        expObs.add(obs3);

        expEnc.add(enc1);
        expEnc.add(enc2);
        expEnc.add(enc3);

        replay(rctService);
        List<ExpectedObs> filteredObs = registrarBean.filterRCTObs(new ArrayList(expObs));
        List<ExpectedEncounter> filteredEnc = registrarBean.filterRCTEncounters(new ArrayList(expEnc));

        verify(rctService);

        assertEquals(3, filteredObs.size());
        ExpectedObs obs = filteredObs.get(0);
        assertEquals(p1.getPatientId(), obs.getPatient().getPatientId());

        assertEquals(3, filteredEnc.size());
        ExpectedEncounter enc = filteredEnc.get(0);
        assertEquals(p1.getPatientId(), enc.getPatient().getPatientId());

    }
}
