package org.reactfx.collection;

import java.util.List;
import java.util.function.Function;

import javafx.collections.ObservableList;

import org.reactfx.Subscription;
import org.reactfx.util.Lists;

class MappedList<E, F> extends ObsListBase<E> implements ReadOnlyObsListImpl<E> {
    private final ObservableList<? extends F> source;
    private final Function<? super F, ? extends E> mapper;

    public MappedList(ObservableList<? extends F> source, Function<? super F, ? extends E> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    public E get(int index) {
        return mapper.apply(source.get(index));
    }

    @Override
    public int size() {
        return source.size();
    }

    @Override
    protected Subscription bindToInputs() {
        return ObsList.<F>observeChanges(source, this::sourceChanged);
    }

    private void sourceChanged(ListChange<? extends F> change) {
        fireChange(new ListChange<E>() {

            @Override
            public List<TransientListModification<E>> getModifications() {
                List<? extends TransientListModification<? extends F>> mods = change.getModifications();
                return Lists.<TransientListModification<? extends F>, TransientListModification<E>>mappedView(mods, mod -> new TransientListModification<E>() {

                    @Override
                    public int getFrom() {
                        return mod.getFrom();
                    }

                    @Override
                    public int getAddedSize() {
                        return mod.getAddedSize();
                    }

                    @Override
                    public List<? extends E> getRemoved() {
                        return Lists.mappedView(mod.getRemoved(), mapper);
                    }

                    @Override
                    public ObservableList<? extends E> getList() {
                        return MappedList.this;
                    }

                });
            }

        });
    }
}