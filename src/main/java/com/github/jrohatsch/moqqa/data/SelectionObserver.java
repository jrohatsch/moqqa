package com.github.jrohatsch.moqqa.data;

import com.github.jrohatsch.moqqa.domain.PathListItem;

public interface SelectionObserver {
    void update(PathListItem selection);
    void clear();
}
