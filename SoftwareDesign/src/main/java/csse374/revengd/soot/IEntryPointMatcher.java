package csse374.revengd.soot;

import java.util.Collection;

import soot.SootClass;
import soot.SootMethod;

/**
 * SOOT needs an entry-point or set of entry-points to construct call-graph and to perform other
 * analyses. Use this matcher with {@link SceneBuilder} to decide which {@link SootMethod}s should 
 * be used as entry point methods for SOOT analysis.
 * 
 * @see {@link SceneBuilder#addEntryPointMatcher(IEntryPointMatcher)} 
 * {@link SceneBuilder#addEntryPointMatcher(Collection)} 
 */
public interface IEntryPointMatcher {
	public boolean match(SootClass clazz);
	public boolean match(SootMethod method);
}
