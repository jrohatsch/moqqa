package com.github.jrohatsch.moqqa.data;

import com.github.jrohatsch.moqqa.domain.PathListItem;

import java.util.Optional;

public interface SelectionObserver {
    void update(PathListItem selection);
    void clear();
}
