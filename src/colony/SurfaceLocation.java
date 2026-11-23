package colony;

import interfaces.ILocation;
import universalHelpers.Validation;

public class SurfaceLocation implements ILocation {
    private final String name;

    public SurfaceLocation(String name) {
        this.name = Validation.validateString(name, "Location name");
    }

    @Override
    public String getName() { return name; }

    @Override
    public boolean isSurface() { return true; }
}
