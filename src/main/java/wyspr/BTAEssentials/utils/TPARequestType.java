package wyspr.BTAEssentials.utils;


public enum TPARequestType {
	TPA {
		@Override
		public String toString() {
			return "To you";
		}
	}, TPAHERE {
		@Override
		public String toString() {
			return "To them";
		}
	}
}
