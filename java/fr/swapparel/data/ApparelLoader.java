package fr.swapparel.data;

import java.util.List;

public interface ApparelLoader {
    //IMPORTANT: returns empty list if any error
    void onApparelReceived(List<Apparel> response);
}