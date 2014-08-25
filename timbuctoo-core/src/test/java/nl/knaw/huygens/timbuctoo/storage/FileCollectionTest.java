package nl.knaw.huygens.timbuctoo.storage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import nl.knaw.huygens.timbuctoo.model.SystemEntity;

import org.hamcrest.Matcher;

public abstract class FileCollectionTest<T extends SystemEntity> {
	protected abstract FileCollection<T> getInstance();

	protected void verifyAddReturnsAnIdAndAddsItToTheEntity(T entity, String expectedId) {

		String actualId = getInstance().add(entity);

		assertThat(actualId, is(equalTo(expectedId)));
		assertThat(entity.getId(), is(equalTo(expectedId)));
	}

	protected void verifyAddIncrementsTheId(T entity1, T entity2, T entity3, String expectedId) {
		getInstance().add(entity1);
		getInstance().add(entity2);
		String actualId = getInstance().add(entity3);

		assertThat(actualId, is(equalTo(expectedId)));
	}

	protected void verifyAddAddsTheEntityToItsCollection(T entity) {
		String id = getInstance().add(entity);
		T foundEntity = getInstance().get(id);

		assertThat(foundEntity, is(equalTo(entity)));
	}

	protected void verifyGetAllReturnsAllTheKnownEntities(Matcher<Iterable<? extends T>> matcher) {
		// action
		StorageIterator<T> entities = getInstance().getAll();

		// verify
		assertThat(entities, is(notNullValue()));
		assertThat(entities.getAll(), matcher);
	}
}
