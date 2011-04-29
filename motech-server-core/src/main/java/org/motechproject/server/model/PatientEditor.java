package org.motechproject.server.model;

import org.openmrs.Patient;

public class PatientEditor {

    private Patient patient;

    public PatientEditor(Patient patient) {
        this.patient = patient;
    }

    public PatientEditor removeFrom(Facility oldFacility) {
        oldFacility.remove(patient);
        return this;
    }

    public PatientEditor addTo(Facility newFacility) {
        newFacility.addPatient(patient);
        return this;
    }

    public Patient done(){
        return patient;
    }
}
