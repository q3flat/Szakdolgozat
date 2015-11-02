package hu.elte.bfw1p6.poker.command.type;

import hu.elte.bfw1p6.poker.command.type.api.HousePokerCommandType;

public enum HoldemPlayerPokerCommandType implements HousePokerCommandType<HoldemPlayerPokerCommandType> {
	BLIND, CALL, CHECK, FOLD, RAISE, QUIT;

	@Override
	public HoldemPlayerPokerCommandType getNext() {
		return values()[(ordinal()+1) % values().length];
	}

	@Override
	public HoldemPlayerPokerCommandType[] getValues() {
		return values();
	}

	@Override
	public HoldemPlayerPokerCommandType getActual() {
		return this;
	}
}
