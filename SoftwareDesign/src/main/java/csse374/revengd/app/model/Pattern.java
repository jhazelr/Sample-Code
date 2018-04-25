package csse374.revengd.app.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

public class Pattern {
	private ListMultimap<String, SootClass> components;
	private String name;
	private List<Dependency> relations;
	private List<SootMethod> methods;
	private List<SootField> fields;

	public Pattern(String name) {
		this.components = ArrayListMultimap.create();
		this.name = name;
		this.relations = new ArrayList<>();
		this.methods = new ArrayList<>();
		this.fields = new ArrayList<>();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ListMultimap<String, SootClass> getComponents() {
		return components;
	}

	public void putComponent(String key, SootClass clazz) {
		components.put(key, clazz);
	}

	public List<Dependency> getRelations() {
		return this.relations;
	}

	public void addRelation(Dependency dependency) {
		relations.add(dependency);
	}

	public List<SootMethod> getMethods() {
		return methods;
	}

	public void addMethod(SootMethod method) {
		methods.add(method);
	}

	public List<SootField> getFields() {
		return fields;
	}

	public void addField(SootField field) {
		fields.add(field);
	}
}
