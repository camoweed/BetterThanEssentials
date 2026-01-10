package wyspr.BTE.utils;


public enum TPARequestType {
	TPA, TPAHERE;

	public String toString() {
		switch (this) {
			case TPA:
				return "To you";
			case TPAHERE:
				return "To them";
		}
		return "";
	}
}
