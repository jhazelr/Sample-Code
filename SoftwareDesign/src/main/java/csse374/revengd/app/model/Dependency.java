package csse374.revengd.app.model;

import soot.SootClass;

public class Dependency {
	private SootClass from;
	private SootClass to;
	private RELATIONSHIP type;

	public enum RELATIONSHIP {
		IMPLEMENTS, EXTENDS, DEPENDS, ASSOCIATES, DEPENDS_MANY, ASSOCIATES_MANY
	};

	public Dependency(SootClass from, SootClass to, RELATIONSHIP type) {
		this.from = from;
		this.to = to;
		this.type = type;
	}

	public SootClass getFrom() {
		return from;
	}

	public SootClass getTo() {
		return to;
	}

	public RELATIONSHIP getType() {
		return type;
	}

	public void setType(RELATIONSHIP type) {
		this.type = type;
	}
}
