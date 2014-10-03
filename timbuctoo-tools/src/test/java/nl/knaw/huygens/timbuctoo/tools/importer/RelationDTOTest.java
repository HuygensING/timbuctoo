package nl.knaw.huygens.timbuctoo.tools.importer;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

public class RelationDTOTest {
	@Test
	public void testEquals() {
		RelationDTO r1 = new RelationDTO();
		RelationDTO r2 = new RelationDTO();
		assertEquals(r1, r2);

		Set<RelationDTO> set = Sets.newHashSet();
		set.add(r1);
		set.add(r2);
		assertEquals(1, set.size());
	}
}
