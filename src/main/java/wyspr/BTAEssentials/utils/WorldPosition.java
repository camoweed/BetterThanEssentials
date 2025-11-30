package wyspr.BTAEssentials.utils;

import java.io.Serializable;
import java.util.Objects;

public class WorldPosition implements Serializable {
	private static final long serialVersionUID = 1L; // Ensures version compatibility during deserialization

	public double x, y, z;
	public int dimID;

	public WorldPosition(double x, double y, double z, int dimID) {
		this.x     = x;
		this.y     = y;
		this.z     = z;
		this.dimID = dimID;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y, z, dimID);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WorldPosition that = (WorldPosition) o;
		return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0 && Double.compare(
			that.z,
			z
		) == 0 && dimID == that.dimID;
	}
}
