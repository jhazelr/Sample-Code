package csse374.revengd.app.transformer;

import java.util.List;

import csse374.revengd.app.filter.IFilter;
import csse374.revengd.app.model.Database;

public abstract class Transformer {
	List<IFilter> filters;

	public Transformer(List<IFilter> filters) {
		this.filters = filters;
	}

	public void setName(String name) {
		// do nothing
	}

	public abstract Database transform(Database db);

	public void addFilter(IFilter f) {
		this.filters.add(f);
	}
}
