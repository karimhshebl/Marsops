package emergency;

import assets.resources.ResourceRequirement;
import enums.EmergencyType;
import enums.ResourceType;
import interfaces.ILocation;
import interfaces.IPerson;

public class MedicalEmergency extends Emergency {
    private static final int INJURY = 40;
    private static final int RECOVERY = 40;
    private static final int DETERIORATION = 20;

    private final IPerson patient;

    public MedicalEmergency(IPerson patient) {
        super(EmergencyType.MEDICAL);
        if (patient == null) throw new IllegalArgumentException("Patient cannot be null");
        this.patient = patient;
    }

    @Override
    public ILocation getLocation() { return patient.getLocation(); }

    @Override
    public ResponderRequirement getResponderRequirement() { return new ResponderRequirement(1, 0); }

    @Override
    public ResourceRequirement getResponseCost() {
        return new ResourceRequirement().add(ResourceType.MEDICAL_SUPPLY, 2);
    }

    @Override
    public void onDetected() { patient.changeHealthBy(-INJURY); }

    @Override
    public void onResolved() { patient.changeHealthBy(RECOVERY); }

    @Override
    public void onUnresolved() { patient.changeHealthBy(-DETERIORATION); }

    public IPerson getPatient() { return patient; }
}