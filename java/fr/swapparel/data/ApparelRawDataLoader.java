package fr.swapparel.data;

import java.util.List;

import fr.swapparel.data.SavedApparel;

public interface ApparelRawDataLoader {
    void onDataReceived(List<SavedApparel> response);
}
