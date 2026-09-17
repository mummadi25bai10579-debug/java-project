package com.library.interfaces;

import java.util.List;

/**
 * Generic search interface for catalog and registry components.
 *
 * @param <T> the entity type being searched
 */
public interface Searchable<T> {
    List<T> search(String keyword);
}
