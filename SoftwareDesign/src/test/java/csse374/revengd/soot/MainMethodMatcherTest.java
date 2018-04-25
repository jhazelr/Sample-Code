package csse374.revengd.soot;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import soot.ArrayType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import static org.mockito.Mockito.*;

public class MainMethodMatcherTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMainMethodMatcher() {
		String className = "abc";
		MainMethodMatcher matcher = new MainMethodMatcher(className);
		
		assertNotNull(matcher);
		assertEquals(className, matcher.className);
	}

	@Test
	public void testMatchSootClassPositive() {
		String className = "csse374.revengd.soot.MockedClass";
		SootClass clazz = mock(SootClass.class);
		when(clazz.getName()).thenReturn(className);

		MainMethodMatcher matcher = new MainMethodMatcher(className);
		
		assertTrue(matcher.match(clazz));
	}

	@Test
	public void testMatchSootClassNegative() {
		String className = "csse374.revengd.soot.MockedClass";
		SootClass clazz = mock(SootClass.class);
		when(clazz.getName()).thenReturn(className);

		MainMethodMatcher matcher = new MainMethodMatcher("random");
		
		assertFalse(matcher.match(clazz));
	}
	
	@Test
	public void testMatchSootMethodPositive() {
		SootMethod method = mock(SootMethod.class);
		when(method.getName()).thenReturn("main");
		when(method.getParameterCount()).thenReturn(1);

		Type baseType = mock(RefType.class);
		when(baseType.toString()).thenReturn("java.lang.String");

		ArrayType arrayType = ArrayType.v(baseType, 1);
		when(method.getParameterType(0)).thenReturn(arrayType);

		String className = "csse374.revengd.soot.MockedClass";		
		MainMethodMatcher matcher = new MainMethodMatcher(className);

		assertTrue(matcher.match(method));
	}

	@Test
	public void testMatchSootMethodNameNotEqual() {
		SootMethod method = mock(SootMethod.class);
		when(method.getName()).thenReturn("notMain");
		when(method.getParameterCount()).thenReturn(1);

		String className = "csse374.revengd.soot.MockedClass";		
		MainMethodMatcher matcher = new MainMethodMatcher(className);
		
		assertFalse(matcher.match(method));
	}
	
	@Test
	public void testMatchSootMethodInvalidParamCount() {
		SootMethod method = mock(SootMethod.class);
		when(method.getName()).thenReturn("main");
		when(method.getParameterCount()).thenReturn(2);

		String className = "csse374.revengd.soot.MockedClass";		
		MainMethodMatcher matcher = new MainMethodMatcher(className);

		assertFalse(matcher.match(method));
	}	

	@Test
	public void testMatchSootMethodInvalidParamType() {
		SootMethod method = mock(SootMethod.class);
		when(method.getName()).thenReturn("main");
		when(method.getParameterCount()).thenReturn(1);

		Type nonArrayType = mock(Type.class);
		when(method.getParameterType(0)).thenReturn(nonArrayType);

		String className = "csse374.revengd.soot.MockedClass";		
		MainMethodMatcher matcher = new MainMethodMatcher(className);

		assertFalse(matcher.match(method));
	}
	
	@Test
	public void testMatchSootMethodInvalidParamBaseType() {
		SootMethod method = mock(SootMethod.class);
		when(method.getName()).thenReturn("main");
		when(method.getParameterCount()).thenReturn(1);

		Type baseType = mock(RefType.class);
		when(baseType.toString()).thenReturn("java.lang.NotString");

		ArrayType arrayType = ArrayType.v(baseType, 1);
		when(method.getParameterType(0)).thenReturn(arrayType);

		String className = "csse374.revengd.soot.MockedClass";		
		MainMethodMatcher matcher = new MainMethodMatcher(className);

		assertFalse(matcher.match(method));
	}	
}
