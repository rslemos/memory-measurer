package objectexplorer;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import objectexplorer.ObjectExplorer.Feature;
import objectexplorer.ObjectVisitor.Traversal;

import org.junit.Test;
import org.mockito.InOrder;

@SuppressWarnings("unchecked")
public class ObjectExplorerUnitTest {
	
	@Test
	public void testExploreNull() {
		ObjectVisitor<Object> visitor = mock(ObjectVisitor.class);
		final Object result = new Object();
		
		when(visitor.result()).thenReturn(result);
		
		Object root = null;
		assertThat(
				ObjectExplorer.exploreObject(root, visitor, EnumSet.noneOf(Feature.class)),
				is(sameInstance(result))
			);
		
		verify(visitor).result();
		verifyNoMoreInteractions(visitor);
	}

	@Test
	public void testExplorePlainInode() {
		ObjectVisitor<Object> visitor = mock(ObjectVisitor.class);
		final Object result = new Object();
		
		when(visitor.visit((Chain)anyObject())).thenReturn(Traversal.EXPLORE);
		when(visitor.result()).thenReturn(result);
		
		Inode root = new Inode();
		
		assertThat(
				ObjectExplorer.exploreObject(root, visitor, EnumSet.noneOf(Feature.class)),
				is(sameInstance(result))
			);
		
		InOrder inOrder = inOrder(visitor);
		
		inOrder.verify(visitor).visit((Chain)argThat(allOf(
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(root))
			)));
		
		inOrder.verify(visitor).result();
		verifyNoMoreInteractions(visitor);
	}

	@Test
	public void testExploreEmptyFolder() {
		final ObjectVisitor<Object> visitor = mock(ObjectVisitor.class);
		final Object result = new Object();
		
		when(visitor.visit((Chain)anyObject())).thenReturn(Traversal.EXPLORE);
		when(visitor.result()).thenReturn(result);
		
		final Folder root = new Folder();
		root.contents = new Inode[] {};
		
		assertThat(
				ObjectExplorer.exploreObject(root, visitor, EnumSet.noneOf(Feature.class)),
				is(sameInstance(result))
			);
		
		final InOrder inOrder = inOrder(visitor);
		
		inOrder.verify(visitor).visit((Chain)argThat(allOf(
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(root))
			)));

		inOrder.verify(visitor).visit((Chain.FieldChain)argThat(allOf(
				is(instanceOf(Chain.FieldChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(root.contents))
				// won't work since hamcrest can't access Chain.FieldChain
				// hasProperty("field", hasProperty("name", is(equalTo("contents"))))
			)));
		
		inOrder.verify(visitor).result();
		verifyNoMoreInteractions(visitor);
	}

	@Test
	public void testExploreFolderWith1NullSubinode() {
		final ObjectVisitor<Object> visitor = mock(ObjectVisitor.class);
		final Object result = new Object();
		
		when(visitor.visit((Chain)anyObject())).thenReturn(Traversal.EXPLORE);
		when(visitor.result()).thenReturn(result);
		
		final Folder root = new Folder();
		root.contents = new Inode[] { null };
		
		assertThat(
				ObjectExplorer.exploreObject(root, visitor, EnumSet.of(Feature.VISIT_NULL)),
				is(sameInstance(result))
			);
		
		final InOrder inOrder = inOrder(visitor);
		
		inOrder.verify(visitor).visit((Chain)argThat(allOf(
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(root))
			)));

		inOrder.verify(visitor).visit((Chain.FieldChain)argThat(allOf(
				is(instanceOf(Chain.FieldChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(root.contents))
				// won't work since hamcrest can't access Chain.FieldChain
				// hasProperty("field", hasProperty("name", is(equalTo("contents"))))
			)));

		inOrder.verify(visitor).visit((Chain.ArrayIndexChain)argThat(allOf(
				is(instanceOf(Chain.ArrayIndexChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", nullValue(Inode.class)),
				hasProperty("parent", hasProperty("value", sameInstance(root.contents)))
				// won't work since hamcrest can't access Chain.ArrayIndexChain
				// hasProperty("arrayIndex", is(equalTo(0)))
			)));
		
		inOrder.verify(visitor).result();
		verifyNoMoreInteractions(visitor);
	}

	@Test
	public void testExploreFolderWith1FileWithoutContents() {
		final ObjectVisitor<Object> visitor = mock(ObjectVisitor.class);
		final Object result = new Object();
		
		when(visitor.visit((Chain)anyObject())).thenReturn(Traversal.EXPLORE);
		when(visitor.result()).thenReturn(result);
		
		Folder root = new Folder();
		File file = new File();
		
		root.contents = new Inode[] { file };
		file.contents = null;
		file.created = 42;
		
		assertThat(
				ObjectExplorer.exploreObject(root, visitor, EnumSet.allOf(Feature.class)),
				is(sameInstance(result))
			);
		
		final InOrder inOrder = inOrder(visitor);
		
		inOrder.verify(visitor).visit((Chain)argThat(allOf(
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(root))
			)));

		inOrder.verify(visitor).visit((Chain.FieldChain)argThat(allOf(
				is(instanceOf(Chain.FieldChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(root.contents))
				// won't work since hamcrest can't access Chain.FieldChain
				// hasProperty("field", hasProperty("name", is(equalTo("contents"))))
			)));

		inOrder.verify(visitor).visit((Chain.ArrayIndexChain)argThat(allOf(
				is(instanceOf(Chain.ArrayIndexChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(file)),
				hasProperty("parent", hasProperty("value", sameInstance(root.contents)))
				// won't work since hamcrest can't access Chain.ArrayIndexChain
				// hasProperty("arrayIndex", is(equalTo(0)))
			)));
		
		inOrder.verify(visitor).visit((Chain.FieldChain)argThat(allOf(
				is(instanceOf(Chain.FieldChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", nullValue(byte[].class)),
				hasProperty("parent", hasProperty("value", sameInstance(file)))
				// won't work since hamcrest can't access Chain.FieldChain
				// hasProperty("field", hasProperty("name", is(equalTo("contents"))))
			)));

		inOrder.verify(visitor).visit((Chain.FieldChain)argThat(allOf(
				is(instanceOf(Chain.FieldChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", is(equalTo(file.created))),
				hasProperty("parent", hasProperty("value", sameInstance(file)))
				// won't work since hamcrest can't access Chain.FieldChain
				// hasProperty("field", hasProperty("name", is(equalTo("created"))))
			)));
		
		inOrder.verify(visitor).result();
		verifyNoMoreInteractions(visitor);
	}

	@Test
	public void testExploreFolderWith1FileWithContents() {
		final Object result = new Object();
		final ObjectVisitor<Object> visitor = mock(ObjectVisitor.class);
		
		when(visitor.visit((Chain)anyObject())).thenReturn(Traversal.EXPLORE);
		when(visitor.result()).thenReturn(result);
		
		Folder root = new Folder();
		File file = new File();
		
		root.contents = new Inode[] { file };
		file.contents = new byte[] { 0x0, 0x1, 0x2 };
		file.created = 0x42;
		
		assertThat(
				ObjectExplorer.exploreObject(root, visitor, EnumSet.allOf(Feature.class)),
				is(sameInstance(result))
			);
		
		final InOrder inOrder = inOrder(visitor);
		
		inOrder.verify(visitor).visit((Chain)argThat(allOf(
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(root))
			)));

		inOrder.verify(visitor).visit((Chain.FieldChain)argThat(allOf(
				is(instanceOf(Chain.FieldChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(root.contents))
				// won't work since hamcrest can't access Chain.FieldChain
				// hasProperty("field", hasProperty("name", is(equalTo("contents"))))
			)));

		inOrder.verify(visitor).visit((Chain.ArrayIndexChain)argThat(allOf(
				is(instanceOf(Chain.ArrayIndexChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(file)),
				hasProperty("parent", hasProperty("value", sameInstance(root.contents)))
				// won't work since hamcrest can't access Chain.ArrayIndexChain
				// hasProperty("arrayIndex", is(equalTo(0)))
			)));

		inOrder.verify(visitor).visit((Chain.FieldChain)argThat(allOf(
				is(instanceOf(Chain.FieldChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", is(equalTo(file.created))),
				hasProperty("parent", hasProperty("value", sameInstance(file)))
				// won't work since hamcrest can't access Chain.FieldChain
				// hasProperty("field", hasProperty("name", is(equalTo("created"))))
			)));
		
		inOrder.verify(visitor).visit((Chain.FieldChain)argThat(allOf(
				is(instanceOf(Chain.FieldChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", sameInstance(file.contents)),
				hasProperty("parent", hasProperty("value", sameInstance(file)))
				// won't work since hamcrest can't access Chain.FieldChain
				// hasProperty("field", hasProperty("name", is(equalTo("contents"))))
			)));
		
		inOrder.verify(visitor).visit((Chain.ArrayIndexChain)argThat(allOf(
				is(instanceOf(Chain.ArrayIndexChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", is(equalTo(file.contents[2]))),
				hasProperty("parent", hasProperty("value", sameInstance(file.contents)))
				// won't work since hamcrest can't access Chain.ArrayIndexChain
				// hasProperty("arrayIndex", is(equalTo(2)))
			)));
		
		inOrder.verify(visitor).visit((Chain.ArrayIndexChain)argThat(allOf(
				is(instanceOf(Chain.ArrayIndexChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", is(equalTo(file.contents[1]))),
				hasProperty("parent", hasProperty("value", sameInstance(file.contents)))
				// won't work since hamcrest can't access Chain.ArrayIndexChain
				// hasProperty("arrayIndex", is(equalTo(1)))
			)));
		
		inOrder.verify(visitor).visit((Chain.ArrayIndexChain)argThat(allOf(
				is(instanceOf(Chain.ArrayIndexChain.class)),
				hasProperty("root", sameInstance(root)),
				hasProperty("value", is(equalTo(file.contents[0]))),
				hasProperty("parent", hasProperty("value", sameInstance(file.contents)))
				// won't work since hamcrest can't access Chain.ArrayIndexChain
				// hasProperty("arrayIndex", is(equalTo(0)))
			)));
		
		inOrder.verify(visitor).result();
		verifyNoMoreInteractions(visitor);
	}
}

/*****************************************************************************/

class Inode {
	
}

class Folder extends Inode {
	public Inode[] contents;
}

class File extends Inode {
	public byte[] contents;
	public int created;
}
