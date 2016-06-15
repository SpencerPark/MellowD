package cas.cs4tb3.mellowd.intermediate;

import java.util.LinkedList;
import java.util.List;

public class Phrase implements Playable {
    protected final List<Playable> elements;

    public Phrase() {
        this.elements = new LinkedList<>();
    }

    public Phrase(Phrase toCopy) {
        this.elements = new LinkedList<>(toCopy.elements);
    }

    @Override
    public void play(MIDIChannel channel) {
        elements.forEach(p -> p.play(channel));
    }
}
