package objectexplorer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

public class ChainUnitTest {
	
	private static final Field DUMMY1_FIELD;
	private static final Field DUMMY2_FIELD;
	private static final Field DUMMYINT_FIELD;
	
	static {
		try {
			DUMMY1_FIELD = ChainUnitTest.class.getField("dummy1");
			DUMMY2_FIELD = ChainUnitTest.class.getField("dummy2");
			DUMMYINT_FIELD = ChainUnitTest.class.getField("dummyint");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	};
	
	public Object dummy1;
	public List<?> dummy2;
	public int dummyint;
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testObject() {
		Object root = new Object();
		Chain chain = Chain.root(root);
		assertThat(chain.hasParent(), is(equalTo(false)));
		try {
			chain.getParent();
			fail("Should have thrown " + IllegalStateException.class);
		} catch (IllegalStateException e) { /* ignore */ }
		assertThat(chain.getRoot(), is(sameInstance(root)));
		assertThat(chain.getValue(), is(sameInstance(root)));
		assertThat((Class)chain.getValueType(), is(equalTo((Class)root.getClass())));
		assertThat(chain.isPrimitive(), is(equalTo(false)));
		assertThat(chain.isThroughArrayIndex(), is(equalTo(false)));
		assertThat(chain.isThroughField(), is(equalTo(false)));
		assertThat(chain.toString(), is(equalTo(root.toString())));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testObject_dummy1_Object() {
		Object root = new Object();
		Object link1 = new Object();
		Chain chain = Chain.root(root).appendField(DUMMY1_FIELD, link1);
		
		assertThat(chain.hasParent(), is(equalTo(true)));
		assertThat(chain.getParent(), is(notNullValue(Chain.class)));
		assertThat(chain.getParent().getValue(), is(sameInstance(root)));
		assertThat(chain.getRoot(), is(sameInstance(root)));
		assertThat(chain.getValue(), is(sameInstance(link1)));
		assertThat((Class)chain.getValueType(), is(equalTo((Class)DUMMY1_FIELD.getType())));
		assertThat(chain.isPrimitive(), is(equalTo(false)));
		assertThat(chain.isThroughArrayIndex(), is(equalTo(false)));
		assertThat(chain.isThroughField(), is(equalTo(true)));
		assertThat(chain.toString(), is(equalTo(root.toString() + "->" + DUMMY1_FIELD.getName())));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testObject_dummy1_Object_dummy2_Object() {
		Object root = new Object();
		Object link1 = new Object();
		Object link2 = new Object();
		Chain chain = Chain.root(root).appendField(DUMMY1_FIELD, link1).appendField(DUMMY2_FIELD, link2);
		
		assertThat(chain.hasParent(), is(equalTo(true)));
		assertThat(chain.getParent(), is(notNullValue(Chain.class)));
		assertThat(chain.getParent().getValue(), is(sameInstance(link1)));
		assertThat(chain.getRoot(), is(sameInstance(root)));
		assertThat(chain.getValue(), is(sameInstance(link2)));
		assertThat((Class)chain.getValueType(), is(equalTo((Class)DUMMY2_FIELD.getType())));
		assertThat(chain.isPrimitive(), is(equalTo(false)));
		assertThat(chain.isThroughArrayIndex(), is(equalTo(false)));
		assertThat(chain.isThroughField(), is(equalTo(true)));
		assertThat(chain.toString(), is(equalTo(root.toString() + "->" + DUMMY1_FIELD.getName() + "->" + DUMMY2_FIELD.getName())));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testObjectArray_0_Object() {
		Object[] root = new Object[1];
		Object value0 = new Object();
		Chain chain = Chain.root(root).appendArrayIndex(0, value0);
		
		assertThat(chain.hasParent(), is(equalTo(true)));
		assertThat(chain.getParent(), is(notNullValue(Chain.class)));
		assertThat(chain.getParent().getValue(), is(sameInstance((Object)root)));
		assertThat(chain.getRoot(), is(sameInstance((Object)root)));
		assertThat(chain.getValue(), is(sameInstance(value0)));
		assertThat((Class)chain.getValueType(), is(equalTo((Class)root.getClass().getComponentType())));
		assertThat(chain.isPrimitive(), is(equalTo(false)));
		assertThat(chain.isThroughArrayIndex(), is(equalTo(true)));
		assertThat(chain.isThroughField(), is(equalTo(false)));
		assertThat(chain.toString(), is(equalTo(root.toString() + "->[0]")));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testObjectArray_1_Object() {
		Object[] root = new Object[2];
		Object value1 = new Object();
		Chain chain = Chain.root(root).appendArrayIndex(1, value1);
		
		assertThat(chain.hasParent(), is(equalTo(true)));
		assertThat(chain.getParent(), is(notNullValue(Chain.class)));
		assertThat(chain.getParent().getValue(), is(sameInstance((Object)root)));
		assertThat(chain.getRoot(), is(sameInstance((Object)root)));
		assertThat(chain.getValue(), is(sameInstance(value1)));
		assertThat((Class)chain.getValueType(), is(equalTo((Class)root.getClass().getComponentType())));
		assertThat(chain.isPrimitive(), is(equalTo(false)));
		assertThat(chain.isThroughArrayIndex(), is(equalTo(true)));
		assertThat(chain.isThroughField(), is(equalTo(false)));
		assertThat(chain.toString(), is(equalTo(root.toString() + "->[1]")));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testObjectArray_0_ObjectArray_1_Object() {
		Object[] root = new Object[1];
		Object[] value0 = new Object[2];
		Object value01 = new Object();
		Chain chain = Chain.root(root).appendArrayIndex(0, value0).appendArrayIndex(1, value01);
		
		assertThat(chain.hasParent(), is(equalTo(true)));
		assertThat(chain.getParent(), is(notNullValue(Chain.class)));
		assertThat(chain.getParent().getValue(), is(sameInstance((Object)value0)));
		assertThat(chain.getRoot(), is(sameInstance((Object)root)));
		assertThat(chain.getValue(), is(sameInstance(value01)));
		assertThat((Class)chain.getValueType(), is(equalTo((Class)value0.getClass().getComponentType())));
		assertThat(chain.isPrimitive(), is(equalTo(false)));
		assertThat(chain.isThroughArrayIndex(), is(equalTo(true)));
		assertThat(chain.isThroughField(), is(equalTo(false)));
		assertThat(chain.toString(), is(equalTo(root.toString() + "->[0]->[1]")));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testObject_dummy1_ObjectArray_0() {
		Object root = new Object();
		Object[] link1 = new Object[1];
		Object value0 = new Object();
		Chain chain = Chain.root(root).appendField(DUMMY1_FIELD, link1).appendArrayIndex(0, value0);
		
		assertThat(chain.hasParent(), is(equalTo(true)));
		assertThat(chain.getParent(), is(notNullValue(Chain.class)));
		assertThat(chain.getParent().getValue(), is(sameInstance((Object)link1)));
		assertThat(chain.getRoot(), is(sameInstance(root)));
		assertThat(chain.getValue(), is(sameInstance(value0)));
		assertThat((Class)chain.getValueType(), is(equalTo((Class)link1.getClass().getComponentType())));
		assertThat(chain.isPrimitive(), is(equalTo(false)));
		assertThat(chain.isThroughArrayIndex(), is(equalTo(true)));
		assertThat(chain.isThroughField(), is(equalTo(false)));
		assertThat(chain.toString(), is(equalTo(root.toString() + "->" + DUMMY1_FIELD.getName() + "->[0]")));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testObjectArray_1_Object_dummy2_Object() {
		Object[] root = new Object[1];
		Object value1 = new Object();
		Object link1 = new Object();
		Chain chain = Chain.root(root).appendArrayIndex(1, value1).appendField(DUMMY2_FIELD, link1);
		
		assertThat(chain.hasParent(), is(equalTo(true)));
		assertThat(chain.getParent(), is(notNullValue(Chain.class)));
		assertThat(chain.getParent().getValue(), is(sameInstance((Object)value1)));
		assertThat(chain.getRoot(), is(sameInstance((Object)root)));
		assertThat(chain.getValue(), is(sameInstance(link1)));
		assertThat((Class)chain.getValueType(), is(equalTo((Class)DUMMY2_FIELD.getType())));
		assertThat(chain.isPrimitive(), is(equalTo(false)));
		assertThat(chain.isThroughArrayIndex(), is(equalTo(false)));
		assertThat(chain.isThroughField(), is(equalTo(true)));
		assertThat(chain.toString(), is(equalTo(root.toString() + "->[1]->" + DUMMY2_FIELD.getName())));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testObject_dummyint_42() {
		Object root = new Object();
		int link1 = 42;
		Chain chain = Chain.root(root).appendField(DUMMYINT_FIELD, link1);
		
		assertThat(chain.hasParent(), is(equalTo(true)));
		assertThat(chain.getParent(), is(notNullValue(Chain.class)));
		assertThat(chain.getParent().getValue(), is(sameInstance(root)));
		assertThat(chain.getRoot(), is(sameInstance(root)));
		assertThat(chain.getValue(), is(equalTo((Object)link1)));
		assertThat((Class)chain.getValueType(), is(equalTo((Class)DUMMYINT_FIELD.getType())));
		assertThat(chain.isPrimitive(), is(equalTo(true)));
		assertThat(chain.isThroughArrayIndex(), is(equalTo(false)));
		assertThat(chain.isThroughField(), is(equalTo(true)));
		assertThat(chain.toString(), is(equalTo(root.toString() + "->" + DUMMYINT_FIELD.getName())));
	}
}
