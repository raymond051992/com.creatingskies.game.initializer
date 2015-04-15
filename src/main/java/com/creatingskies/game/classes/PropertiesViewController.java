package com.creatingskies.game.classes;

import com.creatingskies.game.model.IRecord;

public abstract class PropertiesViewController extends ViewController{

	private Action currentAction;
	private IRecord currentRecord;
	
	public Action getCurrentAction() {
		return currentAction;
	}
	
	public void setCurrentAction(Action currentAction) {
		this.currentAction = currentAction;
	}
	
	public IRecord getCurrentRecord() {
		return currentRecord;
	}
	
	public void setCurrentRecord(IRecord currentRecord) {
		this.currentRecord = currentRecord;
	}
}
