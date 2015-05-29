package com.scapelog.client.event.parser;

import com.scapelog.api.event.impl.VariableEvent;
import com.scapelog.client.reflection.wrappers.Settings;

public final class VariableEventParser extends EventParser<VariableEvent> {

	public VariableEventParser() {
		super(VariableEvent.class);
	}

	@Override
	public VariableEvent parse(String[] messageParts) {
		int id = Integer.parseInt(messageParts[1]);
		int value = Integer.parseInt(messageParts[2]);
		Settings.set(id, value);
		return new VariableEvent(id, value);
	}

}