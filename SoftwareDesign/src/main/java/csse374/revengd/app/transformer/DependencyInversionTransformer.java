package csse374.revengd.app.transformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;
import csse374.revengd.app.model.Dependency;
import csse374.revengd.app.model.Pattern;
import csse374.revengd.app.model.Dependency.RELATIONSHIP;
import soot.SootClass;

public class DependencyInversionTransformer extends Transformer {
	private String name;
	private Database db;

	public DependencyInversionTransformer() {
		super(new ArrayList<IFilter>());
	}

	public DependencyInversionTransformer(List<IFilter> filters) {
		super(filters);
	}

	@Override
	public Database transform(Database db) {
		this.db = db;

		@SuppressWarnings("unchecked")
		List<Object> depen = db.getFilteredObjects(List.class, "dependencies");
		Collection<Dependency> dependencies = this.db.castElements(Dependency.class, depen);

		checkDependencyInversion(dependencies);

		return db;
	}

	private void checkDependencyInversion(Collection<Dependency> dependencies) {
		ListMultimap<SootClass, Dependency> mapofDependencie = ArrayListMultimap.create();
		for (Dependency c : dependencies) {
			if (c.getType().equals(RELATIONSHIP.DEPENDS) || c.getType().equals(RELATIONSHIP.DEPENDS_MANY)
					|| c.getType().equals(RELATIONSHIP.ASSOCIATES)
					|| c.getType().equals(RELATIONSHIP.ASSOCIATES_MANY)) {
				SootClass toClass = c.getTo();
				SootClass fromClass = c.getFrom();
				if ((!toClass.isAbstract()) || (!toClass.isInterface())) {
					if (toClass.hasSuperclass() || (toClass.getInterfaceCount() > 1)) {
						if (!toClass.equals(fromClass)) {
							if (!toClass.getSuperclass().equals(fromClass)) {
								mapofDependencie.put(fromClass, c);
							}
						}
					}
				} else if (toClass.isAbstract() || toClass.isInterface()) {
					if ((!fromClass.isAbstract()) || (!fromClass.isInterface())) {
						if (fromClass.hasSuperclass() || (fromClass.getInterfaceCount() > 1)) {
							if (!toClass.equals(fromClass)) {
								if (!toClass.getSuperclass().equals(fromClass)) {
									mapofDependencie.put(fromClass, c);
								}
							}
						}
					}
				}
			}
		}
		makePattern(mapofDependencie);
	}

	private void makePattern(ListMultimap<SootClass, Dependency> mapofDependencie) {

		for (SootClass s : mapofDependencie.keySet()) {
			System.out.println("====WE GOT A VILATION=====");
			System.out.println(s.getName());
			System.out.println('\n');
			List<Dependency> listofDep = mapofDependencie.get(s);

			Pattern newPattern = new Pattern(name);
			newPattern.putComponent("class", s);

			for (Dependency d : listofDep) {
				newPattern.addRelation(d);
			}

			@SuppressWarnings("unchecked")
			List<Object> pats = db.getFilteredObjects(List.class, "patterns");
			Collection<Pattern> patterns = db.castElements(Pattern.class, pats);
			patterns.add(newPattern);
			db.putObject("patterns", patterns);

		}

	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
